/**
 *
 */
package org.dataminx.dts.wn.jms;

import org.dataminx.dts.wn.WorkerNodeManager;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;

/**
 * <p>A DTS specific SourcePollingChannelAdapter extension to hook DTS control logic of
 * polling the JMS message from the queue. This Channel Adapter will prevent Queue
 * throttling by only picking up a configurable number of messages for processing instead
 * of trying to receive all possible messages.</p>
 * <p>The intention is to have minimal logic implemented in this class but delegate it to
 * {@link WorkerNodeManager} </p>
 *
 * @author hnguyen
 */
public class DtsSourcePollingChannelAdapter extends SourcePollingChannelAdapter {

    private WorkerNodeManager mWorkerNodeManager;

    public void setmWorkerNodeManager(WorkerNodeManager mWorkerNodeManager) {
        this.mWorkerNodeManager = mWorkerNodeManager;
    }

    @Override
    protected boolean doPoll() {
        if (mWorkerNodeManager.canPoll()) {
            return super.doPoll();
        }
        return false;
    }
}
