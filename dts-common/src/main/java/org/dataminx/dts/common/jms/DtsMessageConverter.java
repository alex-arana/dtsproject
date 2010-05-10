/**
 *
 */
package org.dataminx.dts.common.jms;

import static org.dataminx.dts.common.xml.XmlUtils.newDocument;

import java.io.IOException;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.common.xml.ByteArrayResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.xml.transformer.XmlPayloadUnmarshallingTransformer;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.SimpleMessageConverter;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.xml.transform.StringResult;

/**
 * @author hnguyen
 *
 */
public class DtsMessageConverter extends SimpleMessageConverter {

    private XmlPayloadUnmarshallingTransformer mTransformer;

    private Marshaller mMarshaller;

    /**
     * Default format of outgoing messages.
     */
    private final OutputFormat mOutputFormat = OutputFormat.XML_TEXT;

    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsMessageConverter.class);

    @Override
    public Object fromMessage(final Message message) throws JMSException, MessageConversionException {
        final String jobId = message.getJMSCorrelationID();
        LOG.info("A new JMS message has been received: " + jobId);
        final Object payload = super.fromMessage(message);
        final Object transformed = mTransformer.transformPayload(payload);
        if (ClassUtils.isAssignableValue(XmlObject.class, transformed)) {
            return transformed;
        }
        return payload;
    }

    @Override
    public Message toMessage(final Object object,
        final Session session) throws JMSException, MessageConversionException {

        Assert.notNull(object);
        final Class<? extends Object> objectClass = object.getClass();
        if (!mMarshaller.supports(objectClass)) {
//            throw new MessageConversionException(String.format(
//                "Unable to convert object of type '%s' to a valid DTS Job update JMS message.", objectClass.getName()));
        }

        // convert the input schema entity to an object we can send back as the payload of a JMS message
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

    public Marshaller getMarshaller() {
        return mMarshaller;
    }

    public void setMarshaller(Marshaller mMarshaller) {
        this.mMarshaller = mMarshaller;
    }

    /**
     * Returns a suitable instance of {@link Result} that can be used to hold a transformation result tree.
     *
     * @return a new instance of Result that matches the currently set output format
     */
    private Result createOutputResult() {
        switch (mOutputFormat) {
            case BYTE_ARRAY:
                return new ByteArrayResult();
            case DOM_OBJECT:
                return new DOMResult(newDocument());
            default:
                return new StringResult();
        }
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
            case BYTE_ARRAY:
                return ((ByteArrayResult) result).toBytes();
            case DOM_OBJECT:
                return ((DOMResult) result).getNode();
            default:
                return result.toString();
        }
    }


    public XmlPayloadUnmarshallingTransformer getTransformer() {
        return mTransformer;
    }

    public void setTransformer(XmlPayloadUnmarshallingTransformer mTransformer) {
        this.mTransformer = mTransformer;
    }
    /**
     * Enumerated type that represents the various kinds of output that this class can use when creating
     * outgoing messages.
     */
    public enum OutputFormat {
        /**
         * Send outgoing messages as an array of bytes containing an XML document.
         */
        BYTE_ARRAY,

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
