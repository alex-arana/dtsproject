/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

import org.dataminx.dts.batch.DtsJob;
import org.dataminx.dts.domain.model.JobStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

/**
 * Notification service used by DTS Worker Node jobs to:
 * <ol>
 *   <li>Inform other parts of the system of job progress
 *   <li>Notify job lifecycle events (JOB_STARTED, JOB_COMPLETED etc)
 *   <li>Notify job exceptions.
 * </ol>
 *
 * @author Alex Arana
 */
public interface JobNotificationService {

    /**
     * Posts a message on the Job Event queue informing of the progress in a currently active DTS operation.
     *
     * @param dtsJob An active DTS Job instance
     * @param message A progress message to send
     */
    void notifyJobProgress(DtsJob dtsJob, String message);

    /**
     * Posts an error message on the Job Event queue concerning a currently active DTS operation.
     *
     * @param jobId Unique identifier of the DTS Job in error
     * @param jobExecution the execution context of the DTS Job in error
     */
    void notifyJobError(String jobId, JobExecution jobExecution);

    /**
     * Posts an error message event on the JMS Job Event queue concerning a DTS Job step.
     *
     * @param dtsJobId Unique identifier of the step's parent DTS Job
     * @param stepExecution the execution context of the DTS Job step in error
     */
    void notifyStepFailures(String dtsJobId, StepExecution stepExecution);

    /**
     * Posts a status message on the Job Event queue concerning a currently active DTS operation.
     * <p>
     * The list of possible DTS Job statuses is represented by the {@link JobStatus} enumeration.
     *
     * @param dtsJob An active DTS Job instance
     * @param jobStatus DTS Job Status
     */
    void notifyJobStatus(DtsJob dtsJob, JobStatus jobStatus);
}
