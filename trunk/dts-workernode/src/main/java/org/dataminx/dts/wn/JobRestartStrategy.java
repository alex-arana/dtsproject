/**
 *
 */
package org.dataminx.dts.wn;


/**
 * Strategy interface for restarting a job execution
 *
 * @author hnguyen
 */
public interface JobRestartStrategy {

    public void restartJob(String jobName);

}
