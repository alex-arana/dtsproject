package org.dataminx.dts.wn;

import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_DATA_TRANSFER_STEP_KEY;
import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_JOB_DETAILS;
import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_SUBMIT_JOB_REQUEST_KEY;

import java.util.List;

import org.dataminx.dts.batch.common.util.ExecutionContextCleaner;
//import org.dataminx.schemas.dts.x2009.x07.jms.FireUpJobErrorEventDocument;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.callbackevent.FireUpJobErrorEventDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.Message;

public class BatchJobUpdateHandler {

    /** Internal application logger. */
    private static final Logger LOG = LoggerFactory
        .getLogger(BatchJobUpdateHandler.class);

    /** to access job instance and executions from job id */
    private JobExplorer mJobExplorer;

    /** query latest job execution of a job instance */
    private JobRepository mJobRepository;

    /** manage how to restart a job if needed */
    private JobRestartStrategy mRestartStrategy;

    private ExecutionContextCleaner mExecutionContextCleaner;

    /**
     * Depends on type of update of batchjob execution, original update can be forwarded
     * back to client or it will be handled here. Typical action is for restarting a failed
     * execution if maxAttempt is set in the original job launch request.
     * @param message
     * @return message back to client
     */
    @ServiceActivator
    public Message<?> handleBatchJobUpdate(final Message<?> message) {
        final Object payload = message.getPayload();
        if (payload instanceof FireUpJobErrorEventDocument) {
            if (LOG.isDebugEnabled()) {
                LOG
                    .debug("Received a FireUpJobErrorEventDocument, checking if retry is needed");
            }
            final FireUpJobErrorEventDocument jobErrorDoc = (FireUpJobErrorEventDocument) payload;
            final String jobErrorKey = jobErrorDoc.getFireUpJobErrorEvent()
                .getJobResourceKey();
            final JobInstance instance = mJobExplorer.getJobInstances(
                jobErrorKey, 0, 1).get(0);
            final long maxAttempts = instance.getJobParameters().getLong(
                "maxAttempts");
            final List<JobExecution> executions = mJobExplorer
                .getJobExecutions(instance);
            long retries = 0;
            for (final JobExecution exec : executions) {
                if (exec.getExitStatus().equals(ExitStatus.FAILED)) {
                    retries++;
                }
            }
            if (retries < maxAttempts) {
                String jName = instance.getJobName();
                try {
                    mRestartStrategy.restartJob(jName);
                } catch (Exception ex) {
                    LOG.debug("Unknown error during restarting job execution" + jName, ex);

                }
            } else {
                // let's try and clean up the execution context when we know
                // that the job won't be retried anymore
                final List<JobExecution> jobExecutions = mJobExplorer
                    .getJobExecutions(instance);
                mExecutionContextCleaner.forceRemoveExecutionContextEntries(
                    jobExecutions, new String[] {DTS_JOB_DETAILS,
                        DTS_SUBMIT_JOB_REQUEST_KEY},
                    new String[] {DTS_DATA_TRANSFER_STEP_KEY});
                // TODO: we'll need to delete the job step files for this failed job as well
            }

            return null;
        }
        return message;
    }

    public void setJobExplorer(final JobExplorer mJobExplorer) {
        this.mJobExplorer = mJobExplorer;
    }

    public void setJobRepository(final JobRepository mJobRepository) {
        this.mJobRepository = mJobRepository;
    }

    public void setExecutionContextCleaner(
        final ExecutionContextCleaner executionContextCleaner) {
        mExecutionContextCleaner = executionContextCleaner;
    }

}
