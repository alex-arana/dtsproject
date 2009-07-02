package org.dataminx.dts.service;

import org.dataminx.schemas.dts._2009._05.dts.CancelJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;
import org.dataminx.schemas.dts._2009._05.dts.ResumeJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.SubmitJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.SuspendJobRequest;

/**
 * This specifies the operations supported by the Data Transfer Service WS.
 *
 * @author Gerson Galang
 */
public interface DataTransferService {

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

    // *** TEMP METHODS UNTIL I TALK TO ALEX ON WHAT TO DO WITH THE MESSAGE FORMAT ISSUE
    String submitJob(SubmitJobRequest submitJobRequest);
    void cancelJob(CancelJobRequest cancelJobRequest);
    void suspendJob(SuspendJobRequest suspendJobRequest);
    void resumeJob(ResumeJobRequest resumeJobRequest);
    // no need to overload getJobStatus with the signature below as we don't need to query the WN
    // for the status of the job
    //String getJobStatus(GetJobStatusRequest getJobStatusRequest);

}
