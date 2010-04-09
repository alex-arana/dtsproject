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
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dataminx.dts.DtsException;
import org.dataminx.dts.common.batch.util.FileObjectMap;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.dataminx.dts.common.vfs.FileSystemManagerCache;
import org.dataminx.dts.common.vfs.FileSystemManagerCacheAlreadyInitializedException;
import org.dataminx.dts.security.crypto.CryptoLoader;
import org.dataminx.dts.security.crypto.Encrypter;
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
 * The <code>MaxStreamCounterTask</code> is {@link org.springframework.batch.core.step.tasklet.Tasklet} that checks the
 * maximum number of connections the job can have to the sources and sinks it is going to connect to while processing
 * the data transfer job. This Tasklet will also cache the connections (ie {@link FileSystemManager}s to a
 * {@link FileSystemManagerCache} that the {@link FileCopyTask} steps can share.
 *
 * @author Gerson Galang
 */
public class MaxStreamCounterTask implements Tasklet, InitializingBean {

    /**
     * A thread representation of the remote connection of the worker-agent to the given source/target FileObject.
     */
    private class RemoteConnection implements Runnable {

        /** The thread's wait time. */
        private static final int REMOTE_CONNECTION_THREADS_WAIT_TIME = 1000;

        /** A reference to the MaxStreamCounterTask object. */
        private final MaxStreamCounterTask mParent;

        /** The name of this Remote Connection thread. */
        private final String mConnectionName;

        /** The cyclic barrier. */
        private final CyclicBarrier mBarrier;

        /** The FileObject to connect to. */
        private final String mFoRootURI;

        /** The FileSystemOptions to use for the given FileObject, mFoRootURI. */
        private final FileSystemOptions mOptions;

        /**
         * A reference to the list where a successful FileSystemManager connection can be stored after the first
         * time an unsuccessful connection happens.
         */
        private final List<FileSystemManager> mWorkingConnectionsList;

        /** A flag to say if the connection was successful. */
        private boolean mSuccessfulConnection = true;

        /**
         * The RemoteConnection thread's constructor.
         *
         * @param foRootURI the FileObject to connect to
         * @param options the FileSystemOptions to use for the given FileObject, foRootURI
         * @param parent a reference to the MaxStreamCounterTask object
         * @param connectionName the name of this Remote Connection thread
         * @param barrier the cyclic barrier
         * @param workingConnectionsList a reference to the list where a successful FileSystemManager connection can
         *        be stored after the first time an unsuccessful connection happens
         */
        public RemoteConnection(final String foRootURI,
            final FileSystemOptions options, final MaxStreamCounterTask parent,
            final String connectionName, final CyclicBarrier barrier,
            final List<FileSystemManager> workingConnectionsList) {
            mParent = parent;
            mConnectionName = connectionName;
            mBarrier = barrier;
            mFoRootURI = foRootURI;
            mOptions = options;
            mWorkingConnectionsList = workingConnectionsList;
            LOGGER_RC.debug(connectionName + " created.");
        }

        /**
         * {@inheritDoc}
         */
        public void run() {

            FileSystemManager fileSystemManager = null;
            try {
                fileSystemManager = mDtsVfsUtil.createNewFsManager();
                final FileObject fileObject = fileSystemManager.resolveFile(
                    mFoRootURI, mOptions);
                LOGGER_RC.debug(mConnectionName + " successfully connected.");

                try {
                    // just hang in here for a while just in case there's a
                    // delay in connection with the other threads...
                    Thread.sleep(REMOTE_CONNECTION_THREADS_WAIT_TIME);
                }
                catch (final InterruptedException e) {
                    LOGGER_RC.error("InterruptedException during sleep", e);
                }

                try {
                    LOGGER_RC.debug(mConnectionName + " disconnecting.");
                    fileObject.close();
                }
                catch (final FileSystemException e) {
                    LOGGER_RC
                        .warn("FileSystemException thrown during the logout process of the max "
                            + "parallel connection test task.");

                }
            }
            catch (final FileSystemException e) {
                LOGGER_RC
                    .error("FileSystemException thrown during the login process of the max parallel connection test task.");

                mParent.setHasConnectionErrorArised(true);
                mSuccessfulConnection = false;
            }

            try {
                mBarrier.await();
            }
            catch (final BrokenBarrierException e) {
                LOGGER_RC
                    .warn(
                        "BrokenBarrierException thrown while the barrier is waiting for the other tasks to finish.",
                        e);
                return;
            }
            catch (final InterruptedException e) {
                LOGGER_RC
                    .warn(
                        "InterruptedException thrown the barrier is waiting for the other tasks to finish.",
                        e);
                return;
            }
            finally {
                // having this here will help us avoid old connections not being
                // let go
                if (!mSuccessfulConnection
                    || (!mParent.getHasConnectionErrorArised()
                        && !mParent.isLastTry() && (fileSystemManager != null))) {
                    ((DefaultFileSystemManager) fileSystemManager).close();
                }
                else {
                    // we'll add the working connections in a list that the steps can share later on
                    LOGGER_RC
                        .debug("Adding a new fileSystemManager to the cache.");
                    mWorkingConnectionsList.add(fileSystemManager);
                }
            }
        }
    }

    /** This class' logger. */
    private static final Log LOGGER = LogFactory
        .getLog(MaxStreamCounterTask.class);

    /** The RemoteConnection's thread's logger. */
    private static final Log LOGGER_RC = LogFactory
        .getLog(RemoteConnection.class);

    /** The wait time for the thread before it tries to check again if other threads have finished. */
    private static final int MAX_STREAM_COUNTER_THREAD_WAIT_TIME = 100;

    /** A reference to the SubmitJobRequest document. */
    private SubmitJobRequest mSubmitJobRequest;

    /** The maximum connections to be tested for every source/target FileObject as specified in the config file. */
    private int mMaxConnectionsToTry;

    /**
     * A flag to tell if an error arised while the worker-agent is trying to see if it can make another concurrent
     * connection to the given FileObject.
     */
    private volatile boolean mHasConnectionErrorArised;

    /** A reference to DtsVfsUtil. */
    private DtsVfsUtil mDtsVfsUtil;

    /** A reference to the Job repository. */
    private JobRepository mJobRepository;

    /** A reference to the FileSystemManagerCache. */
    private FileSystemManagerCache mFileSystemManagerCache;

    /** A reference to the DTS job details. */
    private DtsJobDetails mDtsJobDetails;

    /** A reference to the Encrypter. */
    private Encrypter mEncrypter;

    /** A container for unique FileObjects specified as source/targets in the job. */
    private final Map<String, FileObject> mFileObjectMap = new FileObjectMap<String, FileObject>();

    /**
     * The cache to hold the FileSystemManagers available for each source/target to use during the file copy process.
     */
    private final Map<String, List<FileSystemManager>> mWorkingConnectionsListPerRootFileObject = new FileObjectMap<String, List<FileSystemManager>>();

    /** An attribute that will say if maximum number of retries is reached. */
    private volatile boolean isLastTry;

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(mSubmitJobRequest != null,
            "Unable to find DTS Job Request in execution context.");
        Assert.state(mDtsVfsUtil != null, "DtsVfsUtil has not been set.");
        Assert.state(mMaxConnectionsToTry != 0,
            "MaxConnectionsToTry has not been set.");
        Assert.state(mJobRepository != null, "JobRepository has not been set.");
        Assert.state(mEncrypter != null, "CryptoLoader has not been set.");
    }

    /**
     * Start checking for the maximum number of connections each given source/target can have.
     *
     * @param contribution mutable state to be passed back to update the current step execution
     * @param chunkContext attributes shared between invocations but not between restarts
     * @return a RepeatStatus indicating whether processing is continuable
     * @throws Exception on failure
     */
    public RepeatStatus execute(final StepContribution contribution,
        final ChunkContext chunkContext) throws Exception {
        LOGGER.debug("MaxStreamCounterTask execute()");

        FileSystemManager fileSystemManager = null;
        try {
            fileSystemManager = mDtsVfsUtil.createNewFsManager();
        }
        catch (final FileSystemException e) {
            throw new DtsJobExecutionException(
                "FileSystemException was thrown while creating new FileSystemManager in the max stream counter task.",
                e);
        }

        // TODO: have this step rerun if it fails... use the user's provided
        // info

        final List<DataTransferType> dataTransfers = new ArrayList<DataTransferType>();

        final JobDescriptionType jobDescription = mSubmitJobRequest
            .getJobDefinition().getJobDescription();
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

        // for each DataTransferType, get the corresponding root FileObject from source
        // and target URIs and put unique root FO in a map<URI in String, FileObject>
        for (final DataTransferType dataTransfer : dataTransfers) {

            final FileObject sourceFO = fileSystemManager.resolveFile(
                dataTransfer.getSource().getURI(), mDtsVfsUtil
                    .createFileSystemOptions(dataTransfer.getSource(),
                        mEncrypter));
            final FileObject targetFO = fileSystemManager.resolveFile(
                dataTransfer.getTarget().getURI(), mDtsVfsUtil
                    .createFileSystemOptions(dataTransfer.getTarget(),
                        mEncrypter));

            // TODO: handle cases where in source and destination root File
            // Object of File System are the same but the credentials to
            // access them are different. So just means that those are still
            // two different scenarios.

            // TODO: what do we do then if the restriction on access/connection
            // is on a per-host rather than a per-user access

            if (!mFileObjectMap.containsKey(sourceFO.getFileSystem().getRoot()
                .getURL().toString())) {
                mFileObjectMap.put(sourceFO.getFileSystem().getRoot().getURL()
                    .toString(), sourceFO.getFileSystem().getRoot());
            }
            if (!mFileObjectMap.containsKey(targetFO.getFileSystem().getRoot()
                .getURL().toString())) {
                mFileObjectMap.put(targetFO.getFileSystem().getRoot().getURL()
                    .toString(), targetFO.getFileSystem().getRoot());
            }
        }

        // let's close the connection here..
        ((DefaultFileSystemManager) fileSystemManager).close();

        final Map<String, Integer> sourceTargetMaxTotalFilesToTransfer = new FileObjectMap<String, Integer>();
        sourceTargetMaxTotalFilesToTransfer.putAll(mDtsJobDetails
            .getSourceTargetMaxTotalFilesToTransfer());

        // go through each FO in the map and check for max connections we can
        // make on each one and put in map<URI in String, Integer of max connections>
        for (final String foRootKey : mFileObjectMap.keySet()) {

            final FileObject foRoot = mFileObjectMap.get(foRootKey);

            // if there are more files to transfer for this source/target, we'll use
            // our own preset max parallel connections to try.
            if (sourceTargetMaxTotalFilesToTransfer.get(foRootKey) > mMaxConnectionsToTry) {
                gatherMaxConnections(foRoot, mMaxConnectionsToTry);
            }
            else {
                // since there's not that many files to transfer for this source/target
                // we'll try and open up connections to the same number of files that will
                // be transferred from this source/target
                gatherMaxConnections(foRoot,
                    sourceTargetMaxTotalFilesToTransfer.get(foRootKey));
            }
        }

        try {
            // TODO: remove this later on... or change FileSystemManagerCache implementation
            mFileSystemManagerCache
                .initFileSystemManagerCache(mWorkingConnectionsListPerRootFileObject);
        }
        catch (final FileSystemManagerCacheAlreadyInitializedException e) {
            LOGGER
                .error(
                    "Initialisation of FileSystemManagerCache failed because it has not been cleared yet.",
                    e);
            throw e;
        }
        return RepeatStatus.FINISHED;
    }

    /**
     * Gather the maximum connections available for the given FileObject by putting them in a
     * {@link FileSystemManagerCache}.
     *
     * @param fileObjectRoot the FileObject to be tested for maximum concurrent connections
     * @param maxTry the maximum number of connections to try
     * @throws FileSystemException if the given fileObjectRoot cannot be accessed at any given time
     */
    private void gatherMaxConnections(final FileObject fileObjectRoot,
        final int maxTry) throws FileSystemException {
        isLastTry = false;

        int threadCounter = 1;

        try {
            // let's use the maximum parallel streams limit if it's a file
            if (fileObjectRoot.getURL().toString().startsWith("file://")
                || fileObjectRoot.getURL().toString().startsWith("tmp://")) {
                threadCounter = maxTry;
            }
        }
        catch (final FileSystemException e1) {
            throw new DtsException(
                "Error occurred while getting the file object root's URL");
        }
        List<FileSystemManager> workingConnections = null;
        while (threadCounter <= maxTry) {
            if (threadCounter == maxTry) {
                isLastTry = true;
            }
            LOGGER.debug("==========================");
            LOGGER.debug("Trying with " + threadCounter + " threads on "
                + fileObjectRoot.getURL().toString() + ".");
            workingConnections = startRemoteConnections(fileObjectRoot,
                threadCounter);

            if (mHasConnectionErrorArised) {
                break;
            }

            if (LOGGER.isDebugEnabled() && (threadCounter != maxTry)) {
                Assert.isTrue(workingConnections.isEmpty(),
                    "WorkingConnections list should be empty.");
            }

            // try testing with a thread higher than the current number of
            // threads we just tried
            threadCounter++;
        }

        if (LOGGER.isDebugEnabled() && (threadCounter == maxTry)) {
            Assert
                .isTrue(
                    maxTry != workingConnections.size(),
                    "MaxAllowedConnections should have the same number of connections in the workingConnections list.");
        }
        try {
            mWorkingConnectionsListPerRootFileObject.put(fileObjectRoot
                .getURL().toString(), workingConnections);
        }
        catch (final FileSystemException e) {
            throw e;
        }

        LOGGER.info("Max allowed concurrent connections for FileObject root \""
            + fileObjectRoot.getURL().toString() + "\": "
            + workingConnections.size());
    }

    private boolean getHasConnectionErrorArised() {
        return mHasConnectionErrorArised;
    }

    public boolean isLastTry() {
        return isLastTry;
    }

    /**
     * Sets the CryptoLoader.
     *
     * @param cryptoLoader the CryptoLoader
     */
    @SuppressWarnings("unchecked")
    public void setCryptoLoader(final String cryptoLoader) {
        try {
            final Class cryptLoaderClass = Class.forName(cryptoLoader);
            mEncrypter = ((CryptoLoader) cryptLoaderClass.newInstance())
                .getEncrypter();
        }
        catch (final ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        }
        catch (final InstantiationException e) {
            LOGGER.error(e.getMessage(), e);
        }
        catch (final IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void setDtsJobDetails(final DtsJobDetails dtsJobDetails) {
        mDtsJobDetails = dtsJobDetails;
    }

    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

    public void setFileSystemManagerCache(
        final FileSystemManagerCache fileSystemManagerCache) {
        mFileSystemManagerCache = fileSystemManagerCache;
    }

    private void setHasConnectionErrorArised(
        final boolean hasConnectionErrorArised) {
        mHasConnectionErrorArised = hasConnectionErrorArised;
    }

    public void setJobRepository(final JobRepository jobRepository) {
        mJobRepository = jobRepository;
    }

    public void setMaxConnectionsToTry(final int maxConnectionsToTry) {
        mMaxConnectionsToTry = maxConnectionsToTry;
    }

    public void setSubmitJobRequest(final SubmitJobRequest submitJobRequest) {
        mSubmitJobRequest = submitJobRequest;
    }

    /**
     * Checks to see if the worker-agent can be allowed to make a maximum of numConnections to the given fileObjectRoot.
     *
     * @param fileObjectRoot the FileObject to access
     * @param numConnections the number of connections to try
     * @return a list of FileSystemManagers that will be cached if the workeragent can only make "numConnections - 1"
     * connections. Returns an empty list on other occasions.
     */
    private List<FileSystemManager> startRemoteConnections(
        final FileObject fileObjectRoot, final int numConnections) {

        final List<FileSystemManager> workingConnectionsList = new ArrayList<FileSystemManager>();

        final CyclicBarrier barrier = new CyclicBarrier(numConnections);
        mHasConnectionErrorArised = false;

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            numConnections, numConnections, 10, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(20));

        String fileObjectRootString = "";
        try {
            fileObjectRootString = fileObjectRoot.getURL().toString();
        }
        catch (final FileSystemException e1) {
            throw new DtsException(
                "Error occurred while getting the file object root's URL");
        }
        for (int i = 0; i < numConnections; i++) {
            final RemoteConnection remoteConnection = new RemoteConnection(
                fileObjectRootString, fileObjectRoot.getFileSystem()
                    .getFileSystemOptions(), this,
                "ConnectionThread" + (i + 1), barrier, workingConnectionsList);
            executor.execute(remoteConnection);
        }

        while (executor.getCompletedTaskCount() != numConnections) {
            try {
                Thread.sleep(MAX_STREAM_COUNTER_THREAD_WAIT_TIME);
            }
            catch (final InterruptedException e) {
                LOGGER.warn("InterruptedException thrown while sleeping", e);
            }
        }

        executor.shutdown();

        // TODO: probably need to remove mMaxAllowedConnections if workignConnectionsList can provide the same info
        return workingConnectionsList;
    }

}
