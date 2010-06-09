package org.dataminx.dts.wn;

import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecutionException;

public class DelayJobRestartStrategy implements JobRestartStrategy {

    /** Internal application logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DelayJobRestartStrategy.class);

    /** internal restart implementation */
    private final WorkerNodeManager mWorkerNodeManager;

    /** delay time in millisecond */
    private final long millisecond;

    public DelayJobRestartStrategy(WorkerNodeManager manager, long milisecond) {
        this.mWorkerNodeManager = manager;
        this.millisecond = milisecond;
    }

    /**
     * restarts job at a specific time in the future or after a fixed amount of time
     */
    @Override
    public void restartJob(final String jobName) {
        Long instanceId;
        try {
            instanceId = mWorkerNodeManager.getJobInstances(jobName, 0, 1).get(0);
            // get latest execution
            final Long executionId = mWorkerNodeManager.getExecutions(instanceId).get(0);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {

                @Override
                public void run() {
                    try {
                        mWorkerNodeManager.restart(executionId);
                    }
                    catch (JobExecutionException ex) {
                        LOG.debug("Unknown error during restarting job execution" + executionId,ex);
                    }

                }
            }, millisecond);
        }
        catch (JobExecutionException ex1) {
            LOG.debug("Error while querying execution information from job + " + jobName);
        }
    }
}
