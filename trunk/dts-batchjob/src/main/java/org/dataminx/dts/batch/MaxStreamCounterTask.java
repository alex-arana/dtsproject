package org.dataminx.dts.batch;

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

    private final Map<String, FileObject> mFileObjectMap = new HashMap<String, FileObject>();

    private static final Log LOGGER = LogFactory.getLog(JobScoperImpl.class);

    private static final Log LOGGER_RC = LogFactory.getLog(RemoteConnection.class);

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

        // for each DataTransferType, get the corresponding root FileObject from
        // source and target URIs and put
        // unique root FO in a map<URI in String, FileObject>
        for (final DataTransferType dataTransfer : dataTransfers) {

            final FileObject sourceFO = fileSystemManager.resolveFile(dataTransfer.getSource().getURI(), mDtsVfsUtil
                    .createFileSystemOptions(dataTransfer.getSource()));
            final FileObject targetFO = fileSystemManager.resolveFile(dataTransfer.getTarget().getURI(), mDtsVfsUtil
                    .createFileSystemOptions(dataTransfer.getTarget()));

            // TODO: handle cases where in source and destination root File
            // Object of File System are the same but the
            // credentials to access them are different. So just means that
            // those are still two different scenarios.

            // TODO: what do we do then if the restriction on access/connection
            // is on
            // a per-host rather than a per-user access

            if (!mFileObjectMap.containsKey(sourceFO.getFileSystem().getRoot().getURL().toString())) {
                mFileObjectMap.put(sourceFO.getFileSystem().getRoot().getURL().toString(), sourceFO.getFileSystem()
                        .getRoot());
            }
            if (!mFileObjectMap.containsKey(targetFO.getFileSystem().getRoot().getURL().toString())) {
                mFileObjectMap.put(targetFO.getFileSystem().getRoot().getURL().toString(), targetFO.getFileSystem()
                        .getRoot());
            }
        }

        // go through each FO in the map and check for max connections we can
        // make on each one and put in
        // map<URI in String, Integer of max connections>
        for (final FileObject foRoot : mFileObjectMap.values()) {
            final int maxConnections = getMaxConnection(foRoot, mMaxConnectionsToTry);
            LOGGER.debug("Max allowed parallel connections for " + foRoot + ": " + maxConnections);

            // TODO: if returned value is zero.. we might need to try again..
            // if we fail 3x, then job fails

            // TODO: put the returned values to a map
        }

        // TODO: for each DataTransferType again, get the min between the source
        // and
        // target and put in
        // map<MaxConnectionKey, Integer of max connections>
        final Map<MaxConnectionKey, Integer> maxConnectionsMap = new HashMap<MaxConnectionKey, Integer>();
        for (final DataTransferType dataTransfer : dataTransfers) {
            final MaxConnectionKey maxConnectionKey = new MaxConnectionKey(dataTransfer.getSource().getURI(),
                    dataTransfer.getTarget().getURI());

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

    public void setMaxConnectionsToTry(final int maxConnectionsToTry) {
        mMaxConnectionsToTry = maxConnectionsToTry;
    }

    private int getMaxConnection(final FileObject fileObjectRoot, final int maxTry) {

        try {
            // let's use the maximum parallel streams limit if it's a file
            if (fileObjectRoot.getURL().toString().startsWith("file://")) {
                return maxTry;
            }
        } catch (final FileSystemException e1) {
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

            // try testing with a thread higher than the current number of
            // threads we just tried
            threadCounter++;
        }
        return mMaxAllowedConnections;
    }

    private void setHasConnectionErrorArised(final boolean hasConnectionErrorArised) {
        mHasConnectionErrorArised = hasConnectionErrorArised;
    }

    private void startRemoteConnections(final FileObject fileObjectRoot, final int numConnections) {

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
                    .getFileSystem().getFileSystemOptions(), this, "connection" + (i + 1), barrier);
            executor.execute(remoteConnection);
        }

        while (executor.getCompletedTaskCount() != numConnections) {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                LOGGER.warn("InterruptedException thrown while sleeping", e);
            }
        }

        if (mHasConnectionErrorArised) {
            mMaxAllowedConnections = numConnections - 1;
        }
        executor.shutdown();
    }

    private class RemoteConnection implements Runnable {
        private final MaxStreamCounterTask mParent;
        private final String mConnectionName;
        private final CyclicBarrier mBarrier;
        private final String mFoRootURI;
        private final FileSystemOptions mOptions;

        public RemoteConnection(final String foRootURI, final FileSystemOptions options,
                final MaxStreamCounterTask parent, final String connectionName, final CyclicBarrier barrier) {
            mParent = parent;
            mConnectionName = connectionName;
            mBarrier = barrier;
            mFoRootURI = foRootURI;
            mOptions = options;
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
            } finally {
                // having this here will help us avoid old connections not being
                // let go
                if (fileSystemManager != null) {
                    mFileSystemManagerDispenser.closeFileSystemManager();
                }
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
            }
        }
    }

    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

    public void setFileSystemManagerDispenser(final FileSystemManagerDispenser fileSystemManagerDispenser) {
        mFileSystemManagerDispenser = fileSystemManagerDispenser;
    }

}
