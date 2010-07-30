package org.dataminx.dts.wn;

import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public void restartJob(final String jobName) throws Exception {
        Long instanceId;
        instanceId = mWorkerNodeManager.getJobInstances(jobName, 0, 1).get(0);
        // get latest execution
        final Long executionId = mWorkerNodeManager.getExecutions(instanceId).get(0);
        Timer timer = new Timer();
        RestartTimerTask tt = new RestartTimerTask(executionId);
        timer.schedule(tt, millisecond);
        //wait for two seconds to see if an exception was generated inside the RestartTimerTask.run() method
        try {
            Thread.sleep(2000);
        } catch (InterruptedException iex) {
        }
        // see if an exception was thrown.
        Exception x = tt.getThrowEx();
        if (x != null) {
            throw x;
        }
    }


    private class RestartTimerTask extends TimerTask {

        Long execuionId;
        private Exception throwEx = null;

        RestartTimerTask(Long execuionId) {
            this.execuionId = execuionId;
        }

        @Override
        public void run() {
            try {
                mWorkerNodeManager.restart(execuionId);
                // need to send message that job was restarted ok
            } catch (Exception ex) {
                LOG.debug("Unknown error during restarting job execution" + execuionId, ex);
                // need to send message informing of error
                //this.throwEx = ex;
                this.setThrowEx(ex); 
            }
        }

        protected synchronized Exception getThrowEx(){
            return this.throwEx;
        }

        protected synchronized void setThrowEx(Exception ex){
            this.throwEx = ex;
        }
    }
}
