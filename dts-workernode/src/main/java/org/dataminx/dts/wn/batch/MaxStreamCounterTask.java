package org.dataminx.dts.wn.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.dataminx.dts.DtsException;
import org.dataminx.dts.vfs.DtsVfsUtil;
import org.dataminx.dts.vfs.FileSystemManagerDispenser;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * 
 * 
 * @author Gerson Galang
 */
public class MaxStreamCounterTask implements Tasklet, InitializingBean {
	private JobDefinitionType mJobDefinition;

	private SubmitJobRequest mSubmitJobRequest;

	private int mMaxConnectionsToTry = 0;

	private int mMaxAllowedConnections = 0;

	private volatile boolean mHasConnectionErrorArised = false;

	private DtsVfsUtil mDtsVfsUtil;

	private FileSystemManagerDispenser mFileSystemManagerDispenser;

	private Map<String, FileObject> mFileObjectMap = new HashMap<String, FileObject>();

	private static final Log LOGGER = LogFactory.getLog(JobScoperImpl.class);

	private static final Log LOGGER_RC = LogFactory.getLog(RemoteConnection.class);

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		LOGGER.debug("MaxStreamCounterTask execute()");

		FileSystemManager fileSystemManager = mFileSystemManagerDispenser.getFileSystemManager();

		// TODO: have this step rerun if it fails... use the user's provided
		// info

		List<DataTransferType> dataTransfers = new ArrayList<DataTransferType>();

		final JobDescriptionType jobDescription = mSubmitJobRequest.getJobDefinition().getJobDescription();
		if (jobDescription instanceof MinxJobDescriptionType) {
			final MinxJobDescriptionType minxJobDescription = (MinxJobDescriptionType) jobDescription;
			CollectionUtils.addAll(dataTransfers, minxJobDescription.getDataTransferArray());
		}
		if (CollectionUtils.isEmpty(dataTransfers)) {
			LOGGER.warn("DTS job request is incomplete as it does not contain any data transfer elements.");
			throw new DtsJobExecutionException("DTS job request contains no data transfer elements.");
		}

		// for each DataTransferType, get the corresponding root FileObject from
		// source and target URIs and put
		// unique root FO in a map<URI in String, FileObject>
		for (DataTransferType dataTransfer : dataTransfers) {

			FileObject sourceFO = fileSystemManager.resolveFile(dataTransfer.getSource().getURI(), mDtsVfsUtil
			        .createFileSystemOptions(dataTransfer.getSource()));
			FileObject targetFO = fileSystemManager.resolveFile(dataTransfer.getTarget().getURI(), mDtsVfsUtil
			        .createFileSystemOptions(dataTransfer.getTarget()));

			// TODO: handle cases where in source and destination root File
			// Object of File System are the same but the
			// credentials to access them are different. So just means that
			// those are still two different scenarios.
			
			// TODO: what do we do then if the restriction on access/connection is on
			// a per-host rather than a per-user access

			if (mFileObjectMap.get(sourceFO.getFileSystem().getRoot().getURL().toString()) != null) {
				mFileObjectMap.put(sourceFO.getFileSystem().getRoot().getURL().toString(), sourceFO.getFileSystem()
				        .getRoot());
			}
			if (mFileObjectMap.get(targetFO.getFileSystem().getRoot().getURL().toString()) != null) {
				mFileObjectMap.put(targetFO.getFileSystem().getRoot().getURL().toString(), targetFO.getFileSystem()
				        .getRoot());
			}
		}

		// go through each FO in the map and check for max connections we can
		// make on each one and put in
		// map<URI in String, Integer of max connections>
		for (FileObject foRoot : mFileObjectMap.values()) {
			getMaxConnection(foRoot, mMaxConnectionsToTry);
			
			
			// TODO: if returned value is zero.. we might need to try again.. 3x we get that.. job fails
			
			// TODO: put the returned values to a map
		}

		// TODO: for each DataTransferType again, get the min between the source and
		// target and put in
		// map<MaxConnectionKey, Integer of max connections>
		Map<MaxConnectionKey, Integer> maxConnectionsMap = new HashMap<MaxConnectionKey, Integer>();
		for (DataTransferType dataTransfer : dataTransfers) {
			MaxConnectionKey maxConnectionKey = new MaxConnectionKey(dataTransfer.getSource().getURI(), dataTransfer
			        .getTarget().getURI());
			
			

		}
		return RepeatStatus.FINISHED;
	}

	public void setSubmitJobRequest(final SubmitJobRequest submitJobRequest) {
		mSubmitJobRequest = submitJobRequest;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.state(mSubmitJobRequest != null);
		Assert.state(mFileSystemManagerDispenser != null);
		Assert.state(mDtsVfsUtil != null);
	}

	public void setMaxConnectionsToTry(int maxConnectionsToTry) {
		mMaxConnectionsToTry = maxConnectionsToTry;
	}

	private int getMaxConnection(FileObject fileObjectRoot, int maxTry) {
		
        try {
        	// let's use the maximum parallel streams limit if it's a file
        	if (fileObjectRoot.getURL().toString().startsWith("file://")) {
    			return maxTry;
    		}
        } catch (FileSystemException e1) {
	        throw new DtsException("Error occurred while getting the file object root's URL");
        }

		int threadCounter = 1;

		while (threadCounter <= maxTry) {
			LOGGER.debug("==========================");
			LOGGER.debug("trying with " + threadCounter + " threads.");
			startRemoteConnections(fileObjectRoot, threadCounter);
			
			if (mHasConnectionErrorArised || mMaxAllowedConnections != 0) {
				break;
			}
			
			// try testing with a thread higher than the current number of threads we just tried
			threadCounter++;
		}
		return mMaxAllowedConnections;
	}

	private void setHasConnectionErrorArised(boolean hasConnectionErrorArised) {
		mHasConnectionErrorArised = hasConnectionErrorArised;
	}

	private void startRemoteConnections(FileObject fileObjectRoot, int numConnections) {

		CyclicBarrier barrier = new CyclicBarrier(numConnections);
		mHasConnectionErrorArised = false;

		ThreadPoolExecutor executor = new ThreadPoolExecutor(numConnections, numConnections, 10, TimeUnit.SECONDS,
		        new ArrayBlockingQueue<Runnable>(20));

		String fileObjectRootString = "";
        try {
	        fileObjectRootString = fileObjectRoot.getURL().toString();
        } catch (FileSystemException e1) {
	        throw new DtsException("Error occurred while getting the file object root's URL");
        }
		for (int i = 0; i < numConnections; i++) {
			RemoteConnection remoteConnection = new RemoteConnection(fileObjectRootString, 
					fileObjectRoot.getFileSystem().getFileSystemOptions(), this, "connection" + (i + 1), barrier);
			executor.execute(remoteConnection);
		}

		while (executor.getCompletedTaskCount() != numConnections) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				LOGGER.warn("InterruptedException thrown while sleeping", e);
			}
		}

		if (mHasConnectionErrorArised) {
			LOGGER.debug("Max allowed parallel connections: " + (numConnections - 1));
			mMaxAllowedConnections = numConnections - 1;
		}
		executor.shutdown();
	}

	private class RemoteConnection implements Runnable {
		private MaxStreamCounterTask mParent;
		private String mConnectionName;
		private CyclicBarrier mBarrier;
		private String mFoRootURI;
		private FileSystemOptions mOptions;

		public RemoteConnection(String foRootURI, FileSystemOptions options, MaxStreamCounterTask parent,
		        String connectionName, CyclicBarrier barrier) {
			mParent = parent;
			mConnectionName = connectionName;
			mBarrier = barrier;
			mFoRootURI = foRootURI;
			mOptions = options;
			LOGGER_RC.debug(connectionName + " created.");
		}

		public void run() {

			FileSystemManager fileSystemManager = mFileSystemManagerDispenser.getFileSystemManager();

			try {
				FileObject fileObject = fileSystemManager.resolveFile(mFoRootURI, mOptions);
				LOGGER_RC.debug(mConnectionName + " successfully connected.");

				try {
					// just hang in here for a while just in case there's a
					// delay in connection with the other threads...
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LOGGER_RC.error("InterruptedException during sleep", e);
				}

				try {
					LOGGER_RC.debug(mConnectionName + " disconnecting.");
					fileObject.close();
				} catch (FileSystemException e) {
					LOGGER_RC.error("Exception thrown during the logout process of the max " +
							"parallel connection test task.");
					e.printStackTrace();
				}
			} catch (FileSystemException e) {
				LOGGER_RC.error("Exception thrown during the login process of the max parallel connection test task.");

				mParent.setHasConnectionErrorArised(true);
			} finally {
				// having this here will help us avoid old connections not being
				// let go
				if (fileSystemManager != null) {
					mFileSystemManagerDispenser.closeFileSystemManager();
				}
			}

			try {
				mBarrier.await();
			} catch (BrokenBarrierException e) {
				return;
			} catch (InterruptedException e) {
				return;
			}
		}
	}

	public void setDtsVfsUtil(DtsVfsUtil dtsVfsUtil) {
		mDtsVfsUtil = dtsVfsUtil;
	}

	public void setFileSystemManagerDispenser(FileSystemManagerDispenser fileSystemManagerDispenser) {
		mFileSystemManagerDispenser = fileSystemManagerDispenser;
	}

}
