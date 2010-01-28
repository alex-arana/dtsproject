/**
 *
 */
package org.dataminx.dts.broker.si;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.core.Message;

/**
 * MessageHandler that picks up message from scheduling queue and schedules
 * the message to be delivered to routing queue according to the scheduling
 * info in the message payload.
 *
 * @author hnguyen
 */
public class JobSchedulingHandler {

    /** routing channel to receive the scheduled message */
    @Autowired
    private QueueChannel dtsRoutingChannel;

    /**
     * Schedules the message according to the info in the message payload. If
     * no info is present, schedules the message to be sent immediately.
     *
     * @param message
     */
    @ServiceActivator
    public void schedule(Message<String> message) {
        // forward immediately
        dtsRoutingChannel.send(message);
    }
}
