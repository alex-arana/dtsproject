/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.batch;

import org.dataminx.dts.wn.service.FileCopyingService;
import org.dataminx.dts.wn.service.JobNotificationService;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * {@link Tasklet}-oriented version of the DTS file transfer job implementation.  It is expected that the
 * item-oriented version of this operation will become the preferred implementation.
 * <p>
 * It is a requirement that the {@link DataTransferType} input to this class be either injected or set manually
 * prior to its {@link #execute(StepContribution, ChunkContext)} method being called.
 *
 * @author Alex Arana
 */
public class FileCopyTask implements Tasklet, StepExecutionListener {
    /** A reference to the internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(FileCopyTask.class);

    /** A reference to the application's file copying service. */
    @Autowired
    private FileCopyingService mFileCopyingService;

    /** A reference to the application's job notification service. */
    @Autowired
    private JobNotificationService mJobNotificationService;

    /** A reference to the input data transfer data structure. */
    private DataTransferType mDataTransfer;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        final StepContext stepContext = chunkContext.getStepContext();
        LOG.info("Executing file copy step: " + stepContext.getStepName());

        Assert.state(mDataTransfer != null, "Unable to find data transfer input data in step context.");
        final SourceTargetType source = mDataTransfer.getSource();
        final SourceTargetType target = mDataTransfer.getTarget();

        //TODO pass the creation flags to the file copying service
        //final CreationFlagEnumeration creationFlag = dataTransfer.getTransferRequirements().getCreationFlag();

        //TODO consider breaking up the job here, by working out all files that need to be transferred as
        //     part of the DataTransferType input and keep returning RepeatStatus.CONTINUABLE to the
        //     framework until all files have been transferred.  Currently, invoking VFS to do an atomic transfer..

        //TODO decide if we should go with this approach later on...
        // Gerson commented this bit to test the changes he made to make GridFTP transfer work
        //mFileCopyingService.copyFiles(source.getURI(), target.getURI());
        mFileCopyingService.copyFiles(source, target);

        return RepeatStatus.FINISHED;
    }

    public void setDataTransfer(final DataTransferType dataTransfer) {
        mDataTransfer = dataTransfer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeStep(final StepExecution stepExecution) {
        // perform any preliminary steps here
    }

    /**
     * Extracts the ID of this Step's parent DTS Job from the specifiec Step execution context.
     *
     * @param stepExecution A reference to this Step's execution context
     * @return The parent DTS Job identifier
     */
    private String extractDtsJobId(final StepExecution stepExecution) {
        Assert.state(stepExecution != null);
        return stepExecution.getJobExecution().getJobInstance().getJobName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {
        final ExitStatus exitStatus = stepExecution.getExitStatus();
        if (stepExecution.getStatus().isUnsuccessful()) {
            final String dtsJobId = extractDtsJobId(stepExecution);
            mJobNotificationService.notifyStepFailures(dtsJobId, stepExecution);
        }
        return exitStatus;
    }
}
