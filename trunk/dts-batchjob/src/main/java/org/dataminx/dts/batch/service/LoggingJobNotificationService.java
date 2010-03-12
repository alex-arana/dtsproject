package org.dataminx.dts.batch.service;

import org.dataminx.dts.common.model.JobStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.batch.DtsJob;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

public class LoggingJobNotificationService implements JobNotificationService {

    private static final Log LOGGER = LogFactory.getLog(LoggingJobNotificationService.class);

    @Override
    public void notifyJobError(final String jobId, final JobExecution jobExecution) {
        LOGGER.debug("DtsBulkCopyJobCLIRunner notifyJobError()");
    }

    @Override
    public void notifyJobProgress(final DtsJob dtsJob, final String message) {
        LOGGER.debug("DtsBulkCopyJobCLIRunner notifyJobProgress()");
    }

    @Override
    public void notifyJobProgress(final String jobId, final int filesTransferred, final long volumeTransferred) {
        LOGGER.debug("DtsBulkCopyJobCLIRunner notifyJobProgress()");
    }

    @Override
    public void notifyJobScope(final String jobId, final int filesTotal, final long volumeTotal) {
        LOGGER.debug("DtsBulkCopyJobCLIRunner notifyJobScope()");
    }

    @Override
    public void notifyJobStatus(final DtsJob dtsJob, final JobStatus jobStatus) {
        LOGGER.debug("DtsBulkCopyJobCLIRunner notifyJobStatus()");
    }

    @Override
    public void notifyStepFailures(final String dtsJobId, final StepExecution stepExecution) {
        LOGGER.debug("DtsBulkCopyJobCLIRunner notifyStepFailures()");
    }

}
