/**
 * Copyright (c) 2009, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.wn.jms;

import static org.dataminx.dts.common.xml.XmlUtils.newDocument;

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
import org.apache.commons.lang.StringUtils;
import org.dataminx.dts.batch.DtsJob;
import org.dataminx.dts.batch.DtsJobFactory;
import org.dataminx.dts.common.xml.ByteArrayResult;
import org.dataminx.dts.wn.common.util.SchemaUtils;
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
 *   <li>Converts a supported schema entity into an outgoing JMS message.
 * </ul>
 *
 * @author Alex Arana
 * @author hnguyen
 */
@Component("dtsMessageConverter")
public class DtsMessageConverter extends SimpleMessageConverter {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsMessageConverter.class);

    /**
     * Default format of outgoing messages.
     */
    private OutputFormat mOutputFormat = OutputFormat.XML_TEXT;

    /**
     * A reference to the DTS Job factory.
     */
    @Autowired
    private DtsJobFactory mJobFactory;

    /** Component used to marshall Java object graphs into XML. */
    @Autowired
    @Qualifier("dtsMarshaller")
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
        if (LOG.isDebugEnabled()) {
            final String auditable = SchemaUtils.getAuditableString(dtsJobRequest);
            if (StringUtils.isNotBlank(auditable)) {
                LOG.debug("transformed JMS message payload to DTS schema instance:\n" + auditable);
            }
        }

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
