/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

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
     * @param jobId Unique job identifier.
     * @param message A progress message to send
     */
    void notifyJobProgress(String jobId, String message);

    /**
     * Posts an error message on the Job Event queue concerning a currently active DTS operation.
     *
     * @param jobId Unique job identifier.
     * @param message An error message to send
     * @param error the cause of the DTS job error containing additional information
     */
    void notifyJobError(String jobId, String message, Throwable error);

    /**
     * Posts a status message on the Job Event queue concerning a currently active DTS operation.
     *
     * @param jobId Unique job identifier.
     * @param message A status message to send
     */
    void notifyJobStatus(String jobId, String message);
}
