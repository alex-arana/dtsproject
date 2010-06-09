package org.dataminx.dts.wn;

import java.util.List;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpJobErrorEventDocument;
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
    private static final Logger LOG = LoggerFactory.getLogger(BatchJobUpdateHandler.class);

    /** to access job instance and executions from job id */
    private JobExplorer mJobExplorer;

    /** query latest job execution of a job instance */
    private JobRepository mJobRepository;

    /** manage how to restart a job if needed */
    private JobRestartStrategy mRestartStrategy;

    /**
     * Depends on type of update of batchjob execution, original update can be forwarded
     * back to client or it will be handled here. Typical action is for restarting a failed
     * execution if maxAttempt is set in the original job launch request.
     * @param message
     * @return message back to client
     */
    @ServiceActivator
    public Message<?> handleBatchJobUpdate(Message<?> message) {
        Object payload  = message.getPayload();
        if (payload instanceof FireUpJobErrorEventDocument) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Received a FireUpJobErrorEventDocument, checking if retry is needed");
            }
            FireUpJobErrorEventDocument jobErrorDoc = (FireUpJobErrorEventDocument)payload;
            final String jobErrorKey = jobErrorDoc.getFireUpJobErrorEvent().getJobResourceKey();
            JobInstance instance = mJobExplorer.getJobInstances(jobErrorKey, 0, 1).get(0);
            long maxAttempts = instance.getJobParameters().getLong("maxAttempts");
            List<JobExecution> executions = mJobExplorer.getJobExecutions(instance);
            long retries = 0;
            for (JobExecution exec:executions) {
                if (exec.getExitStatus().equals(ExitStatus.FAILED)) {
                    retries++;
                }
            }
            if (retries < maxAttempts) {
                mRestartStrategy.restartJob(instance.getJobName());
            }

            return null;
        }
        return message;
    }


    public void setJobExplorer(JobExplorer mJobExplorer) {
        this.mJobExplorer = mJobExplorer;
    }


    public void setJobRepository(JobRepository mJobRepository) {
        this.mJobRepository = mJobRepository;
    }


}
