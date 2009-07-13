/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.jms;

import java.io.ByteArrayInputStream;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.oxm.Marshaller;
import org.springframework.stereotype.Component;

/**
 * This class is a dual-purpose messaging converter:
 * <ul>
 *   <li>Converts an incoming JMS message into a DTS Job definition.
 *   <li>Converts a supported JAXB2 entity into an outgoing JMS message.
 * </ul>
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
@Component("dtsWsMessageConverter")
public class DtsWsMessageConverter extends SimpleMessageConverter {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsWsMessageConverter.class);


    /** Component used to marshall Java object graphs into XML. */
    @Autowired
    private Marshaller mMarshaller;

    /**
     * Component used to transform input DTS Documents into Java objects.
     */
    @Autowired
    @Qualifier("dtsWsMessagePayloadTransformer")
    private DtsWsMessagePayloadTransformer mTransformer;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromMessage(final Message message) throws JMSException, MessageConversionException {
        final String jobId = message.getJMSCorrelationID();
        LOG.info("A new JMS message has been received: " + jobId);

        final Object payload = extractMessagePayload(message);
        LOG.debug(String.format("Finished reading message payload of type: '%s'", payload.getClass().getName()));

        // convert the payload into a JobEventUpdateRequest object
        final Object jobEventUpdateRequest = mTransformer.transformPayload(payload);
        LOG.debug("transformed message payload: " + jobEventUpdateRequest);

        return jobEventUpdateRequest;
    }

    /**
     * Extracts the given JMS Message payload and returns it as an object.
     *
     * @param message the incoming JMS message
     * @return the message payload as an {@link Object}
     * @throws JMSException if the incoming message is not of a supported message type
     */
    private Object extractMessagePayload(final Message message) throws JMSException {
        final Object payload;
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            payload = textMessage.getText();
        }
        else if (message instanceof ObjectMessage) {
            final ObjectMessage objectMessage = (ObjectMessage) message;
            payload = objectMessage.getObject();
        }
        else if (message instanceof BytesMessage) {
            final BytesMessage bytesMessage = (BytesMessage) message;
            final byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            payload = new StreamSource(bis);
        }
        else {
            throw new MessageConversionException("Invalid message type...");
        }
        return payload;
    }

    public void setTransformer(final DtsWsMessagePayloadTransformer transformer) {
        mTransformer = transformer;
    }
}
