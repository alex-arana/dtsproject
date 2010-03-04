package org.dataminx.dts.batch;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.dataminx.dts.DtsException;
import org.dataminx.dts.vfs.DtsVfsUtil;
import org.dataminx.dts.vfs.FileSystemManagerDispenser;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.CreationFlagEnumeration;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class VfsMixedFilesJobPartitioningStrategy implements JobPartitioningStrategy, InitializingBean {

    private final boolean mCancelled = false;
    private String mFailureMessage = "";

    private long mTotalSize = 0;
    private int mTotalFiles = 0;

    private FileSystemManagerDispenser mFileSystemManagerDispenser;

    private long mMaxTotalByteSizePerStepLimit = 0;

    private int mMaxTotalFileNumPerStepLimit = 0;

    private ArrayList<String> mExcluded = new ArrayList<String>();

    private DtsVfsUtil mDtsVfsUtil;

    private static final Log LOGGER = LogFactory.getLog(VfsMixedFilesJobPartitioningStrategy.class);

    private MixedFilesJobStepAllocator mDtsJobStepAllocator;

    public static final int BATCH_SIZE_LIMIT = 3;

    public DtsJobDetails partitionTheJob(final JobDefinitionType jobDefinition, final String jobResourceKey)
            throws JobScopingException {
        Assert.hasText(jobResourceKey, "JobResourceKey should not be null or empty.");
        Assert.notNull(jobDefinition, "JobDefinitionType should not be null.");
        if (mMaxTotalByteSizePerStepLimit < 0) {
            throw new DtsException("MaxTotalByteSizePerLimit should be a positive number.");
        }
        if (mMaxTotalFileNumPerStepLimit < 0) {
            throw new DtsException("MaxTotalFileNumPerStepLimit should be a positive number.");
        }

        final FileSystemManager fileSystemManager = mFileSystemManagerDispenser.getFileSystemManager();

        final DtsJobDetails jobDetails = new DtsJobDetails();
        jobDetails.setJobDefinition(jobDefinition);
        jobDetails.setJobId(jobResourceKey);

        mDtsJobStepAllocator = new MixedFilesJobStepAllocator();
        mExcluded = new ArrayList<String>();
        mTotalSize = 0;
        mTotalFiles = 0;

        final List<DataTransferType> dataTransfers = new ArrayList<DataTransferType>();

        final JobDescriptionType jobDescription = jobDefinition.getJobDescription();
        if (jobDescription instanceof MinxJobDescriptionType) {
            final MinxJobDescriptionType minxJobDescription = (MinxJobDescriptionType) jobDescription;
            CollectionUtils.addAll(dataTransfers, minxJobDescription.getDataTransferArray());
        }
        if (CollectionUtils.isEmpty(dataTransfers)) {
            LOGGER.warn("DTS job request is incomplete as it does not contain any data transfer elements.");
            throw new DtsJobExecutionException("DTS job request contains no data transfer elements.");
        }

        for (final DataTransferType dataTransfer : dataTransfers) {

            try {
                final FileObject sourceParent = fileSystemManager.resolveFile(dataTransfer.getSource().getURI(),
                        mDtsVfsUtil.createFileSystemOptions(dataTransfer.getSource()));
                final FileObject targetParent = fileSystemManager.resolveFile(dataTransfer.getTarget().getURI(),
                        mDtsVfsUtil.createFileSystemOptions(dataTransfer.getTarget()));

                mDtsJobStepAllocator.createNewDataTransfer(sourceParent.getFileSystem().getRoot().getURL().toString(),
                        targetParent.getFileSystem().getRoot().getURL().toString());
                prepare(sourceParent, targetParent, dataTransfer);
            } catch (final DtsJobCancelledException e) {
                // TODO: handle DTS Job Cancel event
                e.printStackTrace();
            } catch (final FileSystemException e) {
                throw new DtsException(e);
            } catch (final JobScopingException e) {
                // TODO Auto-generated catch block
                throw e;
            }

            mDtsJobStepAllocator.closeNewDataTransfer();

        }
        LOGGER.debug("total size of files to be transferred: " + mTotalSize + " bytes");
        LOGGER.debug("list of excluded files: ");
        for (final String excluded : mExcluded) {
            LOGGER.debug(" - " + excluded);
        }

        jobDetails.setExcludedFiles(mExcluded);
        jobDetails.setTotalBytes(mTotalSize);
        jobDetails.setTotalFiles(mTotalFiles);
        jobDetails.setJobSteps(mDtsJobStepAllocator.getAllocatedJobSteps());

        for (final DtsJobStep jobStep : mDtsJobStepAllocator.getAllocatedJobSteps()) {
            LOGGER.debug(jobStep);
        }
        // let's try to put the steps in the step execution context

        // TODO: now that job scoping is run on its own step and by the master thread, we can't close 
        // the file system manager here
        // immediately close the file system manager so FileCopyTask will be able to use all of the 
        // available connections
        //mFileSystemManagerDispenser.closeFileSystemManager();

        return jobDetails;
    }

    private void prepare(final FileObject sourceParent, final FileObject destinationParent,
            final DataTransferType dataTransfer) throws DtsJobCancelledException, JobScopingException {
        if (mCancelled) {
            throw new DtsJobCancelledException();
        }

        try {
            // Handle the following cases...
            // source: /tmp/passwdDtsJobStepAllocator
            // copy to:
            // destination that does not exists: /tmp/passwd
            // destination directory that does not exists: /tmp/hello/
            //			
            // destination file that exists: /tmp/passwd
            //			
            // destination directory that exists: /tmp

            if (sourceParent.getType().equals(FileType.FILE) && !mCancelled) {

                if (sourceParent.getContent().getSize() > mMaxTotalByteSizePerStepLimit) {
                    throw new JobScopingException("file: " + sourceParent.getName()
                            + " too big. Try transferring files smaller than " + mMaxTotalByteSizePerStepLimit
                            + " bytes.");
                }

                final CreationFlagEnumeration.Enum creationFlag = dataTransfer.getTransferRequirements()
                        .getCreationFlag();

                if (!destinationParent.exists()) {
                    // Note that we are not supporting a single file transfer to
                    // a non-existent directory
                    // Any destinationParent which had a "/" at the end of it's
                    // URI will not be handled

                    // TODO: should we handle the above case?

                    addFilesToTransfer(sourceParent, destinationParent, dataTransfer);

                }
                else if (destinationParent.exists() && destinationParent.getType().equals(FileType.FILE)) {
                    // File to File

                    if (creationFlag.equals(CreationFlagEnumeration.OVERWRITE)) {
                        addFilesToTransfer(sourceParent, destinationParent, dataTransfer);
                    }
                    else {
                        mExcluded.add(sourceParent.getName().getFriendlyURI());
                    }
                }
                else {
                    // ... File to Dir

                    // create the new object
                    final String newFilePath = destinationParent.getURL() + FileName.SEPARATOR
                            + sourceParent.getName().getBaseName();
                    final FileObject destinationChild = destinationParent.getFileSystem().getFileSystemManager()
                            .resolveFile(newFilePath, destinationParent.getFileSystem().getFileSystemOptions());
                    // destinationChild.createFile();

                    if (destinationChild.exists()) {
                        if (creationFlag.equals(CreationFlagEnumeration.OVERWRITE)) {
                            addFilesToTransfer(sourceParent, destinationChild, dataTransfer);
                        }
                        else {
                            mExcluded.add(sourceParent.getName().getFriendlyURI());
                        }

                    }
                    else {
                        addFilesToTransfer(sourceParent, destinationChild, dataTransfer);
                    }
                }

            }
            else if (sourceParent.getType().equals(FileType.FOLDER) && !mCancelled) {
                // .. Dir to Dir

                // create the new object
                final String newFolderPath = destinationParent.getURL() + FileName.SEPARATOR
                        + sourceParent.getName().getBaseName();
                final FileObject destinationChild = destinationParent.getFileSystem().getFileSystemManager()
                        .resolveFile(newFolderPath, destinationParent.getFileSystem().getFileSystemOptions());

                if (!destinationChild.exists()) {
                    destinationChild.createFolder();
                }

                // get the children
                final FileObject[] sourceChildren = sourceParent.getChildren();

                // iterate through the children
                for (final FileObject sourceChild : sourceChildren) {
                    // recurse into the directory, or copy the file
                    prepare(sourceChild, destinationChild, dataTransfer);
                }
            }
        } catch (final DtsJobCancelledException e) {
            throw e;
        } catch (final JobScopingException e) {
            throw e;
        } catch (final Exception e) {

            handleError(e);
            // throw everything else that's not DtsJobCancelledException as DtsException
            throw new DtsException(e);
        }
    }

    private void addFilesToTransfer(final FileObject source, final FileObject destination,
            final DataTransferType dataTransfer) throws FileSystemException {
        LOGGER.debug("addFilesToTransfer(\"" + source.getURL() + "\", \"" + destination.getURL() + "\", dataTransfer)");
        mTotalSize += source.getContent().getSize();
        mTotalFiles++;
        mDtsJobStepAllocator.addDataTransferUnit(new DtsDataTransferUnit(source.getURL().toString(), destination
                .getURL().toString(), dataTransfer, source.getContent().getSize()));
    }

    private void handleError(final Exception e) {
        mFailureMessage = e.getMessage();
        LOGGER.error(e);

        // invokeTransferFailedListeners();
    }

    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

    public void setFileSystemManagerDispenser(final FileSystemManagerDispenser fileSystemManagerDispenser) {
        mFileSystemManagerDispenser = fileSystemManagerDispenser;
    }

    public void setMaxTotalByteSizePerStepLimit(final long maxTotalByteSizePerStepLimit) {
        mMaxTotalByteSizePerStepLimit = maxTotalByteSizePerStepLimit;
    }

    public void setMaxTotalFileNumPerStepLimit(final int maxTotalFileNumPerStepLimit) {
        mMaxTotalFileNumPerStepLimit = maxTotalFileNumPerStepLimit;

    }

    private class MixedFilesJobStepAllocator implements DtsJobStepAllocator {
        private final List<DtsJobStep> mSteps;
        private DtsJobStep mTmpDtsJobStep = null;
        private String mSourceRootFileObject;
        private String mTargetRootFileObject;

        public MixedFilesJobStepAllocator() {
            mSteps = new ArrayList<DtsJobStep>();
        }

        /**
         * This method needs to be called before a new DataTransferType instance
         * gets processed by
         * {@link org.dataminx.dts.batch.JobPartitioningStrategy#prepare()}.
         * This will make sure that new DataTransferUnits get added to a new
         * DtsJobStep.
         */
        public void createNewDataTransfer(final String sourceRootFileObject, final String targetRootFileObject) {
            mTmpDtsJobStep = new TransferMixedFilesStep(sourceRootFileObject, targetRootFileObject, mSteps.size() + 1,
                    mMaxTotalFileNumPerStepLimit, mMaxTotalByteSizePerStepLimit);
            mSourceRootFileObject = sourceRootFileObject;
            mTargetRootFileObject = targetRootFileObject;
        }

        public void addDataTransferUnit(final DtsDataTransferUnit dataTransferUnit) {

            // if mTmpDtsJobStep has already been initialised and (number of DataTransferUnits in the step 
            // has reached the max total number of files per step limit OR the size of the file we are going 
            // to add will exceed the max size in bytes of all the files per step limit)
            if ((mTmpDtsJobStep != null && ((mTmpDtsJobStep.getCurrentTotalFileNum() >= mMaxTotalFileNumPerStepLimit) || (mTmpDtsJobStep
                    .getCurrentTotalByteSize()
                    + dataTransferUnit.getSize() >= mMaxTotalByteSizePerStepLimit)))) {
                mSteps.add(mTmpDtsJobStep);
                mTmpDtsJobStep = new TransferMixedFilesStep(mSourceRootFileObject, mTargetRootFileObject,
                        mSteps.size() + 1, mMaxTotalFileNumPerStepLimit, mMaxTotalByteSizePerStepLimit);
                mTmpDtsJobStep.addDataTransferUnit(dataTransferUnit);
            }
            else {
                // if (!tmpDtsJobStep.isFull())
                mTmpDtsJobStep.addDataTransferUnit(dataTransferUnit);
            }
        }

        public void closeNewDataTransfer() {
            if (mTmpDtsJobStep.getDataTransferUnits().size() > 0) {
                mSteps.add(mTmpDtsJobStep);
            }
        }

        public List<DtsJobStep> getAllocatedJobSteps() {
            return mSteps;
        }

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (mMaxTotalByteSizePerStepLimit == 0) {
            mMaxTotalByteSizePerStepLimit = Long.MAX_VALUE;
        }
        else if (mMaxTotalByteSizePerStepLimit < 0) {
            throw new JobScopingException("MaxTotalByteSizePerLimit should be a positive number.");
        }
        if (mMaxTotalFileNumPerStepLimit == 0) {
            mMaxTotalFileNumPerStepLimit = Integer.MAX_VALUE;
        }
        else if (mMaxTotalFileNumPerStepLimit < 0) {
            throw new JobScopingException("MaxTotalFileNumPerStepLimit should be a positive number.");
        }

        Assert.state(mFileSystemManagerDispenser != null, "FileSystemManagerDispenser has not been set.");
    }
}
