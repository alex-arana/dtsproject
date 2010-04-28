/**
 *
 */
package org.dataminx.dts.broker.si;

import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

import org.apache.xmlbeans.XmlException;
import org.dataminx.dts.common.util.SchemaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.core.Message;
import org.springframework.integration.handler.DelayHandler;
import org.springframework.integration.message.MessageBuilder;
import org.springframework.integration.xml.transformer.XmlPayloadUnmarshallingTransformer;

/**
 * An {@link Handler} that processes the incoming message and output an message that can be
 * scheduled by the {@link DelayHandler}. Other implementation can do the scheduling by itself
 * instead of relying on {@link DelayHandler}.
 *
 * @author hnguyen
 */
@MessageEndpoint("brokerJobSubmissionChannel")
public class DtsJobScheduler {
    /** A reference to the internal logger object. */
    private static final Logger LOG = LoggerFactory
        .getLogger(DtsJobScheduler.class);

    /** Extracting delay information from the message payload object*/
    @Autowired
    @Qualifier("xmlPayloadDelayExtractor")
    private DelayExtractor extractor;

    @Autowired
    @Qualifier("dtsMessagePayloadTransformer")
    private XmlPayloadUnmarshallingTransformer transformer;

    public String getDelayHeaderName() {
        return delayHeaderName;
    }

    @Required
    public void setDelayHeaderName(final String delayHeaderName) {
        this.delayHeaderName = delayHeaderName;
    }

    /** delay header that will store the delay information for {@link DelayHandler} to process */
    private String delayHeaderName;

    /**
     * Schedules the message according to the info in the message payload. If
     * no info is present, schedules the message to be sent immediately.
     *
     * @param message DTS Job message to be schedules
     * @throws XmlException
     */
    @ServiceActivator
    public Message<?> schedule(final Message<?> message) {

        final String auditableRequest = SchemaUtils.getAuditableString(message
            .getPayload());

        LOG.debug(auditableRequest);

        final String delay = extractor.extractDelay(message.getPayload());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Found delay transfer requirement: " + delay);
            LOG.debug("All headers: " + message.getHeaders().toString());
        }
        try {
            final Calendar cal = DatatypeConverter.parseDateTime(delay);
            final Date tillDate = cal.getTime();
            return buildMessageWithNewHeader(message, tillDate);
        }
        catch (final IllegalArgumentException e) {
            LOG.warn("Delay is not xsd:datetime formatted");
            return buildMessageWithNewHeader(message, delay);
        }
    }

    /* Constructs a new message with approriate header name/value for {@link DelayHandler} to process */
    private Message<?> buildMessageWithNewHeader(final Message<?> message,
        final Object headerValue) {
        return MessageBuilder.fromMessage(message).setHeader(delayHeaderName,
            headerValue).build();
    }

}
