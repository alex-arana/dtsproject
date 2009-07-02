/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

import org.dataminx.dts.batch.DtsJob;
import org.dataminx.dts.domain.model.JobStatus;

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
     * @param dtsJob An active DTS Job instance
     * @param message An error message to send
     * @param error the cause of the DTS job error containing additional information
     */
    void notifyJobError(DtsJob dtsJob, String message, Throwable error);

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
