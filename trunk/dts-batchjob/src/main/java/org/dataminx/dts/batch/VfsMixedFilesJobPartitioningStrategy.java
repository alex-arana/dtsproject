/**
 * Copyright (c) 2010, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dataminx.dts.DtsException;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.dataminx.dts.security.crypto.DummyEncrypter;
import org.dataminx.dts.security.crypto.Encrypter;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.CreationFlagEnumeration;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * The MixedFilesJobPartitioningStrategy is the a partitioning strategy which employs the mixing and matching of
 * big and small files into a job step as long as they stay within the limits of max allowed files to transfer and
 * max allowed size of all the files to transfer per step.
 *
 * @author Gerson Galang
 */
public class VfsMixedFilesJobPartitioningStrategy implements
    JobPartitioningStrategy, InitializingBean {

    /**
     * The VfsMixedFilesJobPartitioningStrategy's DtsJobStepAllocator.
     */
    private class MixedFilesJobStepAllocator implements DtsJobStepAllocator {

        /** The list of DtsJobSteps where DtsTransferUnits will be allocated. */
        private final List<DtsJobStep> mSteps;

        /** A reference to the DtsJobStep. */
        private DtsJobStep mTmpDtsJobStep;

        /** The Source Root FileObject URI string. */
        private String mSourceRootFileObject;

        /** The Target Root FileObject URI string. */
        private String mTargetRootFileObject;

        /**
         * MixedFilesJobStepAllocator's constructor.
         */
        public MixedFilesJobStepAllocator() {
            mSteps = new ArrayList<DtsJobStep>();
        }

        /**
         * {@inheritDoc}
         */
        public void addDataTransferUnit(
            final DtsDataTransferUnit dataTransferUnit) {

            // if mTmpDtsJobStep has already been initialised and (number of DataTransferUnits in the step
            // has reached the max total number of files per step limit OR the size of the file we are going
            // to add will exceed the max size in bytes of all the files per step limit)
            if (mTmpDtsJobStep != null
                && (mTmpDtsJobStep.getCurrentTotalFileNum() >= mMaxTotalFileNumPerStepLimit || mTmpDtsJobStep
                    .getCurrentTotalByteSize()
                    + dataTransferUnit.getSize() >= mMaxTotalByteSizePerStepLimit)) {
                mSteps.add(mTmpDtsJobStep);
                mTmpDtsJobStep = new TransferMixedFilesStep(
                    mSourceRootFileObject, mTargetRootFileObject,
                    mSteps.size() + 1, mMaxTotalFileNumPerStepLimit,
                    mMaxTotalByteSizePerStepLimit);
                mTmpDtsJobStep.addDataTransferUnit(dataTransferUnit);
            }
            else {
                // if (!tmpDtsJobStep.isFull())
                mTmpDtsJobStep.addDataTransferUnit(dataTransferUnit);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void closeNewDataTransfer() {
            if (mTmpDtsJobStep.getDataTransferUnits().size() > 0) {
                mSteps.add(mTmpDtsJobStep);
            }
        }

        /**
         * {@inheritDoc}
         */
        public void createNewDataTransfer(final String sourceRootFileObject,
            final String targetRootFileObject) {
            mTmpDtsJobStep = new TransferMixedFilesStep(sourceRootFileObject,
                targetRootFileObject, mSteps.size() + 1,
                mMaxTotalFileNumPerStepLimit, mMaxTotalByteSizePerStepLimit);
            mSourceRootFileObject = sourceRootFileObject;
            mTargetRootFileObject = targetRootFileObject;
        }

        /**
         * {@inheritDoc}
         */
        public List<DtsJobStep> getAllocatedJobSteps() {
            return mSteps;
        }

    }

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(VfsMixedFilesJobPartitioningStrategy.class);

    /** A flag if the user has requested the processing of the job to be stopped or cancelled. */
    private final boolean mCancelled = false;

    /** The total size of all the files that will be transferred by this job. */
    private long mTotalSize;

    /** The total number of files to be transferred by this job. */
    private int mTotalFiles;

    /** The total number of files to be transferred by a Data Transfer element (ie Source-Target pair). */
    private int mPerDataTransferTotalFiles;

    /** The maximum byte size limit of all the files that can be transferred by the step. */
    private long mMaxTotalByteSizePerStepLimit;

    /** The maximum number of files that can be transferred by the step. */
    private int mMaxTotalFileNumPerStepLimit;

    /** A reference to the Encrypter. */
    private Encrypter mEncrypter;

    /** The list of files excluded from the transfer. Some of these files might unreadable files. */
    private ArrayList<String> mExcluded = new ArrayList<String>();

    /** A reference to the DtsVfsUtil. */
    private DtsVfsUtil mDtsVfsUtil;

    /** A reference to the DtsJobStepAllocator. */
    private DtsJobStepAllocator mDtsJobStepAllocator;

    /**
     * Add the given source to the list of files to transfer.
     *
     * @param source the source which normally is represents a single file
     * @param destination the target
     * @param dataTransferIndex the DataTransfer index on how this source/destination's is listed in the
     *        JobDefinitionDocument
     * @throws FileSystemException on failure to connect to either the source or destination
     */
    private void addFilesToTransfer(final FileObject source,
        final FileObject destination, final int dataTransferIndex)
        throws FileSystemException {
        LOGGER.debug("addFilesToTransfer(\"" + source.getURL() + "\", \""
            + destination.getURL() + "\", " + dataTransferIndex + ")");
        mTotalSize += source.getContent().getSize();
        mTotalFiles++;
        mPerDataTransferTotalFiles++;
        mDtsJobStepAllocator.addDataTransferUnit(new DtsDataTransferUnit(source
            .getURL().toString(), destination.getURL().toString(),
            dataTransferIndex, source.getContent().getSize()));
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        if (mEncrypter == null) {
            mEncrypter = new DummyEncrypter();
        }
        if (mMaxTotalByteSizePerStepLimit == 0) {
            mMaxTotalByteSizePerStepLimit = Long.MAX_VALUE;
        }
        else if (mMaxTotalByteSizePerStepLimit < 0) {
            throw new JobScopingException(
                "MaxTotalByteSizePerLimit should be a positive number.");
        }
        if (mMaxTotalFileNumPerStepLimit == 0) {
            mMaxTotalFileNumPerStepLimit = Integer.MAX_VALUE;
        }
        else if (mMaxTotalFileNumPerStepLimit < 0) {
            throw new JobScopingException(
                "MaxTotalFileNumPerStepLimit should be a positive number.");
        }
    }

    /**
     * {@inheritDoc}
     */
    public DtsJobDetails partitionTheJob(final JobDefinitionType jobDefinition,
        final String jobResourceKey) throws JobScopingException {
        Assert.hasText(jobResourceKey,
            "JobResourceKey should not be null or empty.");
        Assert.notNull(jobDefinition, "JobDefinitionType should not be null.");
        if (mMaxTotalByteSizePerStepLimit < 0) {
            throw new DtsException(
                "MaxTotalByteSizePerLimit should be a positive number.");
        }
        if (mMaxTotalFileNumPerStepLimit < 0) {
            throw new DtsException(
                "MaxTotalFileNumPerStepLimit should be a positive number.");
        }

        FileSystemManager fileSystemManager = null;
        try {
            fileSystemManager = mDtsVfsUtil.createNewFsManager();
        }
        catch (final FileSystemException e) {
            throw new JobScopingException(
                "FileSystemException was thrown while creating new FileSystemManager in the job scoping task.",
                e);
        }

        final DtsJobDetails jobDetails = new DtsJobDetails();
        jobDetails.setJobDefinition(jobDefinition);
        jobDetails.setJobId(jobResourceKey);

        final Map<String, Integer> sourceTargetMaxTotalFilesToTransfer = jobDetails
            .getSourceTargetMaxTotalFilesToTransfer();

        mDtsJobStepAllocator = new MixedFilesJobStepAllocator();
        mExcluded = new ArrayList<String>();
        mTotalSize = 0;
        mTotalFiles = 0;

        final List<DataTransferType> dataTransfers = new ArrayList<DataTransferType>();

        final JobDescriptionType jobDescription = jobDefinition
            .getJobDescription();
        if (jobDescription instanceof MinxJobDescriptionType) {
            final MinxJobDescriptionType minxJobDescription = (MinxJobDescriptionType) jobDescription;
            CollectionUtils.addAll(dataTransfers, minxJobDescription
                .getDataTransferArray());
        }
        if (CollectionUtils.isEmpty(dataTransfers)) {
            LOGGER
                .warn("DTS job request is incomplete as it does not contain any data transfer elements.");
            throw new DtsJobExecutionException(
                "DTS job request contains no data transfer elements.");
        }
        int dataTransferIndex = 0;
        for (final DataTransferType dataTransfer : dataTransfers) {

            // reset the total number of files to be transferred within this DataTransfer element
            mPerDataTransferTotalFiles = 0;

            FileObject sourceParent = null;

            try {
                sourceParent = fileSystemManager.resolveFile(dataTransfer
                    .getSource().getURI(), mDtsVfsUtil.getFileSystemOptions(
                    dataTransfer.getSource(), mEncrypter));

                if (!sourceParent.getContent().getFile().exists()
                    || !sourceParent.getContent().getFile().isReadable()) {
                    throw new JobScopingException("The source " + sourceParent
                        + " provided does not exist or is not readable.");
                }
            }
            catch (final FileSystemException e) {
                throw new JobScopingException(
                    "FileSystemException was thrown while accessing the remote file "
                        + dataTransfer.getSource().getURI() + ".", e);
            }

            FileObject targetParent = null;
            try {
                targetParent = fileSystemManager.resolveFile(dataTransfer
                    .getTarget().getURI(), mDtsVfsUtil.getFileSystemOptions(
                    dataTransfer.getTarget(), mEncrypter));
            }
            catch (final FileSystemException e) {
                throw new JobScopingException(
                    "FileSystemException was thrown while accessing the remote file "
                        + dataTransfer.getTarget().getURI() + ".", e);
            }

            try {
                mDtsJobStepAllocator.createNewDataTransfer(sourceParent
                    .getFileSystem().getRoot().getURL().toString(),
                    targetParent.getFileSystem().getRoot().getURL().toString());

                final CreationFlagEnumeration.Enum creationFlag = dataTransfer
                    .getTransferRequirements().getCreationFlag();

                prepare(sourceParent, targetParent, dataTransferIndex,
                    creationFlag);

                final String sourceParentRootStr = sourceParent.getFileSystem()
                    .getRoot().getURL().toString();
                final String targetParentRootStr = targetParent.getFileSystem()
                    .getRoot().getURL().toString();

                if (sourceTargetMaxTotalFilesToTransfer
                    .containsKey(sourceParentRootStr)
                    && sourceTargetMaxTotalFilesToTransfer
                        .containsKey(targetParentRootStr)) {
                    updateSourceTargetMaxTotalFilesToTransfer(
                        sourceTargetMaxTotalFilesToTransfer,
                        sourceParentRootStr, mPerDataTransferTotalFiles);
                    updateSourceTargetMaxTotalFilesToTransfer(
                        sourceTargetMaxTotalFilesToTransfer,
                        targetParentRootStr, mPerDataTransferTotalFiles);
                }
                else if (sourceTargetMaxTotalFilesToTransfer
                    .containsKey(sourceParentRootStr)
                    && !sourceTargetMaxTotalFilesToTransfer
                        .containsKey(targetParentRootStr)) {
                    updateSourceTargetMaxTotalFilesToTransfer(
                        sourceTargetMaxTotalFilesToTransfer,
                        sourceParentRootStr, mPerDataTransferTotalFiles);
                    sourceTargetMaxTotalFilesToTransfer.put(
                        targetParentRootStr, mPerDataTransferTotalFiles);
                }
                else if (!sourceTargetMaxTotalFilesToTransfer
                    .containsKey(sourceParentRootStr)
                    && sourceTargetMaxTotalFilesToTransfer
                        .containsKey(targetParentRootStr)) {
                    sourceTargetMaxTotalFilesToTransfer.put(
                        sourceParentRootStr, mPerDataTransferTotalFiles);
                    updateSourceTargetMaxTotalFilesToTransfer(
                        sourceTargetMaxTotalFilesToTransfer,
                        targetParentRootStr, mPerDataTransferTotalFiles);
                }
                else {
                    sourceTargetMaxTotalFilesToTransfer.put(
                        sourceParentRootStr, mPerDataTransferTotalFiles);
                    sourceTargetMaxTotalFilesToTransfer.put(
                        targetParentRootStr, mPerDataTransferTotalFiles);
                }

                dataTransferIndex++;
            }
            catch (final DtsJobCancelledException e) {
                // TODO: handle DTS Job Cancel event
                LOGGER.debug("Job has been cancelled.");
            }
            catch (final FileSystemException e) {
                throw new JobScopingException(
                    "FileSystemException was thrown while accessing the remote files in the job scoping task.",
                    e);
            }
            catch (final JobScopingException e) {
                // TODO Auto-generated catch block
                throw e;
            }

            mDtsJobStepAllocator.closeNewDataTransfer();

        }
        LOGGER.info("Total number of files to be transferred: " + mTotalFiles);
        LOGGER.info("Total size of files to be transferred: " + mTotalSize
            + " bytes");
        LOGGER.debug("list of excluded files: ");
        for (final String excluded : mExcluded) {
            LOGGER.debug(" - " + excluded);
        }

        jobDetails.setExcludedFiles(mExcluded);
        jobDetails.setTotalBytes(mTotalSize);
        jobDetails.setTotalFiles(mTotalFiles);
        jobDetails.saveJobSteps(mDtsJobStepAllocator.getAllocatedJobSteps());

        for (final DtsJobStep jobStep : mDtsJobStepAllocator
            .getAllocatedJobSteps()) {
            LOGGER.debug(jobStep);
        }
        // let's try to put the steps in the step execution context

        // immediately close the file system manager so FileCopyTask will be able to use all of the
        // available connections
        ((DefaultFileSystemManager) fileSystemManager).close();

        return jobDetails;
    }

    /**
     * Prepares the remote destination for the files that will be transferred to it by pre-generating the folders where
     * files from the source will be copied. This method will also scope the source FileObject.
     *
     * @param sourceParent the source FileObject
     * @param destinationParent the destination FileObject
     * @param dataTransferIndex the DataTransfer index on how this source/destination's is listed in the
     *        JobDefinitionDocument
     * @param creationFlag a flag to say if the file on the destination should be overwritten or not
     * @throws DtsJobCancelledException if the job has been cancelled
     * @throws JobScopingException if any error arised while the job was being scoped
     */
    private void prepare(final FileObject sourceParent,
        final FileObject destinationParent, final int dataTransferIndex,
        final CreationFlagEnumeration.Enum creationFlag)
        throws DtsJobCancelledException, JobScopingException {
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

            if (!sourceParent.getContent().getFile().isReadable()
                && !mCancelled) {
                mExcluded.add(sourceParent.getName().getFriendlyURI());
            }
            else if (sourceParent.getType().equals(FileType.FILE)
                && !mCancelled) {

                // check and see if the size of the file has exceeded the max size of files to be transferred
                // by a step
                if (sourceParent.getContent().getSize() > mMaxTotalByteSizePerStepLimit) {
                    throw new JobScopingException("file: "
                        + sourceParent.getName()
                        + " too big. Try transferring files smaller than "
                        + mMaxTotalByteSizePerStepLimit + " bytes.");
                }

                if (!destinationParent.exists()) {
                    // Note that we are not supporting a single file transfer to
                    // a non-existent directory
                    // Any destinationParent which had a "/" at the end of it's
                    // URI will not be handled

                    // TODO: should we handle the above case?

                    addFilesToTransfer(sourceParent, destinationParent,
                        dataTransferIndex);

                }
                else if (destinationParent.exists()
                    && destinationParent.getType().equals(FileType.FILE)) {
                    // File to File

                    if (creationFlag.equals(CreationFlagEnumeration.OVERWRITE)) {
                        addFilesToTransfer(sourceParent, destinationParent,
                            dataTransferIndex);
                    }
                    else {
                        mExcluded.add(sourceParent.getName().getFriendlyURI());
                    }
                }
                else {
                    // ... File to Dir

                    // create the new object
                    final String newFilePath = destinationParent.getURL()
                        + FileName.SEPARATOR
                        + sourceParent.getName().getBaseName();
                    final FileObject destinationChild = destinationParent
                        .getFileSystem().getFileSystemManager().resolveFile(
                            newFilePath,
                            destinationParent.getFileSystem()
                                .getFileSystemOptions());
                    // destinationChild.createFile();

                    if (destinationChild.exists()) {
                        if (creationFlag
                            .equals(CreationFlagEnumeration.OVERWRITE)) {
                            addFilesToTransfer(sourceParent, destinationChild,
                                dataTransferIndex);
                        }
                        else {
                            mExcluded.add(sourceParent.getName()
                                .getFriendlyURI());
                        }

                    }
                    else {
                        addFilesToTransfer(sourceParent, destinationChild,
                            dataTransferIndex);
                    }
                }
            }
            else if (sourceParent.getType().equals(FileType.FOLDER)
                && !mCancelled) {
                // .. Dir to Dir

                // create the new object
                final String newFolderPath = destinationParent.getURL()
                    + FileName.SEPARATOR + sourceParent.getName().getBaseName();
                final FileObject destinationChild = destinationParent
                    .getFileSystem().getFileSystemManager().resolveFile(
                        newFolderPath,
                        destinationParent.getFileSystem()
                            .getFileSystemOptions());

                if (!destinationChild.exists()) {
                    destinationChild.createFolder();
                }

                // get the children
                final FileObject[] sourceChildren = sourceParent.getChildren();

                // iterate through the children
                for (final FileObject sourceChild : sourceChildren) {
                    // recurse into the directory, or copy the file
                    prepare(sourceChild, destinationChild, dataTransferIndex,
                        creationFlag);
                }
            }
        }
        catch (final DtsJobCancelledException e) {
            throw e;
        }
        catch (final JobScopingException e) {
            throw e;
        }
        catch (final FileSystemException e) {
            throw new DtsException(e);
        }
    }

    /**
     * Sets the Encrypter.
     *
     * @param encrypter the Encrypter
     */
    public void setEncrypter(final Encrypter encrypter) {
        mEncrypter = encrypter;
    }

    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

    public void setMaxTotalByteSizePerStepLimit(
        final long maxTotalByteSizePerStepLimit) {
        mMaxTotalByteSizePerStepLimit = maxTotalByteSizePerStepLimit;
    }

    public void setMaxTotalFileNumPerStepLimit(
        final int maxTotalFileNumPerStepLimit) {
        mMaxTotalFileNumPerStepLimit = maxTotalFileNumPerStepLimit;
    }

    /**
     * This method is used to get the optimum number of connections that should be cached by the MaxStreamCounterTask
     * step on the FileSystemManagerCache. There's no point caching 5 connections so we could do 5 concurrent
     * connections when there's actually only 2 files to be transferred for the given source/target FileObject.
     *
     * @param sourceTargetMaxTotalFilesToTransfer a map that holds the maximum number of files that will be transferred
     *        for the given source/target Root FileObject URI string
     * @param parentRootStr the source or target Root FileObject URI string
     * @param perDataTransferTotalFiles the total number of files to be transferred for the given DataTransferElement
     */
    private void updateSourceTargetMaxTotalFilesToTransfer(
        final Map<String, Integer> sourceTargetMaxTotalFilesToTransfer,
        final String parentRootStr, final int perDataTransferTotalFiles) {
        if (sourceTargetMaxTotalFilesToTransfer.get(parentRootStr) < perDataTransferTotalFiles) {
            sourceTargetMaxTotalFilesToTransfer.put(parentRootStr,
                perDataTransferTotalFiles);
        }
    }
}
