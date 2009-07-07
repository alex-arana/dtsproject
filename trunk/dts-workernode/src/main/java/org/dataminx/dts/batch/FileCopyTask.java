/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.batch;

import org.dataminx.dts.service.FileCopyingService;
import org.dataminx.schemas.dts._2009._05.dts.DataTransferType;
import org.dataminx.schemas.dts._2009._05.dts.SourceTargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
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
public class FileCopyTask implements Tasklet {
    /** A reference to the internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(FileCopyTask.class);

    /** A reference to the application's file copying service. */
    @Autowired
    private FileCopyingService mFileCopyingService;

    /** A reference to the input data transfer data structure. */
    private DataTransferType mDataTransfer;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
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
        mFileCopyingService.copyFiles(source.getURI(), target.getURI());

        return RepeatStatus.FINISHED;
    }

    public void setDataTransfer(final DataTransferType dataTransfer) {
        mDataTransfer = dataTransfer;
    }
}