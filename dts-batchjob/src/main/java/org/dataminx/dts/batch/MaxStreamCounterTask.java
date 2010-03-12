package org.dataminx.dts.batch;

import org.dataminx.dts.common.batch.util.FileObjectMap;

import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.dataminx.dts.common.vfs.FileSystemManagerCache;
import org.dataminx.dts.common.vfs.FileSystemManagerCacheAlreadyInitializedException;
import org.dataminx.dts.common.vfs.FileSystemManagerDispenser;

import java.util.ArrayList;
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
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * The <code>MaxStreamCounterTask</code> is
 * {@link org.springframework.batch.core.step.tasklet.Tasklet} that checks the
 * maximum number of connections the job can have to the sources and sinks it is
 * going to connect to while processing the data transfer job. This Tasklet will
 * also cache the connections (ie {@link FileSystemManager}s to a
 * {@link FileSystemManagerCache} that the {@link FileCopyTask} steps can share.
 * 
 * @author Gerson Galang
 */
public class MaxStreamCounterTask implements Tasklet, InitializingBean {

    private SubmitJobRequest mSubmitJobRequest;

    private int mMaxConnectionsToTry = 0;

    private volatile boolean mHasConnectionErrorArised = false;

    private DtsVfsUtil mDtsVfsUtil;

    private FileSystemManagerDispenser mFileSystemManagerDispenser;

    private JobRepository mJobRepository;

    private FileSystemManagerCache mFileSystemManagerCache;

    private final Map<String, FileObject> mFileObjectMap = new FileObjectMap<String, FileObject>();

    private final Map<String, List<FileSystemManager>> mWorkingConnectionsListPerRootFileObject = new FileObjectMap<String, List<FileSystemManager>>();

    private static final Log LOGGER = LogFactory.getLog(MaxStreamCounterTask.class);

    private static final Log LOGGER_RC = LogFactory.getLog(RemoteConnection.class);

    private volatile boolean isLastTry = false;

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        LOGGER.debug("MaxStreamCounterTask execute()");

        final FileSystemManager fileSystemManager = mFileSystemManagerDispenser.getFileSystemManager();

        // TODO: have this step rerun if it fails... use the user's provided
        // info

        final List<DataTransferType> dataTransfers = new ArrayList<DataTransferType>();

        final JobDescriptionType jobDescription = mSubmitJobRequest.getJobDefinition().getJobDescription();
        if (jobDescription instanceof MinxJobDescriptionType) {
            final MinxJobDescriptionType minxJobDescription = (MinxJobDescriptionType) jobDescription;
            CollectionUtils.addAll(dataTransfers, minxJobDescription.getDataTransferArray());
        }
        if (CollectionUtils.isEmpty(dataTransfers)) {
            LOGGER.warn("DTS job request is incomplete as it does not contain any data transfer elements.");
            throw new DtsJobExecutionException("DTS job request contains no data transfer elements.");
        }

        // for each DataTransferType, get the corresponding root FileObject from source
        // and target URIs and put unique root FO in a map<URI in String, FileObject>
        for (final DataTransferType dataTransfer : dataTransfers) {

            final FileObject sourceFO = fileSystemManager.resolveFile(dataTransfer.getSource().getURI(), mDtsVfsUtil
                    .createFileSystemOptions(dataTransfer.getSource()));
            final FileObject targetFO = fileSystemManager.resolveFile(dataTransfer.getTarget().getURI(), mDtsVfsUtil
                    .createFileSystemOptions(dataTransfer.getTarget()));

            // TODO: handle cases where in source and destination root File
            // Object of File System are the same but the credentials to 
            // access them are different. So just means that those are still
            // two different scenarios.

            // TODO: what do we do then if the restriction on access/connection
            // is on a per-host rather than a per-user access

            if (!mFileObjectMap.containsKey(sourceFO.getFileSystem().getRoot().getURL().toString())) {
                mFileObjectMap.put(sourceFO.getFileSystem().getRoot().getURL().toString(), sourceFO.getFileSystem()
                        .getRoot());
            }
            if (!mFileObjectMap.containsKey(targetFO.getFileSystem().getRoot().getURL().toString())) {
                mFileObjectMap.put(targetFO.getFileSystem().getRoot().getURL().toString(), targetFO.getFileSystem()
                        .getRoot());
            }
        }

        // TODO:
        // we'll force the closing of the main thread's connection here.. not sure if
        // it will still be used by other tasks/steps being executed by the main thread
        // later on. we'll see..
        mFileSystemManagerDispenser.closeFileSystemManager();

        // go through each FO in the map and check for max connections we can
        // make on each one and put in map<URI in String, Integer of max connections>
        for (final FileObject foRoot : mFileObjectMap.values()) {
            gatherMaxConnections(foRoot, mMaxConnectionsToTry);
        }

        try {
            // TODO: remove this later on... or change FileSystemManagerCache implementation
            mFileSystemManagerCache.initFileSystemManagerCache(mWorkingConnectionsListPerRootFileObject);
        } catch (final FileSystemManagerCacheAlreadyInitializedException e) {
            LOGGER.error("Initialisation of FileSystemManagerCache failed because it has not been cleared yet.", e);
            throw e;
        }
        return RepeatStatus.FINISHED;
    }

    public void setSubmitJobRequest(final SubmitJobRequest submitJobRequest) {
        mSubmitJobRequest = submitJobRequest;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(mSubmitJobRequest != null, "Unable to find DTS Job Request in execution context.");
        Assert.state(mFileSystemManagerDispenser != null, "FileSystemManagerDispenser has not been set.");
        Assert.state(mDtsVfsUtil != null, "DtsVfsUtil has not been set.");
        Assert.state(mMaxConnectionsToTry != 0, "MaxConnectionsToTry has not been set.");
        Assert.state(mJobRepository != null, "JobRepository has not been set.");
    }

    public void setMaxConnectionsToTry(final int maxConnectionsToTry) {
        mMaxConnectionsToTry = maxConnectionsToTry;
    }

    private void gatherMaxConnections(final FileObject fileObjectRoot, final int maxTry) throws FileSystemException {
        isLastTry = false;

        int threadCounter = 1;

        try {
            // let's use the maximum parallel streams limit if it's a file
            if (fileObjectRoot.getURL().toString().startsWith("file://")
                    || fileObjectRoot.getURL().toString().startsWith("tmp://")) {
                threadCounter = maxTry;
            }
        } catch (final FileSystemException e1) {
            throw new DtsException("Error occurred while getting the file object root's URL");
        }
        List<FileSystemManager> workingConnections = null;
        while (threadCounter <= maxTry) {
            if (threadCounter == maxTry) {
                isLastTry = true;
            }
            LOGGER.debug("==========================");
            LOGGER.debug("Trying with " + threadCounter + " threads.");
            workingConnections = startRemoteConnections(fileObjectRoot, threadCounter);

            if (mHasConnectionErrorArised) {
                break;
            }

            if (LOGGER.isDebugEnabled() && threadCounter != maxTry) {
                Assert.isTrue(workingConnections.isEmpty(), "WorkingConnections list should be empty.");
            }

            // try testing with a thread higher than the current number of
            // threads we just tried
            threadCounter++;
        }

        if (LOGGER.isDebugEnabled() && threadCounter == maxTry) {
            Assert.isTrue(maxTry != workingConnections.size(),
                    "MaxAllowedConnections should have the same number of connections in the workingConnections list.");
        }
        try {
            mWorkingConnectionsListPerRootFileObject.put(fileObjectRoot.getURL().toString(), workingConnections);
        } catch (final FileSystemException e) {
            throw e;
        }

        LOGGER.debug("Max allowed concurrent connections for FileObject root \"" + fileObjectRoot.getURL().toString()
                + "\" " + workingConnections.size());
    }

    public boolean isLastTry() {
        return isLastTry;
    }

    private void setHasConnectionErrorArised(final boolean hasConnectionErrorArised) {
        mHasConnectionErrorArised = hasConnectionErrorArised;
    }

    private boolean getHasConnectionErrorArised() {
        return mHasConnectionErrorArised;
    }

    private List<FileSystemManager> startRemoteConnections(final FileObject fileObjectRoot, final int numConnections) {

        final List<FileSystemManager> workingConnectionsList = new ArrayList<FileSystemManager>();

        final CyclicBarrier barrier = new CyclicBarrier(numConnections);
        mHasConnectionErrorArised = false;

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(numConnections, numConnections, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));

        String fileObjectRootString = "";
        try {
            fileObjectRootString = fileObjectRoot.getURL().toString();
        } catch (final FileSystemException e1) {
            throw new DtsException("Error occurred while getting the file object root's URL");
        }
        for (int i = 0; i < numConnections; i++) {
            final RemoteConnection remoteConnection = new RemoteConnection(fileObjectRootString, fileObjectRoot
                    .getFileSystem().getFileSystemOptions(), this, "ConnectionThread" + (i + 1), barrier,
                    workingConnectionsList);
            executor.execute(remoteConnection);
        }

        while (executor.getCompletedTaskCount() != numConnections) {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                LOGGER.warn("InterruptedException thrown while sleeping", e);
            }
        }

        executor.shutdown();

        // TODO: probably need to remove mMaxAllowedConnections if workignConnectionsList can provide the same info
        return workingConnectionsList;
    }

    private class RemoteConnection implements Runnable {
        private final MaxStreamCounterTask mParent;
        private final String mConnectionName;
        private final CyclicBarrier mBarrier;
        private final String mFoRootURI;
        private final FileSystemOptions mOptions;
        private final List<FileSystemManager> mWorkingConnectionsList;
        private boolean successfulConnection = true;

        public RemoteConnection(final String foRootURI, final FileSystemOptions options,
                final MaxStreamCounterTask parent, final String connectionName, final CyclicBarrier barrier,
                final List<FileSystemManager> workingConnectionsList) {
            mParent = parent;
            mConnectionName = connectionName;
            mBarrier = barrier;
            mFoRootURI = foRootURI;
            mOptions = options;
            mWorkingConnectionsList = workingConnectionsList;
            LOGGER_RC.debug(connectionName + " created.");
        }

        public void run() {

            final FileSystemManager fileSystemManager = mFileSystemManagerDispenser.getFileSystemManager();

            try {
                final FileObject fileObject = fileSystemManager.resolveFile(mFoRootURI, mOptions);
                LOGGER_RC.debug(mConnectionName + " successfully connected.");

                try {
                    // just hang in here for a while just in case there's a
                    // delay in connection with the other threads...
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    LOGGER_RC.error("InterruptedException during sleep", e);
                }

                try {
                    LOGGER_RC.debug(mConnectionName + " disconnecting.");
                    fileObject.close();
                } catch (final FileSystemException e) {
                    LOGGER_RC.warn("FileSystemException thrown during the logout process of the max "
                            + "parallel connection test task.");

                }
            } catch (final FileSystemException e) {
                LOGGER_RC
                        .error("FileSystemException thrown during the login process of the max parallel connection test task.");

                mParent.setHasConnectionErrorArised(true);
                successfulConnection = false;
            }

            try {
                mBarrier.await();
            } catch (final BrokenBarrierException e) {
                LOGGER_RC.warn(
                        "BrokenBarrierException thrown while the barrier is waiting for the other tasks to finish.", e);
                return;
            } catch (final InterruptedException e) {
                LOGGER_RC.warn("InterruptedException thrown the barrier is waiting for the other tasks to finish.", e);
                return;
            } finally {
                // having this here will help us avoid old connections not being
                // let go
                if (!successfulConnection
                        || (!mParent.getHasConnectionErrorArised() && !mParent.isLastTry() && fileSystemManager != null)) {
                    mFileSystemManagerDispenser.closeFileSystemManager();
                }
                else {
                    // we'll add the working connections in a list that the steps can share later on
                    LOGGER_RC.debug("Adding a new fileSystemManager to the cache.");
                    mWorkingConnectionsList.add(fileSystemManager);
                }
            }
        }
    }

    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

    public void setFileSystemManagerDispenser(final FileSystemManagerDispenser fileSystemManagerDispenser) {
        mFileSystemManagerDispenser = fileSystemManagerDispenser;
    }

    public void setFileSystemManagerCache(final FileSystemManagerCache fileSystemManagerCache) {
        mFileSystemManagerCache = fileSystemManagerCache;
    }

    public void setJobRepository(final JobRepository jobRepository) {
        mJobRepository = jobRepository;
    }

}
