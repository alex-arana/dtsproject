/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MessageConverter;
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

    /** A reference to the DTS JMS message converter. */
    @Autowired
    @Qualifier("dtsMessageConverter")
    private MessageConverter mDtsMessageConverter;

    /**
     * Sends a pre-configured message type to a remote destination.
     *
     * @param jobId DTS Job ID
     * @param message JMS message payload
     */
    public void doSend(final String jobId, final Object message) {
        mJmsTemplate.send(mQueue, new MessageCreator() {
            public Message createMessage(final Session session) throws JMSException {
                final Message jmsMessage = mDtsMessageConverter.toMessage(message, session);
                jmsMessage.setJMSCorrelationID(jobId);
                return jmsMessage;
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mQueue, "A remote queue must be configured for the JMS Queue sender.");
        Assert.notNull(mJmsTemplate, "An instance of JmsTemplate must be configured for the JMS Queue sender.");
    }
}
