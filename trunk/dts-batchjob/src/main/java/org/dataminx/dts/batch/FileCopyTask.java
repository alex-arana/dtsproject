/**
 * Copyright (c) 2009, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
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

import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_DATA_TRANSFER_STEP_KEY;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.dataminx.dts.DtsException;
import org.dataminx.dts.batch.common.util.ExecutionContextCleaner;
import org.dataminx.dts.batch.service.FileCopyingService;
import org.dataminx.dts.batch.service.JobNotificationService;
import org.dataminx.dts.common.batch.util.RootFileObjectComparator;
import org.dataminx.dts.common.util.StopwatchTimer;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.dataminx.dts.common.vfs.FileSystemManagerCache;
import org.dataminx.dts.common.vfs.UnknownFileSystemManagerException;
import org.dataminx.dts.common.vfs.UnknownRootFileObjectException;
import org.dataminx.dts.security.crypto.DummyEncrypter;
import org.dataminx.dts.security.crypto.Encrypter;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * {@link Tasklet}-oriented version of the DTS file transfer job implementation.
 * <p>
 * It is a requirement that the {@link DataTransferType} input to this class be either injected or set manually prior to
 * its {@link #execute(StepContribution, ChunkContext)} method being called.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
public class FileCopyTask implements Tasklet, StepExecutionListener,
    InitializingBean {

    /**
     * The FileCopier is the actual class that does the file-to-file transfer and is run as a thread.
     */
    private class FileCopier implements Runnable {

        /** A reference to the iterator of the List of DtsDataTransferUnits. */
        private final Iterator<DtsDataTransferUnit> mDataTransferUnitIterator;

        /** The FileSystemManager to be used by the source. */
        private final FileSystemManager mSourceFileSystemManager;

        /** The FileSystemManager to be used by the target. */
        private final FileSystemManager mTargetFileSystemManager;

        /** The name of this thread. */
        private final String mCopierName;

        /**
         * The FileCopier constructor.
         *
         * @param dataTransferUnitIterator the reference to the iterator of the List of DtsDataTransferUnits
         * @param copierName the name of this thread
         * @throws UnknownRootFileObjectException if there is no FileSystemManager for the given source and/or target.
         */
        public FileCopier(
            final Iterator<DtsDataTransferUnit> dataTransferUnitIterator,
            final String copierName) throws UnknownRootFileObjectException {
            mCopierName = copierName;
            LOGGER_FC.debug(mCopierName + " started.");

            mDataTransferUnitIterator = dataTransferUnitIterator;

            if (mRootFileObjectComparator.compare(mJobStep
                .getSourceRootFileObjectString(), mJobStep
                .getTargetRootFileObjectString()) == 0) {
                mSourceFileSystemManager = mFileSystemManagerCache
                    .borrowOne(mJobStep.getSourceRootFileObjectString());
                mTargetFileSystemManager = mSourceFileSystemManager;
                LOGGER_FC
                    .debug("Using the same FileSystemManager for the source and destination.");
            }
            else {
                mSourceFileSystemManager = mFileSystemManagerCache
                    .borrowOne(mJobStep.getSourceRootFileObjectString());
                mTargetFileSystemManager = mFileSystemManagerCache
                    .borrowOne(mJobStep.getTargetRootFileObjectString());
                LOGGER_FC
                    .debug("Using different FileSystemManagers for the source and destination.");
            }
        }

        /**
         * Gets the next DtsDataTransferUnit to process.
         *
         * @return the next DtsDataTransferUnit
         */
        public synchronized DtsDataTransferUnit getNextDataTransferUnit() {
            if (mDataTransferUnitIterator.hasNext()) {
                return mDataTransferUnitIterator.next();
            }
            else {
                LOGGER_FC.debug(mCopierName + " has nothing left to process.");
                return null;
            }
        }

        /** Performs the actual data transfer. */
        public void run() {

            // we won't use hasNext() in testing for the contents of the mDataTransferUnitIterator
            // as there might be issues with race conditions.

            DtsDataTransferUnit dataTransferUnit = getNextDataTransferUnit();
            while (dataTransferUnit != null) { // TODO: && !stopped) {

                if (mHasTransferErrorArised) {
                    LOGGER_FC
                        .debug("No need to continue the transfer as an error has occurred on a"
                            + " data transfer being performed by another FileCopier thread.");
                    break;
                }

                LOGGER_FC.debug(mCopierName + " is doing a transfer from "
                    + dataTransferUnit.getSourceFileUri() + " to "
                    + dataTransferUnit.getDestinationFileUri());

                final DataTransferType dataTransfer = ((MinxJobDescriptionType) mSubmitJobRequest
                    .getJobDefinition().getJobDescription())
                    .getDataTransferArray(dataTransferUnit
                        .getDataTransferIndex());

                try {
                    // once we get to this point, we can safely assume that we have successfully authenticated and are
                    // authorised to access the files specified in the DTS job definition document.
                    mFileCopyingService.copyFiles(dataTransferUnit
                        .getSourceFileUri(), dataTransferUnit
                        .getDestinationFileUri(), dataTransfer,
                        mSourceFileSystemManager, mTargetFileSystemManager);

                    // the only reason why the call to copyFile would fail might be due to a DtsException (a descendent
                    // of the RuntimeException) being thrown where we couldn't really do anything much (eg. a sudden
                    // expiration of the user's credential while doing the transfer). at that point, we shouldn't
                    // continue with the transfer anymore.
                }
                catch (final DtsException e) {
                    // no need to continue pro
                    LOGGER_FC.error("A DtsException was thrown while copying "
                        + dataTransferUnit.getSourceFileUri(), e);
                    mHasTransferErrorArised = true;
                    break;
                }
                try {
                    mBatchVolumeSize += mSourceFileSystemManager.resolveFile(
                        dataTransferUnit.getSourceFileUri(),
                        mDtsVfsUtil.getFileSystemOptions(dataTransfer
                            .getSource(), mEncrypter)).getContent().getSize();
                }
                catch (final FileSystemException e) {
                    LOGGER_FC.debug(
                        "FileSystemException was thrown while getting the size of the "
                            + "file that was recently copied.", e);
                }

                dataTransferUnit = getNextDataTransferUnit();
            }

            // let's try and return the borrowed FileSystemManager connections...
            try {
                if (mRootFileObjectComparator.compare(mJobStep
                    .getSourceRootFileObjectString(), mJobStep
                    .getTargetRootFileObjectString()) == 0) {
                    LOGGER_FC.debug(mCopierName
                        + " is returning the FileSystemManager for \""
                        + mJobStep.getSourceRootFileObjectString()
                        + "\" to the cache.");
                    mFileSystemManagerCache.returnOne(mJobStep
                        .getSourceRootFileObjectString(),
                        mSourceFileSystemManager);
                }
                else {
                    LOGGER_FC
                        .debug(mCopierName
                            + " is returning the FileSystemManagers for the source and target to the cache.");
                    mFileSystemManagerCache.returnOne(mJobStep
                        .getSourceRootFileObjectString(),
                        mSourceFileSystemManager);
                    mFileSystemManagerCache.returnOne(mJobStep
                        .getTargetRootFileObjectString(),
                        mTargetFileSystemManager);
                }
            }
            catch (final UnknownFileSystemManagerException e) {
                LOGGER_FC
                    .error("UnknownFileSystemManagerException was thrown by "
                        + mCopierName + "\n" + e.getMessage());

            }
            catch (final UnknownRootFileObjectException e) {
                LOGGER_FC.error("UnknownRootFileObjectException was thrown by "
                    + mCopierName + "\n" + e.getMessage());
            }
        }
    }

    /** The wait time for the finished FileCopier. */
    private static final int FILE_COPIER_WAIT_TIME = 100;

    /** The key used to query for the last completed suspended step from the Execution Context. */
    private static final String LAST_COMPLETED_SUSPENDED_STEP = "lastCompletedSuspendedStep";

    /** A reference to the internal logger object. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(FileCopyTask.class);

    /** The FileCopier's logger. */
    private static final Logger LOGGER_FC = LoggerFactory
        .getLogger(FileCopier.class);

    /** Indicates if an error has occurred while doing the transfer. */
    private volatile boolean mHasTransferErrorArised;

    /** A reference to the application's file copying service. */
    private FileCopyingService mFileCopyingService;

    /** A reference to the application's job notification service. */
    private JobNotificationService mJobNotificationService;

    /** A reference to the ExecutionContextCleaner. */
    private ExecutionContextCleaner mExecutionContextCleaner;

    /** A reference to the DtsVfsUtil. */
    private DtsVfsUtil mDtsVfsUtil;

    /** The list of names of steps to skip. */
    private List<String> mSuspendedStepToSkip;

    /** A reference to the input DTS job request. */
    private SubmitJobRequest mSubmitJobRequest;

    /** A reference to the FileSystemManagerCache. */
    private FileSystemManagerCache mFileSystemManagerCache;

    /** A reference to the StopwatchTimer. */
    private StopwatchTimer mStopwatchTimer;

    /** A reference to the DtsJobStep. */
    private DtsJobStep mJobStep;

    /** A reference to the Encrypter. */
    private Encrypter mEncrypter;

    /** The total size of all the files that have been transferred by this batch. */
    private long mBatchVolumeSize;

    /** The total number of files to be transferred within this batch. */
    private int mBatchTotalFiles;

    /** A reference to the job operator. */
    private JobOperator mJobOperator;

    /** A flag to tell if the step has finished the transfer. */
    private boolean finishedTransfer;

    /** A comparator to see if the Root of the FileObjects are the same. */
    private final RootFileObjectComparator mRootFileObjectComparator = new RootFileObjectComparator();

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(mJobStep != null,
            "Unable to find DtsJobStep in execution context.");
        Assert.state(mDtsVfsUtil != null, "DtsVfsUtil has not been set.");
        Assert.state(mJobNotificationService != null,
            "JobNotificationService has not been set.");
        Assert.state(mExecutionContextCleaner != null,
            "ExecutionContextCleaner has not been set.");
        Assert.state(mFileSystemManagerCache != null,
            "FileSystemManagerCache has not been set.");
        Assert.state(mSubmitJobRequest != null,
            "Unable to find DTS Job Request in execution context.");
        Assert.state(mJobOperator != null, "JobOperator has not been set.");
        if (mEncrypter == null) {
            mEncrypter = new DummyEncrypter();
        }
    }

    /**
     * {@inheritDoc}
     */
    public ExitStatus afterStep(final StepExecution stepExecution) {

        LOGGER.debug("FileCopyTask.afterStep() for step "
            + stepExecution.getStepName() + " has a status of: "
            + stepExecution.getStatus());

        final ExitStatus exitStatus = stepExecution.getExitStatus();
        final String dtsJobId = extractDtsJobId(stepExecution);
        if (stepExecution.getStatus().isUnsuccessful()) {
            // if unsuccessful, we won't report about the files we might have successfully transferred during this step
            mJobNotificationService.notifyStepFailures(dtsJobId, stepExecution);
        }
        else {
            // we'll only delete the entry in the step execution context if the
            // job has not been stopped ie it has completed/finished running
            if (stepExecution.getStatus().equals(BatchStatus.COMPLETED)) {
                mJobNotificationService.notifyJobProgress(dtsJobId,
                    mBatchTotalFiles, mBatchVolumeSize, stepExecution);

                mExecutionContextCleaner.removeStepExecutionContextEntry(
                    stepExecution, DTS_DATA_TRANSFER_STEP_KEY);
            }
            if (mSuspendedStepToSkip != null) {
                LOGGER
                    .debug("^^^^^**** Setting suspended step to skip again to: "
                        + mSuspendedStepToSkip + " ****^^^^^");

                stepExecution.getJobExecution().getExecutionContext().put(
                    LAST_COMPLETED_SUSPENDED_STEP, mSuspendedStepToSkip);
            }
        }

        if (stepExecution.getStatus().equals(BatchStatus.STOPPED)
            && finishedTransfer) {
            if (mSuspendedStepToSkip == null) {

                // this condition gets satisfied the first time a suspend signal is sent
                // and the current step running at the time the suspend signal is sent finishes
                LOGGER.debug("^^^^^**** Setting suspended step to skip to: "
                    + stepExecution.getStepName() + " ****^^^^^");

                final List<String> suspendedSteps = new ArrayList<String>();
                suspendedSteps.add(stepExecution.getStepName());

                stepExecution.getJobExecution().getExecutionContext().put(
                    LAST_COMPLETED_SUSPENDED_STEP, suspendedSteps);
            }
            else {

                try {
                    // check if this has been resumed by at least once
                    if (mJobOperator.getExecutions(
                        stepExecution.getJobExecution().getJobId()).size() > 1
                        && mSuspendedStepToSkip.size() < mJobOperator
                            .getExecutions(
                                stepExecution.getJobExecution().getJobId())
                            .size()) {
                        mSuspendedStepToSkip.add(stepExecution.getStepName());
                    }
                    // this is called when the succeeding steps after the first step that has finished
                    // when a job gets suspended, is called
                    LOGGER
                        .debug("^^^^^**** Setting suspended step to skip again to: "
                            + mSuspendedStepToSkip + " ****^^^^^");

                    stepExecution.getJobExecution().getExecutionContext().put(
                        LAST_COMPLETED_SUSPENDED_STEP, mSuspendedStepToSkip);
                }
                catch (final NoSuchJobInstanceException e) {
                    LOGGER.debug("This line shouldn't be called");
                }
            }
        }

        LOGGER.debug("");
        return exitStatus;
    }

    /**
     * {@inheritDoc}
     */
    public void beforeStep(final StepExecution stepExecution) {
        // perform any preliminary steps here
        LOGGER.debug("");
        LOGGER.debug("vvvvv**** FileCopyTask.beforeStep() is called by step "
            + stepExecution.getStepName() + " ****vvvvv");

        mSuspendedStepToSkip = (List) stepExecution.getJobExecution()
            .getExecutionContext().get(LAST_COMPLETED_SUSPENDED_STEP);

        LOGGER.debug("Name of the suspended steps to skip: "
            + mSuspendedStepToSkip);

        finishedTransfer = false;
    }

    /**
     * Performs the actual copy by accessing unprocessed DataTransferUnits from the list of files to transfer.
     *
     * @param contribution mutable state to be passed back to update the current step execution
     * @param chunkContext attributes shared between invocations but not between restarts
     * @return a RepeatStatus indicating whether processing is continuable
     * @throws Exception on failure
     */
    public RepeatStatus execute(final StepContribution contribution,
        final ChunkContext chunkContext) throws Exception {

        final StepContext stepContext = chunkContext.getStepContext();
        LOGGER.info("Executing copy step: " + stepContext.getStepName());

        if (mSuspendedStepToSkip != null
            && mSuspendedStepToSkip.contains(stepContext.getStepName())) {
            // let's also remove LAST_COMPLETED_SUSPENDED_STEP as we won't be needing it anymore
            //stepContext.getStepExecution().getJobExecution()
            //    .getExecutionContext().remove(LAST_COMPLETED_SUSPENDED_STEP);

            LOGGER
                .info("Skipping this step as it has already finished when suspend was called.");
            return RepeatStatus.FINISHED;
        }

        LOGGER.debug("Started up the FileCopyTask at "
            + mStopwatchTimer.getFormattedElapsedTime());

        // TODO: remove this block of code later on once testing is done
        //if (stepContext.getStepName().equals("fileCopyStep:DATA_TRANSFER_STEP:000")) {
        //    throw new Exception("throw test error in step");
        //}

        mBatchVolumeSize = 0;

        Assert.state(mJobStep != null,
            "Unable to find data transfer input data in step context.");
        LOGGER.debug(mJobStep.toString());

        // TODO: DTUs should be read from the step property files !
        final List<DtsDataTransferUnit> dataTransferUnits = mJobStep
            .getDataTransferUnits();

        // shortcut! as we don't really need to send updates everytime a new dataTransferUnit is processed
        mBatchTotalFiles = dataTransferUnits.size();

        int numConcurrentConnections = mFileSystemManagerCache
            .getSizeOfAvailableFileSystemManagers(mJobStep
                .getSourceRootFileObjectString());

        if (numConcurrentConnections == 0) {
            throw new NoAvailableConnectionException(
                "No available connection for "
                    + mJobStep.getSourceRootFileObjectString());
        }

        if (mRootFileObjectComparator.compare(mJobStep
            .getSourceRootFileObjectString(), mJobStep
            .getTargetRootFileObjectString()) != 0) {
            final int tmpNumConcurrentConnections = mFileSystemManagerCache
                .getSizeOfAvailableFileSystemManagers(mJobStep
                    .getTargetRootFileObjectString());

            // get the minimum between the two
            if (tmpNumConcurrentConnections < numConcurrentConnections) {
                numConcurrentConnections = tmpNumConcurrentConnections;
            }
        }

        // we'll need to test for this condition in here again just in case the
        // destination is the one that doesn't have any available connections for
        // file transfer
        if (numConcurrentConnections == 0) {
            throw new NoAvailableConnectionException(
                "No available connection for "
                    + mJobStep.getTargetRootFileObjectString());
        }

        LOGGER.info("Processing a FileCopyStep that uses "
            + numConcurrentConnections
            + " concurrent connection/s to the remote destination.");

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            numConcurrentConnections, numConcurrentConnections, 10,
            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));

        final Iterator<DtsDataTransferUnit> dataTransferUnitIterator = dataTransferUnits
            .iterator();
        for (int i = 0; i < numConcurrentConnections; i++) {
            final FileCopier fileCopier = new FileCopier(
                dataTransferUnitIterator, "CopierThread" + (i + 1));
            executor.execute(fileCopier);
        }

        while (executor.getCompletedTaskCount() != numConcurrentConnections) {
            try {
                Thread.sleep(FILE_COPIER_WAIT_TIME);
            }
            catch (final InterruptedException e) {
                LOGGER.warn("InterruptedException thrown while sleeping", e);
            }
        }

        executor.shutdown();

        if (mHasTransferErrorArised) {
            throw new DtsException(
                "An error has occurred while one of the FileCopier threads is doing a data transfer.");
        }

        LOGGER.debug("Finished up the FileCopyTask at "
            + mStopwatchTimer.getFormattedElapsedTime());
        finishedTransfer = true;

        // TODO handle failures by returning ...

        return RepeatStatus.FINISHED;
    }

    /**
     * Extracts the ID of this Step's parent DTS Job from the specifiec Step execution context.
     *
     * @param stepExecution
     *            A reference to this Step's execution context
     * @return The parent DTS Job identifier
     */
    private String extractDtsJobId(final StepExecution stepExecution) {
        Assert.state(stepExecution != null);
        return stepExecution.getJobExecution().getJobInstance().getJobName();
    }

    /**
     * Sets the Encrypter.
     *
     * @param encrypter the Encrypter
     */
    public void setEncrypter(final Encrypter encrypter) {
        mEncrypter = encrypter;
    }

    public void setJobOperator(final JobOperator jobOperator) {
        mJobOperator = jobOperator;
    }

    /**
     * Sets the DtsVfsUtil.
     *
     * @param dtsVfsUtil the DtsVfsUtil
     */
    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

    /**
     * Sets the ExecutionContextCleaner.
     *
     * @param executionContextCleaner the ExecutionContextCleaner
     */
    public void setExecutionContextCleaner(
        final ExecutionContextCleaner executionContextCleaner) {
        mExecutionContextCleaner = executionContextCleaner;
    }

    /**
     * Sets the FileCopyingService.
     *
     * @param fileCopyingService the FileCopyingService
     */
    public void setFileCopyingService(
        final FileCopyingService fileCopyingService) {
        mFileCopyingService = fileCopyingService;
    }

    /**
     * Sets the FileSystemManagerCache.
     *
     * @param fileSystemManagerCache the FileSystemManagerCache
     */
    public void setFileSystemManagerCache(
        final FileSystemManagerCache fileSystemManagerCache) {
        mFileSystemManagerCache = fileSystemManagerCache;
    }

    /**
     * Sets the JobNotificationService.
     *
     * @param jobNotificationService the JobNotificationService
     */
    public void setJobNotificationService(
        final JobNotificationService jobNotificationService) {
        mJobNotificationService = jobNotificationService;
    }

    /**
     * Sets the DtsJobStep.
     *
     * @param jobStep the DtsJobStep
     */
    public void setJobStep(final DtsJobStep jobStep) {
        mJobStep = jobStep;
    }

    /**
     * Sets the StopwatchTimer.
     *
     * @param stopwatchTimer the StopwatchTimer
     */
    public void setStopwatchTimer(final StopwatchTimer stopwatchTimer) {
        mStopwatchTimer = stopwatchTimer;
    }

    /**
     * Sets the SubmitJobRequest.
     *
     * @param submitJobRequest the SubmitJobRequest
     */
    public void setSubmitJobRequest(final SubmitJobRequest submitJobRequest) {
        mSubmitJobRequest = submitJobRequest;
    }
}
