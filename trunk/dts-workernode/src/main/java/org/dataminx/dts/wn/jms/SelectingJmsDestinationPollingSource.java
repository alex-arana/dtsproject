/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataminx.dts.wn.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import org.springframework.integration.core.Message;
import org.springframework.integration.jms.JmsDestinationPollingSource;
import org.springframework.integration.message.GenericMessage;
import org.springframework.jms.core.JmsTemplate;

/**
 * A source for receiving JMS Messages with a polling listener. This source is
 * only recommended for very low message volume. Otherwise, the
 * {@link JmsMessageDrivenEndpoint} that uses Spring's MessageListener container
 * support is a better option. This class extends {@link JmsDestinationPollingSource}
 * in order to inject a message selector value. The selector is used
 * when the recieve() method is called and when the selector value is not null. 
 *
 * @author David Meredith
 */
public class SelectingJmsDestinationPollingSource extends JmsDestinationPollingSource {

    private volatile String mMessageSelector;

    public SelectingJmsDestinationPollingSource(JmsTemplate jmsTemplate) {
        super(jmsTemplate);
    }

    public SelectingJmsDestinationPollingSource(ConnectionFactory connectionFactory, Destination destination) {
        super(connectionFactory, destination);
    }

    public SelectingJmsDestinationPollingSource(ConnectionFactory connectionFactory, String destinationName) {
        super(connectionFactory, destinationName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Message<Object> receive() {
        Object receivedObject = null;
        receivedObject = this.getJmsTemplate().receiveSelectedAndConvert(this.mMessageSelector);
        if (receivedObject == null) {
            return null;
        }
        if (receivedObject instanceof Message) {
            return (Message) receivedObject;
        }
        return new GenericMessage<Object>(receivedObject);
    }

    /**
     * Specify a JMS Message Selector expression to use when receiving Messages.
     * 
     * @param messageSelector
     */
    public void setMessageSelector(String messageSelector) {
        this.mMessageSelector = messageSelector;
    }

    /**
     * Get the JMS message selector value 
     * @return
     */
    public String getMessageSelector() {
        return this.mMessageSelector;
    }
}
