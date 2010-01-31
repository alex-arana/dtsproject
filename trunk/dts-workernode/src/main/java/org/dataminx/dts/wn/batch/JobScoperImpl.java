package org.dataminx.dts.wn.batch;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.dataminx.dts.vfs.DtsFileSystemManager;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.CreationFlagEnumeration;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.springframework.util.Assert;


public class JobScoperImpl implements JobScoper {
	
	private boolean mCancelled = false;
	private String mFailureMessage = "";

	private int mTotalSize = 0;
    private ArrayList<String> mExcluded = new ArrayList<String>();
    
    //@Autowired
    private DtsFileSystemManager mFileSystemManager;
    	
	private static final Log LOGGER = LogFactory.getLog(JobScoperImpl.class);
	
	private DtsJobStepAllocator mDtsJobStepAllocator;
	
	public static final int BATCH_SIZE_LIMIT = 2;
		
	public void setFileSystemManager(DtsFileSystemManager fileSystemManager) {
		
		// jobScoper only needs access to a fileSystemManager that has to be handed to it by its caller.
		// a DtsFileSystemManagerDispenser is only needed if the scoping task will be run more than one thread.
		mFileSystemManager = fileSystemManager;
	}
	
	public DtsJobDetails scopeTheJob(JobDefinitionType jobDefinition) {
		
        Assert.notNull(jobDefinition);
                
        DtsJobDetails jobDetails = new DtsJobDetails();
        jobDetails.setJobDefinition(jobDefinition);
        jobDetails.setJobId(jobDefinition.getId());        
        
        mDtsJobStepAllocator = new DtsJobStepAllocator();
        mExcluded = new ArrayList<String>();
        mTotalSize = 0;
        
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
                
        for (DataTransferType dataTransfer : dataTransfers) {        	
        	mDtsJobStepAllocator.initNewDataTransfer();
        	
        	prepare(mFileSystemManager.resolveFile(dataTransfer.getSource()), 
        			mFileSystemManager.resolveFile(dataTransfer.getTarget()), 
        			dataTransfer);
        	
        	mDtsJobStepAllocator.closeNewDataTransfer();
        	
        }
        LOGGER.debug("total size of files to be transferred: " + mTotalSize + " bytes");
        LOGGER.debug("list of excluded files: ");
        for (String excluded : mExcluded) {
        	LOGGER.debug(" - " + excluded);
        }
        
        jobDetails.setExcludedFiles(mExcluded);
        jobDetails.setTotalBytes(mTotalSize);
        jobDetails.setJobSteps(mDtsJobStepAllocator.getAllocatedJobSteps());
        
        //mDtsJobStepAllocator.printDebugStepContents();
        //let's try to put the steps in the step execution context
        
        return jobDetails;
	}
	
	private void prepare(FileObject sourceParent, FileObject destinationParent, DataTransferType dataTransfer) 
			throws DtsJobCancelledException {
        if (mCancelled)
            throw new DtsJobCancelledException();

        try {
        	// Handle the following cases...
			//   source: /tmp/passwd			
			//		 copy to:	
			//	 destination that does not exists: /tmp/passwd
			//	 destination directory that does not exists: /tmp/hello/
			//			
			//	 destination file that exists: /tmp/passwd
			//			
			//	 destination directory that exists: /tmp        
        	        	
        	if (sourceParent.getType().equals(FileType.FILE) && !mCancelled) {
        		
        		CreationFlagEnumeration.Enum creationFlag = dataTransfer.getTransferRequirements().getCreationFlag();
        		
        		if (!destinationParent.exists()) {
        			// Note that we are not supporting a single file transfer to a non-existent directory
        			// Any destinationParent which had a "/" at the end of it's URI will not be handled
        			
        			// TODO: should we handle the above case?
        			
        			addFilesToTransfer(sourceParent, destinationParent, dataTransfer);
        			
        		} else if (destinationParent.exists() && destinationParent.getType().equals(FileType.FILE)) {
        			// File to File
        			
        			if (creationFlag.equals(CreationFlagEnumeration.OVERWRITE)) {
        				addFilesToTransfer(sourceParent, destinationParent, dataTransfer);
                    } else {
                        mExcluded.add(sourceParent.getName().getFriendlyURI());
                    }
        		} else {
	        		// ... File to Dir
	        		
	                //create the new object
	                String newFilePath = destinationParent.getURL() + 
	                	FileName.SEPARATOR + sourceParent.getName().getBaseName();
	                FileObject destinationChild = destinationParent.getFileSystem()
	                	.getFileSystemManager().resolveFile(newFilePath, destinationParent
	                			.getFileSystem().getFileSystemOptions());
	                //destinationChild.createFile();
	                
	                if (destinationChild.exists()) {
	                	LOGGER.debug("creationFlag: " + creationFlag);
	                	if (creationFlag.equals(CreationFlagEnumeration.OVERWRITE)) {
	                		addFilesToTransfer(sourceParent, destinationChild, dataTransfer);
	                    } else {
	                        mExcluded.add(sourceParent.getName().getFriendlyURI());
	                    }
	                    
	                } else {
	                	addFilesToTransfer(sourceParent, destinationChild, dataTransfer);
	                }
        		}
        		

            } else if (sourceParent.getType().equals(FileType.FOLDER) && !mCancelled) {
            	// .. Dir to Dir
            	
                //create the new object
                String newFolderPath = destinationParent.getURL() + FileName.SEPARATOR + sourceParent.getName().getBaseName();
                FileObject destinationChild = destinationParent.getFileSystem().getFileSystemManager().resolveFile(newFolderPath, destinationParent.getFileSystem().getFileSystemOptions());

                if(!destinationChild.exists())
                    destinationChild.createFolder();

                //get the children
                FileObject[] sourceChildren = sourceParent.getChildren();

                //iterate through the children
                for (FileObject sourceChild : sourceChildren) {
                    //recurse into the directory, or copy the file
                    prepare(sourceChild, destinationChild, dataTransfer);
                }
            }
        } catch (DtsJobCancelledException e) {
            throw e;
        } catch (Exception e) {
            handleError(e);
        }
	}
	
	private void addFilesToTransfer(FileObject source, 
			FileObject destination, DataTransferType dataTransfer) throws FileSystemException {
		LOGGER.debug("addFilesToTransfer(\"" + source.getURL() + "\", \"" + destination.getURL() + "\", dataTransfer)");
		mTotalSize += source.getContent().getSize();
		mDtsJobStepAllocator.addDataTransferUnit(
				new DtsDataTransferUnit(source.getURL().toString(), destination.getURL().toString(), dataTransfer));
	}
	
	private void handleError(Exception e) {
        mFailureMessage = e.getMessage();
        LOGGER.error(e);
        
        //invokeTransferFailedListeners();
    }
	
	private class DtsJobStepAllocator {
		private List<DtsJobStep> mSteps;
		private DtsJobStep mTmpDtsJobStep = null;
		
		public DtsJobStepAllocator() {
			mSteps = new ArrayList<DtsJobStep>();
		}
		
		/**
		 * This method  needs to be called before a new DataTransferType instance gets processed by 
		 * {@link org.dataminx.dts.wn.batch.JobScoper#prepare()}. This will make sure that new 
		 * DataTransferUnits get added to a new DtsJobStep.
		 */
		public void initNewDataTransfer() {
			mTmpDtsJobStep = new DtsJobStep(mSteps.size() + 1, BATCH_SIZE_LIMIT);
			
		}
		
		public void addDataTransferUnit(DtsDataTransferUnit dataTransferUnit) {
			if ((mTmpDtsJobStep != null && mTmpDtsJobStep.isFull())) {
				mSteps.add(mTmpDtsJobStep);
				mTmpDtsJobStep = new DtsJobStep(mSteps.size() + 1, BATCH_SIZE_LIMIT);
				mTmpDtsJobStep.addDataTransferUnit(dataTransferUnit);
				
			} else {
				//if (!tmpDtsJobStep.isFull())
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

}