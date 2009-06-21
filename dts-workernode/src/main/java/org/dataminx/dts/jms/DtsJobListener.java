/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.jms;

import javax.jms.Message;
import javax.jms.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for incoming DTS job requests in the configured JMS queue.
 *
 * @author Alex Arana
 */
public class DtsJobListener implements MessageListener {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsJobListener.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(final Message message) {
        LOG.info("JMS message received: " + message);
    }
}
