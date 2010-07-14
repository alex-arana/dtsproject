/**
 *
 */
package org.dataminx.dts.common.jms;

import java.util.Iterator;
import static org.dataminx.dts.common.broker.DtsBrokerConstants.ROUTING_HEADER_KEY;

import java.util.Map;
import java.util.Set;

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
 * @author Gerson Galang
 */
public class JobQueueSender {

    /** The job submit queue. */
    private Queue mQueue;
    /** A reference to the JMS template. */
    private JmsTemplate mJmsTemplate;

    /**
     * Sets the queue to be used by this message sender.
     *
     * @param queue the job submit queue
     */
    public void setQueue(final Queue queue) {
        mQueue = queue;
    }

    /**
     * Sets the JMS Template.
     *
     * @param jmsTemplate the JMS Template
     */
    public void setJmsTemplate(final JmsTemplate jmsTemplate) {
        mJmsTemplate = jmsTemplate;
    }

    /**
     * Sends a pre-configured message type to a remote destination.
     *
     * @param jobId DTS Job ID
     * @param jmsParameterMap a map of any JMS parameter that users might want to set
     * @param message JMS message payload
     */
    public void doSend(final String jobId,
            final Map<String, Object> jmsParameterMap, final Object message) {
        mJmsTemplate.send(mQueue, new MessageCreator() {

            public Message createMessage(final Session session)
                    throws JMSException {
                final MessageConverter messageConverter = mJmsTemplate.getMessageConverter();
                final Message jmsMessage = messageConverter.toMessage(message,
                        session);
                jmsMessage.setJMSCorrelationID(jobId);
                if (jmsParameterMap != null) {
                    Set<String> keys = jmsParameterMap.keySet();
                    Iterator<String> iterator = keys.iterator();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        jmsMessage.setStringProperty(key, (String) jmsParameterMap.get(key));
                    }


                    /*if (jmsParameterMap.get(ROUTING_HEADER_KEY) != null
                            && !jmsParameterMap.get(ROUTING_HEADER_KEY).toString().trim().equals("")) {
                        jmsMessage.setStringProperty(ROUTING_HEADER_KEY,
                                (String) jmsParameterMap.get(ROUTING_HEADER_KEY));
                    }
                    if (jmsParameterMap.get("ClientID") != null
                            && !jmsParameterMap.get("ClientID").toString().trim().equals("")) {
                        jmsMessage.setStringProperty("ClientID",
                                (String) jmsParameterMap.get("ClientID"));
                    }
                    if (jmsParameterMap.get("DTSWorkerNodeID") != null
                            && !jmsParameterMap.get("DTSWorkerNodeID").toString().trim().equals("")) {
                        jmsMessage.setStringProperty("DTSWorkerNodeID",
                                (String) jmsParameterMap.get("DTSWorkerNodeID"));
                    }*/
                    // check for the other jms properties/parameters here...
                }
                return jmsMessage;
            }
        });
    }

    /**
     * Sends a pre-configured message type to a remote destination.
     *
     * @param jobId DTS Job ID
     * @param message JMS message payload
     */
    public void doSend(final String jobId, final Object message) {
        doSend(jobId, null, message);
    }
}
