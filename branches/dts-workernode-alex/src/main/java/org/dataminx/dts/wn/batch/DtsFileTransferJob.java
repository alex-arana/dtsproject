/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.batch;


import static org.dataminx.dts.wn.common.DtsWorkerNodeConstants.DTS_JOB_ID_KEY;
import static org.dataminx.dts.wn.common.DtsWorkerNodeConstants.DTS_SUBMIT_JOB_REQUEST_KEY;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.wn.common.util.SchemaUtils;
import org.dataminx.dts.wn.common.util.StopwatchTimer;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.StartLimitExceededException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * DTS Job that performs a file copy operation.
 *
 * @author Alex Arana
 */
@Component("dtsFileTransferJob")
@Scope("prototype")
public class DtsFileTransferJob extends DtsJob {
    /** Internal logger object. */
    private final Logger mLogger = LoggerFactory.getLogger(DtsFileTransferJob.class);

    /** Holds the information about the job request. */
    private final SubmitJobRequest mJobRequest;

    /** The master step in this job. */
    @Autowired
    @Qualifier("partitioningStep")
    private Step mPartitioningStep;

    /**
     * Constructs a new instance of <code>DtsSubmitJob</code> using the specified job request details.
     *
     * @param jobId Unique job identifier
     * @param jobRequest Job request details
     * @param jobRepository Job repository
     */
    public DtsFileTransferJob(final String jobId,
        final SubmitJobRequestDocument jobRequest, final JobRepository jobRepository) {

        super(jobId);
        Assert.notNull(jobRequest, "Cannot construct a DTS submit job without the required job details.");
        mJobRequest = jobRequest.getSubmitJobRequest();
        setJobRepository(jobRepository);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return getJobId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return SchemaUtils.extractJobDescription(mJobRequest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute(final JobExecution execution)
        throws JobInterruptedException, JobRestartException, StartLimitExceededException {

        // first set the job start time
        registerStartTime();

        //TODO determine exactly what job notifications we need to return and when
        getJobNotificationService().notifyJobStatus(this, JobStatus.TRANSFERRING);

        // first, store the DTS job request object in the job execution context
        final ExecutionContext context = execution.getExecutionContext();
        context.put(DTS_JOB_ID_KEY, getJobId());
        context.put(DTS_SUBMIT_JOB_REQUEST_KEY, mJobRequest);

        //TODO convert to application exceptions
        final StopwatchTimer timer = new StopwatchTimer();
        final StepExecution stepExecution = handleStep(mPartitioningStep, execution);

        // update the job status to have the same status as the master step
        if (stepExecution != null) {
            mLogger.debug("Upgrading JobExecution status: " + stepExecution);
            execution.upgradeStatus(stepExecution.getStatus());
            execution.setExitStatus(stepExecution.getExitStatus());
        }

        if (stepExecution.getStatus().isUnsuccessful()) {
            getJobNotificationService().notifyJobError(getJobId(), execution);
            return;
        }

        // TODO move this somewhere it always gets called
        registerCompletedTime();
        mLogger.info(String.format("Executed job '%s' in %s", getJobId(), timer.getFormattedElapsedTime()));
        getJobNotificationService().notifyJobStatus(this, JobStatus.DONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("jobId", getJobId())
            .append("description", getDescription())
            .toString();
    }
}
