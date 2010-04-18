/**
 *
 */
package org.dataminx.dts.broker.util;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MessageConverter;

/**
 * Util class to send message to a target queue using {@link JmsTemplate} configured in
 * spring application context.
 *
 * @author hnguyen
 */
public class JobQueueSender {

    private Queue mQueue;

    public void setmQueue(Queue mQueue) {
        this.mQueue = mQueue;
    }

    public void setmJmsTemplate(JmsTemplate mJmsTemplate) {
        this.mJmsTemplate = mJmsTemplate;
    }

    private JmsTemplate mJmsTemplate;

    /**
     * Sends a pre-configured message type to a remote destination.
     *
     * @param jobId DTS Job ID
     * @param message JMS message payload
     */
    public void doSend(final String jobId, final String dest, final Object message) {
        mJmsTemplate.send(mQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                final MessageConverter messageConverter = mJmsTemplate.getMessageConverter();
                final Message jmsMessage = messageConverter.toMessage(message, session);
                jmsMessage.setJMSCorrelationID(jobId);
                jmsMessage.setStringProperty("routingHeader", dest);
                return jmsMessage;
            }
        });
    }

}
