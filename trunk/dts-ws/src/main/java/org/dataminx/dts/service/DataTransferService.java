package org.dataminx.dts.service;

import org.dataminx.dts.domain.repo.JobDao;
import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;

/**
 * This specifies the operations supported by the Data Transfer Service WS.
 *
 * @author Gerson Galang
 */
public interface DataTransferService {

    /**
     * The job repository to be used by the Data Transfer Service
     *
     * @param jobRepository the job repository
     */
    void setJobRepository(JobDao jobRepository);

    /**
     * Process the submitted job.
     *
     * @param job the job definition
     * @return the Universally Unique Identifier (aka Global Unique Identifier) for the submitted job
     */
    String submitJob(JobDefinitionType job);

    /**
     * Cancel the job.
     *
     * @param jobId the Universally Unique Identifier (aka Global Unique Identifier) of the job to cancel
     */
    void cancelJob(String jobId);

    /**
     * Suspend the job.
     *
     * @param jobId the Universally Unique Identifier (aka Global Unique Identifier) of the job to suspend
     */
    void suspendJob(String jobId);

    /**
     * Resume the job.
     *
     * @param jobId the Universally Unique Identifier (aka Global Unique Identifier) of the job to resume
     */
    void resumeJob(String jobId);

    /**
     * Gets the job status.
     *
     * @param jobId the Universally Unique Identifier (aka Global Unique Identifier) of the job being queried about
     * @return the job status
     */
    String getJobStatus(String jobId);

}
