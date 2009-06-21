/**
 * Copyright 2009 - DataMINX Project Team
 * http://www.dataminx.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dataminx.dts.jms;

import java.io.ByteArrayInputStream;
import java.util.Properties;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
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
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

/**
 * Converts an incoming JMS message into a DTS Job definition.
 *
 * @author Alex Arana
 */
@Component
public class DtsMessageConverter implements MessageConverter {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsMessageConverter.class);

    /**
     * A reference to the DTS Job factory.
     */
    @Autowired
    private DtsJobFactory mJobFactory;

    /**
     * Component used to transform input DTS Documents into Java objects.
     */
    @Qualifier("dtsJaxbUnmarshallingTransformer")
    private DtsMessagePayloadTransformer mTransformer;

    /**
     * {@inheritDoc}
     */
    @Override
    public Object fromMessage(Message message) throws JMSException, MessageConversionException {
        final String jobId = message.getJMSMessageID();
        LOG.info("A new JMS message has been received: " + jobId);

        final Object payload = extractMessagePayload(message);
        LOG.debug(String.format("Finished reading message payload of type: '%s'", payload.getClass().getName()));

        // convert the payload into a DTS job definition
        final Object dtsJobRequest = mTransformer.transformPayload(payload);
        LOG.debug("transformed message payload: " + dtsJobRequest);

        // invoke the job factory to create a new job instance
        final DtsJob dtsJob = mJobFactory.createJob(dtsJobRequest);
        LOG.info("Launching DTS Job: " + dtsJob);

        // finally add any additional parameters and return the job request to the framework
        final Properties properties = new Properties();
        return new JobLaunchRequest(dtsJob, new DefaultJobParametersConverter().getJobParameters(properties));
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
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            payload = new StreamSource(bis);
        }
        else {
            throw new MessageConversionException("Invalid message type...");
        }
        return payload;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Message toMessage(Object object, Session session) throws JMSException, MessageConversionException {
        throw new UnsupportedOperationException("Method toMessage() not implemented.");
    }

    public void setTransformer(final DtsMessagePayloadTransformer transformer) {
        mTransformer = transformer;
    }
}
