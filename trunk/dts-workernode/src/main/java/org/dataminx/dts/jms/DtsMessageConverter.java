/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.jms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.dataminx.dts.batch.DtsJob;
import org.dataminx.dts.batch.DtsJobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.xml.transform.StringResult;

/**
 * This class is a dual-purpose messaging converter:
 * <ul>
 *   <li>Converts an incoming JMS message into a DTS Job definition.
 *   <li>Converts a supported JAXB2 entity into an outgoing JMS message.
 * </ul>
 *
 * @author Alex Arana
 */
@Component("dtsMessageConverter")
public class DtsMessageConverter extends SimpleMessageConverter {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsMessageConverter.class);

    /** Default format of outgoing messages. */
    private OutputFormat mOutputFormat = OutputFormat.XML_TEXT;

    /**
     * A reference to the DTS Job factory.
     */
    @Autowired
    private DtsJobFactory mJobFactory;

    /** Component used to marshall Java object graphs into XML. */
    @Autowired
    @Qualifier("dtsJaxbUnmarshaller")
    private Marshaller mMarshaller;

    /**
     * Component used to transform input DTS Documents into Java objects.
     */
    @Autowired
    @Qualifier("dtsMessagePayloadTransformer")
    private DtsMessagePayloadTransformer mTransformer;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromMessage(final Message message) throws JMSException, MessageConversionException {
        final String jobId = message.getJMSCorrelationID();
        LOG.info("A new JMS message has been received: " + jobId);

        final Object payload = extractMessagePayload(message);
        LOG.debug(String.format("Finished reading message payload of type: '%s'", payload.getClass().getName()));

        // convert the payload into a DTS job definition
        final Object dtsJobRequest = mTransformer.transformPayload(payload);
        LOG.debug("transformed message payload: " + dtsJobRequest);

        // invoke the job factory to create a new job instance
        final DtsJob dtsJob = mJobFactory.createJob(jobId, dtsJobRequest);
        LOG.info("Launching DTS Job: " + dtsJob);

        // finally add any additional parameters and return the job request to the framework
        final Properties properties = new Properties();
        return new JobLaunchRequest(dtsJob, new DefaultJobParametersConverter().getJobParameters(properties));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message toMessage(final Object object,
        final Session session) throws JMSException, MessageConversionException {

        Assert.notNull(object);
        final Class<? extends Object> objectClass = object.getClass();
        if (!mMarshaller.supports(objectClass)) {
            throw new MessageConversionException(String.format(
                "Unable to convert object of type '%s' to a valid DTS Job update JMS message.", objectClass.getName()));
        }

        // convert the input JAXB2 entity to an object we can send back as the payload of a JMS message
        final Result result = createOutputResult();
        try {
            mMarshaller.marshal(object, result);
        }
        catch (final XmlMappingException ex) {
            final String message =
                "An error has occurred marshalling the input object graph to an XML document: " + object;
            LOG.error(message, ex);
            throw new MessageConversionException(message, ex);
        }
        catch (final IOException ex) {
            final String message =
                "An I/O error has occurred marshalling the input object graph to an XML document: " + object;
            LOG.error(message, ex);
            throw new MessageConversionException(message, ex);
        }

        // use the base class implementation of this method to convert from the output XML to a JMS Message
        return super.toMessage(extractResultOutput(result), session);
    }

    /**
     * Returns a suitable instance of {@link Result} that can be used to hold a transformation result tree.
     *
     * @return a new instance of Result that matches the currently set output format
     */
    private Result createOutputResult() {
        return mOutputFormat == OutputFormat.DOM_OBJECT ? new DOMResult() : new StringResult();
    }

    /**
     * Extract the transformation output held in the specified {@link Result} object depending on its
     * type.
     *
     * @param result an instance of Result that matches the currently set output format
     * @return the transformation output held in the specified <code>Result</code>
     */
    private Object extractResultOutput(final Result result) {
        switch (mOutputFormat) {
            case DOM_OBJECT:
                return ((DOMResult) result).getNode();
            default:
                return result.toString();
        }
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

    public void setOutputFormat(final OutputFormat outputFormat) {
        mOutputFormat = outputFormat;
    }

    public void setTransformer(final DtsMessagePayloadTransformer transformer) {
        mTransformer = transformer;
    }

    /**
     * Enumerated type that represents the various kinds of output that this class can convert outgoing
     * messages to.
     */
    public enum OutputFormat {
        /**
         * Send outgoing messages as Object messages containing a DOM document.
         */
        DOM_OBJECT,

        /**
         * Send outgoing messages as Text messages containing an XML document.
         */
        XML_TEXT
    }
}
