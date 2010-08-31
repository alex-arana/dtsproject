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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.common.jms.DtsMessagePayloadTransformer;
import org.dataminx.dts.common.xml.ByteArrayResult;
import org.dataminx.schemas.dts.x2009.x07.messages.InvalidJobDefinitionFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.InvalidJobDefinitionFaultType;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
//import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
//import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.DataCopyActivityDocument;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.DataCopyActivityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.message.MessageBuilder;
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
 *   <li>Converts an incoming JMS message into a DTS Job definition wrapped as a
 *       Spring Integration message.
 *   <li>Converts a supported schema entity into an outgoing JMS message.
 * </ul>
 *
 * @author Alex Arana
 * @author hnguyen
 * @author David Meredith
 * @author Ming Jiang
 */
@Component("dtsMessageConverter")
public class DtsJobSubmitMessageConverter extends SimpleMessageConverter {

    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsJobSubmitMessageConverter.class);

    /** Default format of outgoing messages. */
    private OutputFormat mOutputFormat = OutputFormat.XML_TEXT;

    /** Component used to marshall Java object graphs into XML. */
    private Marshaller mMarshaller;

    /**
     * Component used to transform input DTS Documents into Java objects
     * (unmarshaller = XML -to-> java content objects).
     */
    private DtsMessagePayloadTransformer mTransformer;



    /**
     * Converts a jms message into a Spring Integration message.
     *
     * @param message the input jms message
     * @return a {@link org.springframework.integration.core.Message}. The message body
     *  contains either a {@link DataCopyActivityDocument} or a @{link InvalidJobDefinitionFaultDocument}
     *  depending on whether the given jms message wraps a vaild {@link JobDefinitionDocument}.
     *  All the jms headers are copied into the returned Message headers.
     *  Since this converter is jms specific, both the {@link org.springframework.integration.jms.JmsHeaders.CORRELATION_ID}
     *  and {@link org.springframework.integration.core.MessageHeaders.CORRELATION_ID} headers
     *  are also added and hold the unique job identifier extracted from the
     *  jms correlation id (priority) or jms message id (second). If neither the jms correlation id or
     *  the message id exist, a new UUID is created and is stored.
     * @throws JMSException
     * @throws MessageConversionException
     */
    @Override
    public Object fromMessage(final Message message) throws JMSException,
            MessageConversionException {
        String jobId = null;
        // Try to get the job id from the correlation id or JMSMessageID first. If they are
        // not available, create it with UUID.randomUUID().toString().
        if (message.getJMSCorrelationID() != null
                && !message.getJMSCorrelationID().trim().equals("")) {
            jobId = message.getJMSCorrelationID();
        } else if (message.getJMSMessageID() != null
                && !message.getJMSMessageID().trim().equals("")) {
            jobId = message.getJMSMessageID();
        } else {
            jobId = UUID.randomUUID().toString();
        }

        LOG.info("A new JMS message has been received: " + jobId);
        final Object payload = extractMessagePayload(message);
        LOG.debug(String.format(
                "Finished reading message payload of type: '%s'", payload.getClass().getName()));
        //LOG.debug(payload.toString());

        // Copy all the jms headers into the SI message map
        // e.g. consider the given ClientID property that the client uses to filter
        // messages intended only for them. Here we need to 'turn-around' the message headers even
        // if we dont understand them, e.g. consider the JMS.REPLY_TO header).
        Map jmsMsgHeaders = this.getJmsHeadersAsStringMap(message);
        // Add the integration.jms.CORRELATION_ID so that this header is
        // automatically mapped on any return message.
        jmsMsgHeaders.put(org.springframework.integration.jms.JmsHeaders.CORRELATION_ID, jobId);
        // Store the jobID under the generic integration.CORRELATION_ID (this header
        // is intended for internal spring integration usage and is agonostic of
        // any particular transport or messaging protocol.
        jmsMsgHeaders.put(org.springframework.integration.core.MessageHeaders.CORRELATION_ID, jobId); 

        try {
            final XmlObject expectedXML = (XmlObject) mTransformer.transformPayload(payload);
            if (!(expectedXML instanceof SubmitJobRequestDocument)) {
                throw new Exception("Invaild XML Payload: SubmitJobRequestDocument expected");
            }
            SubmitJobRequestDocument doc = (SubmitJobRequestDocument) expectedXML;
            DataCopyActivityType datacopytype = doc.getSubmitJobRequest().getDataCopyActivity();
            DataCopyActivityDocument ourdoc = DataCopyActivityDocument.Factory.newInstance();
            ourdoc.setDataCopyActivity(datacopytype);
            final MessageBuilder<DataCopyActivityDocument> msgbuilder = MessageBuilder.withPayload(ourdoc).copyHeaders(jmsMsgHeaders);

            //JobDefinitionType jobdeftype = doc.getSubmitJobRequest().getJobDefinition();
            //JobDefinitionDocument ourdoc = JobDefinitionDocument.Factory.newInstance();
            //ourdoc.setJobDefinition(jobdeftype);
            //final MessageBuilder<JobDefinitionDocument> msgbuilder = MessageBuilder.withPayload(ourdoc).copyHeaders(jmsMsgHeaders);
            return msgbuilder.build();

        } catch (final Exception e) {
            LOG.debug("Invalid XML payload: " + e.getMessage());
            final InvalidJobDefinitionFaultDocument document = InvalidJobDefinitionFaultDocument.Factory.newInstance();
            final InvalidJobDefinitionFaultType InvalidJobDefinitionFaultDetail = document.addNewInvalidJobDefinitionFault();
            InvalidJobDefinitionFaultDetail.setMessage(e.getMessage());
            final MessageBuilder<InvalidJobDefinitionFaultDocument> msgbuilder = MessageBuilder.withPayload(document).copyHeaders(jmsMsgHeaders);
            return msgbuilder.build();
        }
    }

    private Map getJmsHeadersAsStringMap(final Message message)
            throws JMSException {
        final Map<String, String> headerProperties = new HashMap<String, String>();
        final Enumeration jmsMsgProperyNames = message.getPropertyNames();
        if (jmsMsgProperyNames != null) {
            while (jmsMsgProperyNames.hasMoreElements()) {
                final String pName = (String) jmsMsgProperyNames.nextElement();
                headerProperties.put(pName, message.getStringProperty(pName));
            }
        }
        return headerProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message toMessage(final Object object, final Session session)
            throws JMSException, MessageConversionException {

        Assert.notNull(object);
        final Class<? extends Object> objectClass = object.getClass();
        if (!mMarshaller.supports(objectClass)) {
            throw new MessageConversionException(
                    String.format(
                    "Unable to convert object of type '%s' to a valid DTS Job update JMS message.",
                    objectClass.getName()));
        }

        // convert the input schema entity to an object we can send back as the payload of a JMS message
        final Result result = createOutputResult();
        try {
            mMarshaller.marshal(object, result);
        } catch (final XmlMappingException ex) {
            final String message = "An error has occurred marshalling the input object graph to an XML document: "
                    + object;
            LOG.error(message, ex);
            throw new MessageConversionException(message, ex);
        } catch (final IOException ex) {
            final String message = "An I/O error has occurred marshalling the input object graph to an XML document: "
                    + object;
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
    private Object extractMessagePayload(final Message message)
            throws JMSException {
        final Object payload;
        if (message instanceof TextMessage) {
            final TextMessage textMessage = (TextMessage) message;
            payload = textMessage.getText();
        } else if (message instanceof ObjectMessage) {
            final ObjectMessage objectMessage = (ObjectMessage) message;
            payload = objectMessage.getObject();
        } else if (message instanceof BytesMessage) {
            final BytesMessage bytesMessage = (BytesMessage) message;
            final byte[] bytes = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytes);
            final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            payload = new StreamSource(bis);
        } else {
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

    public void setMarshaller(final Marshaller marshaller) {
        this.mMarshaller = marshaller;
    }

}