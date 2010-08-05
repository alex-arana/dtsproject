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
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
//import org.dataminx.dts.common.batch.util.FileObjectMap;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.dataminx.dts.common.vfs.FileSystemManagerCache;
import org.dataminx.dts.common.vfs.FileSystemManagerCacheAlreadyInitializedException;
import org.dataminx.dts.security.crypto.DummyEncrypter;
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
 * the data transfer job. This Tasklet will also cache the connections for each source and sink (ie {@link FileSystemManager}s to a
 * {@link FileSystemManagerCache} that the {@link FileCopyTask} steps can share.
 *
 * @author David Meredith
 * @author Gerson Galang
 */
public class MaxStreamCounterTask implements Tasklet, InitializingBean {

    /**
     * A Thread that establishes new {@link FileSystemManager} connections to the
     * given rootURL up to a given limit. 
     */
    private class GatherAndCacheConnectionsToRootUrl extends Thread {

        /** Attempt no more that mConnectionLimit connections to the foRootURI*/
        final int mConnectionLimit;

        /** The FileObject to connect to. */
        private final String mRootURL;

        /** The FileSystemOptions to use for the given FileObject, mFoRootURI. */
        private final FileSystemOptions mOptions;

        /** A reference to the list where a successful FileSystemManager connections are stored  */
        private final List<FileSystemManager> mWorkingConnectionsList = new ArrayList<FileSystemManager>(0);

        /**
         * Construct a new GatherAndCacheConnectionsToRootUrl
         * @param rootURL the root URL to connect to
         * @param options used to make the connections
         * @param connectionLimit make no more connections than this limit
         */
        private GatherAndCacheConnectionsToRootUrl(final String rootURL, final FileSystemOptions options, final int connectionLimit) {
            mRootURL = rootURL;
            mOptions = options;
            this.mConnectionLimit = connectionLimit;
        }

        @Override
        public void run() {
            try {
                for(int i=0; i<this.mConnectionLimit; i++){
                  FileSystemManager  fileSystemManager = mDtsVfsUtil.createNewFsManager();
                  //FileSystemManager fileSystemManager = VFSUtil.createNewFsManager(false, false, false, false, false, true, false, System.getProperty("java.io.tmpdir"));
                  fileSystemManager.resolveFile(mRootURL, mOptions);
                  //successfully resolved/connected so lets add the active fileSystemManager.
                  this.mWorkingConnectionsList.add(fileSystemManager);
                }

            } catch (FileSystemException ex) {
                // OK, looks like thats our lot - no more connections allowed.
                //Logger.getLogger(MaxStreamCounterTask.class.getName()).log(Level.SEVERE, null, ex);
                LOGGER.warn("Max number of connections reached: "+ex.getMessage());
            } finally {
                // if not a single connection could be made, throw early.
                if(mWorkingConnectionsList.size() == 0){
                    throw new DtsJobExecutionException("Unable to establish a single connection in MaxStreamCounterTask to: "+mRootURL);
                }
                // cache the working connections into the parent map
                mWorkingConnectionsListPerRootFileObject.put(mRootURL, mWorkingConnectionsList);
            }
        }

        /**
         * @return the rootURL used to initialize this class 
         */
        private String getRootURL(){
           return this.mRootURL;
        }

        /**
         * @return the number of connections cached so far.
         */
        private int getNumberConnections(){
            return mWorkingConnectionsList.size();
        }
    }

    /** This class' logger. */
    private static final Log LOGGER = LogFactory.getLog(MaxStreamCounterTask.class);

    /** A reference to the SubmitJobRequest document. */
    private SubmitJobRequest mSubmitJobRequest;

    /** The maximum connections to be tested for every source/target FileObject as specified in the config file. */
    private int mMaxConnectionsToTry;

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
    //private final Map<String, FileObject> mFileObjectMap = new FileObjectMap<String, FileObject>();
    private final Map<String, FileObject> mFileObjectMap = new java.util.HashMap<String, FileObject>();

    /**
     * The cache to hold the FileSystemManagers available for each source/target to use during the file copy process.
     * Here we use a ConcurrentHashMap because the map is accessed by different threads.
     */
    //private final Map<String, List<FileSystemManager>> mWorkingConnectionsListPerRootFileObject =
    //    new FileObjectMap<String, List<FileSystemManager>>();
    private final Map<String, List<FileSystemManager>> mWorkingConnectionsListPerRootFileObject =
        new java.util.concurrent.ConcurrentHashMap<String, List<FileSystemManager>>();

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
        if (mEncrypter == null) {
            mEncrypter = new DummyEncrypter();
        }
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
            try {
                fileSystemManager = mDtsVfsUtil.createNewFsManager();
            } catch (final FileSystemException e) {
                throw new DtsJobExecutionException(
                        "FileSystemException was thrown while creating new FileSystemManager in the max stream counter task.",e);
            }

            // TODO: have this step rerun if it fails... use the user's provided info

            final List<DataTransferType> dataTransfers = new ArrayList<DataTransferType>();

            final JobDescriptionType jobDescription = mSubmitJobRequest.getJobDefinition().getJobDescription();
            if (jobDescription instanceof MinxJobDescriptionType) {
                final MinxJobDescriptionType minxJobDescription = (MinxJobDescriptionType) jobDescription;
                CollectionUtils.addAll(dataTransfers, minxJobDescription.getDataTransferArray());
            }
            if (CollectionUtils.isEmpty(dataTransfers)) {
                LOGGER.warn("DTS job request is incomplete as it does not contain any data transfer elements.");
                throw new DtsJobExecutionException(
                        "DTS job request contains no data transfer elements.");
            }

            // for each DataTransferType, get the corresponding root FileObject from source
            // and target URIs and put each source and target unique ROOT FO in a map:
            // mFileObjectMap<ROOT URI String, ROOT FileObject>
            for (final DataTransferType dataTransfer : dataTransfers) {

                final FileObject sourceFO = fileSystemManager.resolveFile(dataTransfer.getSource().getURI(), mDtsVfsUtil.getFileSystemOptions(dataTransfer.getSource(), mEncrypter));
                final FileObject targetFO = fileSystemManager.resolveFile(dataTransfer.getTarget().getURI(), mDtsVfsUtil.getFileSystemOptions(dataTransfer.getTarget(), mEncrypter));

                // TODO: handle cases where in source and destination root File
                // Object of File System are the same but the credentials to
                // access them are different. So just means that those are still
                // two different scenarios.
                // TODO: what do we do then if the restriction on access/connection
                // is on a per-host rather than a per-user access

                final FileObject sourceRoot = sourceFO.getFileSystem().getRoot();
                final FileObject targetRoot = targetFO.getFileSystem().getRoot();

                if (!mFileObjectMap.containsKey(sourceRoot.getURL().toString())) {
                    LOGGER.debug("put source in FileObject map: "+sourceRoot.getURL().toString());
                    mFileObjectMap.put(sourceRoot.getURL().toString(), sourceRoot);
                }
                if (!mFileObjectMap.containsKey(targetRoot.getURL().toString())) {
                    LOGGER.debug("put target in File Object map: "+targetRoot.getURL().toString());
                    mFileObjectMap.put(targetRoot.getURL().toString(), targetRoot);
                }
            }

        } finally {
            // let's close the shared connection here..
            ((DefaultFileSystemManager) fileSystemManager).close();
        }

        final Map<String, Integer> sourceTargetMaxTotalFilesToTransfer = mDtsJobDetails.getSourceTargetMaxTotalFilesToTransfer();

        if(LOGGER.isDebugEnabled()){
          for (final String foRootKey : mFileObjectMap.keySet()) {
            LOGGER.debug("iterate FileObject map, key : "+foRootKey);
          }
        }
        
        // For each ROOT FileObject in the map, establish the max connections we can
        // make on each one using the sourceTargetMaxTotalFilesToTransfer Map<URI in String, Integer of max connections>
        GatherAndCacheConnectionsToRootUrl workerThreads[] = new GatherAndCacheConnectionsToRootUrl[mFileObjectMap.size()];
        int i = 0;
        for (final String foRootKey : mFileObjectMap.keySet()) {
            final FileObject foRoot = mFileObjectMap.get(foRootKey);
            int maxConnections;

            // If there are more files to transfer than the mMaxConnectionsToTry
            // for this source or target (most probably true), we'll try to use UP to our
            // own preset max parallel connections to try.
            if (sourceTargetMaxTotalFilesToTransfer.get(foRootKey) > mMaxConnectionsToTry) {
                //gatherMaxConnections(foRoot, mMaxConnectionsToTry);
                maxConnections = mMaxConnectionsToTry;
            } else {
                // Since there's not that many files to transfer for this source or target
                // we'll try and open up connections UP to the same number of files that will
                // be transferred to/from this source or target
                //gatherMaxConnections(foRoot, sourceTargetMaxTotalFilesToTransfer.get(foRootKey));
                maxConnections = sourceTargetMaxTotalFilesToTransfer.get(foRootKey);
            }
            LOGGER.debug("create GatherAndCacheConnectionsToRootUrl for RootURL: " + foRootKey +" with maxConnections: "+maxConnections);
            // Attempt to create UP to maxConnections FileSystemManager connections
            // to the given foRoot and cache the successfull connections in
            // this.mWorkingConnectionsListPerRootFileObject concurrent map.
            workerThreads[i] = new GatherAndCacheConnectionsToRootUrl(
                    foRoot.getURL().toString(),
                    foRoot.getFileSystem().getFileSystemOptions(), maxConnections);
            workerThreads[i].run();
            ++i;
        }

        // Wait here for the worker threads to complete.
        for (int ii = 0; ii < workerThreads.length; ii++) {
            workerThreads[ii].join(); // throws InterruptedException.
            LOGGER.debug(workerThreads[ii].getNumberConnections() + " Connections cached for: " + workerThreads[ii].getRootURL());
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


    /**
     * Sets the Encrypter.
     *
     * @param encrypter the Encrypter
     */
    public void setEncrypter(final Encrypter encrypter) {
        mEncrypter = encrypter;
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


    public void setJobRepository(final JobRepository jobRepository) {
        mJobRepository = jobRepository;
    }

    public void setMaxConnectionsToTry(final int maxConnectionsToTry) {
        mMaxConnectionsToTry = maxConnectionsToTry;
    }

    public void setSubmitJobRequest(final SubmitJobRequest submitJobRequest) {
        mSubmitJobRequest = submitJobRequest;
    }


}
