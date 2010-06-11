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
package org.dataminx.dts.batch.service;

import org.dataminx.dts.batch.DtsFileTransferJob;
import org.dataminx.dts.common.model.JobStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

/**
 * Notification service used by DTS Worker Node jobs to:
 * <ol>
 * <li>Inform other parts of the system of job progress
 * <li>Notify job lifecycle events (JOB_STARTED, JOB_COMPLETED etc)
 * <li>Notify job exceptions.
 * </ol>
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
public interface JobNotificationService {

    /**
     * Posts an error message on the Job Event queue concerning a currently active DTS operation.
     *
     * @param jobId Unique identifier of the DTS Job in error
     * @param jobExecution the execution context of the DTS Job in error
     */
    void notifyJobError(String jobId, JobExecution jobExecution);

    /**
     * Posts a message on the Job Event queue informing of the progress in a currently active DTS operation.
     *
     * @param dtsJob An active DTS Job instance
     * @param message A progress message to send
     */
    void notifyJobProgress(DtsFileTransferJob dtsJob, String message);

    /**
     * Posts a message on the Job Event queue informing of the progress in a currently active DTS operation.
     *
     * @param jobId Unique identifier of the DTS Job
     * @param filesTransferred number of files that has already been transferred
     * @param volumeTransferred amount of data in bytes that has already been transferred
     */
    void notifyJobProgress(String jobId, int filesTransferred,
        long volumeTransferred, final StepExecution stepExecution);

    /**
     * Posts a message on the Job Event queue informing of the file and volume details of the data that will be
     * transferred from Source to Target.
     *
     * @param jobId Unique identifier of the DTS Job
     * @param filesTotal total number of files that will be transferred
     * @param volumeTotal total size in bytes of all the files that will be transferred
     */
    void notifyJobScope(String jobId, int filesTotal, long volumeTotal, final StepExecution stepExecution);

    /**
     * Posts a status message on the Job Event queue concerning a currently active DTS operation.
     * <p>
     * The list of possible DTS Job statuses is represented by the {@link JobStatus} enumeration.
     *
     * @param dtsJob An active DTS Job instance
     * @param jobStatus DTS Job Status
     */
    void notifyJobStatus(DtsFileTransferJob dtsJob, JobStatus jobStatus, JobExecution jobExecution);

    /**
     * Posts an error message event on the JMS Job Event queue concerning a DTS Job step.
     *
     * @param dtsJobId Unique identifier of the step's parent DTS Job
     * @param stepExecution the execution context of the DTS Job step in error
     */
    void notifyStepFailures(String dtsJobId, StepExecution stepExecution);
}
