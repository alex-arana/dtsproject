/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.batch;


import static org.dataminx.dts.wn.common.DtsWorkerNodeConstants.DTS_SUBMIT_JOB_REQUEST_KEY;

import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobIdentificationType;
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
import org.springframework.util.ObjectUtils;

/**
 * DTS Job that performs a file copy operation.
 *
 * @author Alex Arana
 */
@Component("dtsFileTransferJob")
@Scope("prototype")
public class DtsFileTransferJob extends DtsJob {
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
        String description = null;
        final JobIdentificationType jobIdentification = getJobIdentification();
        if (jobIdentification != null) {
            description = jobIdentification.getDescription();
        }
        return description;
    }

    /**
     * Returns the job description, containing all details about the underlying job.
     *
     * @return Job request details
     */
    protected JobDescriptionType getJobDescription() {
        JobDescriptionType result = null;
        final JobDefinitionType jobDefinition = mJobRequest.getJobDefinition();
        if (jobDefinition != null) {
            result = jobDefinition.getJobDescription();
        }
        return result;
    }

    /**
     * Returns the job identification, containing all details about the underlying job.
     *
     * @return Job identification details
     */
    protected JobIdentificationType getJobIdentification() {
        JobIdentificationType result = null;
        final JobDescriptionType jobDescription = getJobDescription();
        if (jobDescription != null) {
            result = jobDescription.getJobIdentification();
        }
        return result;
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
        context.put(DTS_SUBMIT_JOB_REQUEST_KEY, mJobRequest);

        //TODO convert to application exceptions
        final StepExecution stepExecution = handleStep(mPartitioningStep, execution);

        // update the job status to have the same status as the master step
        if (stepExecution != null) {
            logger.debug("Upgrading JobExecution status: " + stepExecution);
            execution.upgradeStatus(stepExecution.getStatus());
            execution.setExitStatus(stepExecution.getExitStatus());
        }

        if (stepExecution.getStatus().isUnsuccessful()) {
            getJobNotificationService().notifyJobError(getJobId(), execution);
            return;
        }

        // TODO move this somewhere it always gets called
        registerCompletedTime();
        getJobNotificationService().notifyJobStatus(this, JobStatus.DONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(ObjectUtils.identityToString(this));
        buffer.append(" [jobId").append("='").append(getJobId()).append("' ");
        buffer.append("jobDescription").append("='").append(getDescription()).append("']");
        return buffer.toString();
    }
}
