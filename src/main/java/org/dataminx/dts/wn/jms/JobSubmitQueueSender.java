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
public class JobSubmitQueueSender implements InitializingBean {
    /** Remote {@link Queue} definition. */
    @Autowired
    @Qualifier("jmsJobSubmitQueue")
    private Queue mQueue;

    /** Spring helper class to provide JMS support. */
    @Autowired
    private JmsTemplate mJmsTemplate;

    /**
     * Sends a pre-configured message type to a remote destination.
     *
     * @param jobId DTS Job ID
     * @param message JMS message payload
     */
    public void doSend(final String jobId, final Object message) {
        mJmsTemplate.send(mQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                final MessageConverter messageConverter = mJmsTemplate.getMessageConverter();
                final Message jmsMessage = messageConverter.toMessage(message, session);
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
