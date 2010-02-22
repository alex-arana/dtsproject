/**
 *
 */
package org.dataminx.dts.wn.service;

/**
 * Interface to determine whether a WorkNode Job message can be polled from the source.
 *
 * @author hnguyen
 */
public interface WorkerNodeJobPollable {

    /**
     * Determines if this object can poll the message for processing
     *
     * @return <ol>
     *           <li>true if the implementation can handle more message</li>
     *           <li>false if the implementation is at max capacity </li>
     *         </ol>
     */
    boolean canPoll();

}
