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

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.dataminx.dts.batch.common.util.ExecutionContextCleaner;
import org.dataminx.dts.batch.service.FileCopyingService;
import org.dataminx.dts.batch.service.JobNotificationService;
import org.dataminx.dts.vfs.DtsVfsUtil;
import org.dataminx.dts.vfs.FileSystemManagerCache;
import org.dataminx.dts.vfs.FileSystemManagerDispenser;
import org.dataminx.dts.vfs.UnknownFileSystemManagerException;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * {@link Tasklet}-oriented version of the DTS file transfer job implementation.
 * It is expected that the item-oriented version of this operation will become
 * the preferred implementation.
 * <p>
 * It is a requirement that the {@link DataTransferType} input to this class be
 * either injected or set manually prior to its
 * {@link #execute(StepContribution, ChunkContext)} method being called.
 * 
 * @author Alex Arana
 * @author Gerson Galang
 */
public class FileCopyTask implements Tasklet, StepExecutionListener, InitializingBean {
    /** A reference to the internal logger object. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileCopyTask.class);
    private static final Logger LOGGER_FC = LoggerFactory.getLogger(FileCopier.class);

    /** A reference to the application's file copying service. */
    private FileCopyingService mFileCopyingService;

    /** A reference to the application's job notification service. */
    private JobNotificationService mJobNotificationService;

    private ExecutionContextCleaner mExecutionContextCleaner;

    private DtsVfsUtil mDtsVfsUtil;

    private FileSystemManagerDispenser mFileSystemManagerDispenser;

    private FileSystemManagerCache mFileSystemManagerCache;

    private DtsJobStep mJobStep;

    private long mBatchVolumeSize = 0;
    private int mBatchTotalFiles = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        final StepContext stepContext = chunkContext.getStepContext();
        LOGGER.info("Executing copy step: " + stepContext.getStepName());

        // TODO: remove this block of code later on once testing is done
        //if (stepContext.getStepName().equals("fileCopyStep:DATA_TRANSFER_STEP:001")) {
        //    throw new Exception("throw test error in step");
        //}

        LOGGER.info("Processing a FileCopyStep that uses " + mFileSystemManagerCache.getSize()
                + " concurrent connections to the remote destination.");

        mBatchVolumeSize = 0;

        Assert.state(mJobStep != null, "Unable to find data transfer input data in step context.");
        LOGGER.info(mJobStep.toString());

        final List<DtsDataTransferUnit> dataTransferUnits = mJobStep.getDataTransferUnits();

        // shortcut! as we don't really need to send updates everytime a new dataTransferUnit is processed
        mBatchTotalFiles = dataTransferUnits.size();

        // TODO: replace this with the maxconnections value from ExecutionContext
        final int numConnections = 4;

        final ThreadPoolExecutor executor = new ThreadPoolExecutor(numConnections, numConnections, 10,
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(20));

        final Iterator<DtsDataTransferUnit> dataTransferUnitIterator = dataTransferUnits.iterator();
        for (int i = 0; i < numConnections; i++) {
            final FileCopier fileCopier = new FileCopier(dataTransferUnitIterator, "copier" + (i + 1));
            executor.execute(fileCopier);
        }

        while (executor.getCompletedTaskCount() != numConnections) {
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                LOGGER.warn("InterruptedException thrown while sleeping", e);
            }
        }

        executor.shutdown();

        // TODO handle failures by returning ...

        return RepeatStatus.FINISHED;
    }

    public void setJobStep(final DtsJobStep jobStep) {
        mJobStep = jobStep;
    }

    public void setFileSystemManagerDispenser(final FileSystemManagerDispenser fileSystemManagerDispenser) {
        mFileSystemManagerDispenser = fileSystemManagerDispenser;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeStep(final StepExecution stepExecution) {
        // perform any preliminary steps here
    }

    /**
     * Extracts the ID of this Step's parent DTS Job from the specifiec Step
     * execution context.
     * 
     * @param stepExecution A reference to this Step's execution context
     * @return The parent DTS Job identifier
     */
    private String extractDtsJobId(final StepExecution stepExecution) {
        Assert.state(stepExecution != null);
        return stepExecution.getJobExecution().getJobInstance().getJobName();
    }

    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

    public void setFileCopyingService(final FileCopyingService fileCopyingService) {
        mFileCopyingService = fileCopyingService;
    }

    public void setJobNotificationService(final JobNotificationService jobNotificationService) {
        mJobNotificationService = jobNotificationService;
    }

    public void setExecutionContextCleaner(final ExecutionContextCleaner executionContextCleaner) {
        mExecutionContextCleaner = executionContextCleaner;
    }

    public void setFileSystemManagerCache(final FileSystemManagerCache fileSystemManagerCache) {
        mFileSystemManagerCache = fileSystemManagerCache;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(mJobStep != null, "Unable to find DtsJobStep in execution context.");
        Assert.state(mDtsVfsUtil != null, "DtsVfsUtil has not been set.");
        Assert.state(mFileSystemManagerDispenser != null, "FileSystemManagerDispenser has not been set.");
        Assert.state(mJobNotificationService != null, "JobNotificationService has not been set.");
        Assert.state(mExecutionContextCleaner != null, "ExecutionContextCleaner has not been set.");
        Assert.state(mFileSystemManagerCache != null, "FileSystemManagerCache has not been set.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {
        final ExitStatus exitStatus = stepExecution.getExitStatus();
        final String dtsJobId = extractDtsJobId(stepExecution);
        if (stepExecution.getStatus().isUnsuccessful()) {
            // if unsuccessful, we won't report about the files we might have successfully transferred during this step
            mJobNotificationService.notifyStepFailures(dtsJobId, stepExecution);
        }
        else {
            mJobNotificationService.notifyJobProgress(dtsJobId, mBatchTotalFiles, mBatchVolumeSize);

            mExecutionContextCleaner.removeStepExecutionContextEntry(stepExecution, DTS_DATA_TRANSFER_STEP_KEY);
        }

        return exitStatus;
    }

    private class FileCopier implements Runnable {

        private final Iterator<DtsDataTransferUnit> mDataTransferUnitIterator;
        private final FileSystemManager mFileSystemManager;
        private final String mCopierName;

        public FileCopier(final Iterator<DtsDataTransferUnit> dataTransferUnitIterator, final String copierName) {
            mDataTransferUnitIterator = dataTransferUnitIterator;
            mFileSystemManager = mFileSystemManagerCache.borrowOne();
            mCopierName = copierName;
            LOGGER_FC.debug(mCopierName + " started.");
        }

        @Override
        public void run() {

            // we won't use hasNext() in testing for the contents of the mDataTransferUnitIterator
            // as there might be issues with race conditions.

            DtsDataTransferUnit dataTransferUnit = getNextDataTransferUnit();
            while (dataTransferUnit != null) {//&& !stopped) {

                LOGGER_FC.debug(mCopierName + " is doing a transfer from " + dataTransferUnit.getSourceFileURI()
                        + " to " + dataTransferUnit.getDestinationFileURI());

                mFileCopyingService.copyFiles(dataTransferUnit.getSourceFileURI(), dataTransferUnit
                        .getDestinationFileURI(), dataTransferUnit.getDataTransfer(), mFileSystemManager);

                try {
                    mBatchVolumeSize += mFileSystemManager.resolveFile(dataTransferUnit.getSourceFileURI(),
                            mDtsVfsUtil.createFileSystemOptions(dataTransferUnit.getDataTransfer().getSource()))
                            .getContent().getSize();
                } catch (final FileSystemException e) {
                    LOGGER_FC.debug("FileSystemException was thrown while getting the size of the "
                            + "file that was recently copied.", e);
                }

                dataTransferUnit = getNextDataTransferUnit();
            }
            try {
                mFileSystemManagerCache.returnOne(mFileSystemManager);
            } catch (final UnknownFileSystemManagerException e) {
                LOGGER_FC.error("UnknownFileSystemManagerException was thrown by " + mCopierName);

            }
        }

        public synchronized DtsDataTransferUnit getNextDataTransferUnit() {
            if (mDataTransferUnitIterator.hasNext()) {
                return mDataTransferUnitIterator.next();
            }
            else {
                LOGGER_FC.debug(mCopierName + " has nothing left to process.");
                return null;
            }
        }
    }
}
