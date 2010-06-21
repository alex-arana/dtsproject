package org.dataminx.dts.wn;

import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument.CancelJobRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument.ResumeJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.Message;

/**
 * A {@link Handler} class that stops or resumes job execution depending on
 * type of the control request.
 *
 * @author hnguyen
 */
public class ControlRequestHandler {

    /** Internal application logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ControlRequestHandler.class);

    /** manager to stop, restart jobs as well as querying batch job related information */
    private WorkerNodeManager mWorkerNodeManager;

    /** job restart strategy */
    private JobRestartStrategy mJobRestartStrategy;

    /**
     * Handles all different control message types sent from the broker or job submit queue. Depending on
     * which control messages, the handler will call cancel, or restart functionality of {@link WorkerNodeManager}
     * class. It is worth noting that this handler doesn't handle errors that this stage because normal errors
     * will be dealt with via the JobNotificationService route. However, there are error cases that need
     * handling here such as dealiing with jobid that is not existed (implementation needed here)
     * @param message
     */
    @ServiceActivator
    public void handleControlRequest(Message<?> message) {
        // this service activator has null return value, therefore, no confirmations
        // can be sent to the dtsJobEvents !

        final Object controlRequest = message.getPayload();
        if (controlRequest instanceof CancelJobRequestDocument) {
            final CancelJobRequest cancelRequest = ((CancelJobRequestDocument)controlRequest).getCancelJobRequest();
            final String jobCancelled = cancelRequest.getJobResourceKey();
            LOG.debug("received cancel job request for " + jobCancelled);
            for (String jobName:mWorkerNodeManager.getJobNames()) {
                if (jobName.equals(jobCancelled)) {
                    LOG.debug("Found running job requested cancelled");
                    try {
                        for (Long execId:mWorkerNodeManager.getRunningExecutions(jobName)) {
                            mWorkerNodeManager.stop(execId);
                        }
                        // do we need to return some confirmation that the job
                        // was stopped ok here ?
                        // Also, do we need to resond if any of the exceptions
                        // are caught below.

                    } catch (NoSuchJobException e) {
                        LOG.debug(e.getMessage());
                    } catch (JobExecutionNotRunningException e) {
                        LOG.debug(e.getMessage());
                    } catch (NoSuchJobExecutionException e) {
                        LOG.debug(e.getMessage());
                    }
                }
            }
        } else if (controlRequest instanceof ResumeJobRequestDocument) {
            final ResumeJobRequest resumeRequest  = ((ResumeJobRequestDocument)controlRequest).getResumeJobRequest();
            final String jobResumed = resumeRequest.getJobResourceKey();
            LOG.debug("Received a resume job request for " + jobResumed);
            mWorkerNodeManager.restartJob(jobResumed);
            // do we need to return some confirmation that the job was resumed
            // ok here ?
            // Also, restartJob seems to swallow all exceptions -
            // shouldn't this class catch any exception and respond to the
            // client accordingly ?

        }
    }

    public void setWorkerNodeManager(final WorkerNodeManager mWorkerNodeManager) {
        this.mWorkerNodeManager = mWorkerNodeManager;
    }

    public void setJobRestartStrategy(final JobRestartStrategy strategy) {
        this.mJobRestartStrategy = strategy;
    }
}
