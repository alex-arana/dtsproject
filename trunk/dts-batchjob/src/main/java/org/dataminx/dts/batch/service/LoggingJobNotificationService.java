/**
 * Copyright (c) 2010, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.batch.DtsFileTransferJob;
import org.dataminx.dts.common.model.JobStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

/**
 * The LoggingJobNotificationService is an implementation of the JobNotificationService interface which only
 * logs the method that was called. This implementation is used when the a DTS job is launched from the
 * DtsBulkCopyJobCliRunner and not the DTS workernode.
 *
 * @author Gerson Galang
 */
public class LoggingJobNotificationService implements JobNotificationService {

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(LoggingJobNotificationService.class);

    /**
     * {@inheritDoc}
     */
    public void notifyJobError(final String jobId,
        final JobExecution jobExecution) {
        LOGGER.debug("DtsBulkCopyJobCliRunner notifyJobError()");
    }

    /**
     * {@inheritDoc}
     */
    /*public void notifyJobProgress(final DtsFileTransferJob dtsJob,
        final String message) {
        LOGGER.debug("DtsBulkCopyJobCliRunner notifyJobProgress()");
    }*/

    /**
     * {@inheritDoc}
     */
    public void notifyJobProgress(final String jobId,
        final int filesTransferred, final long volumeTransferred, final StepExecution stepExecution) {
        LOGGER.debug("DtsBulkCopyJobCliRunner notifyJobProgress()");
    }

    /**
     * {@inheritDoc}
     */
    public void notifyJobScope(final String jobId, final int filesTotal,
        final long volumeTotal, final StepExecution stepExecution) {
        LOGGER.debug("DtsBulkCopyJobCliRunner notifyJobScope()");
    }

    /**
     * {@inheritDoc}
     */
    public void notifyJobStatus(final DtsFileTransferJob dtsJob,
        final JobStatus jobStatus, JobExecution jobExecution) {
        LOGGER.debug("DtsBulkCopyJobCliRunner notifyJobStatus()");
    }

    /**
     * {@inheritDoc}
     */
    public void notifyStepFailures(final String jobId,
        final StepExecution stepExecution) {
        LOGGER.debug("DtsBulkCopyJobCliRunner notifyStepFailures()");
    }

}
