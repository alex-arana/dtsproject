/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.jms;

import javax.jms.Queue;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Sends a {@link javax.jms.Message} to a configured JMS {@link Queue}.
 * <p>
 * A {@link org.springframework.jms.support.converter.MessageConverter} would need to be registered to automatically
 * convert to the desired type.
 *
 * @author Alex Arana
 */
@Component
public class JobEventQueueSender implements InitializingBean {
    /** Remote {@link Queue} definition. */
    @Autowired
    @Qualifier("jmsJobEventQueue")
    private Queue mQueue;

    /** Spring helper class to provide JMS support. */
    @Autowired
    private JmsTemplate mJmsTemplate;

    /**
     * Sends a pre-configured message type to a remote destination.
     *
     * @param msg Message to send
     */
    public void doSend(final Object msg) {
        mJmsTemplate.convertAndSend(mQueue, msg);
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mQueue, "A remote queue must be configured for the JMS Queue sender.");
        Assert.notNull(mJmsTemplate, "An instance of JmsTemplate must be configured for the JMS Queue sender.");
    }
}
