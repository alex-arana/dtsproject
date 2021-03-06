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

/**
 * <p>An {@link Handler} that processes the incoming message and output an message that can be
 * scheduled by the {@link DelayHandler}. Other implementation can do the scheduling by itself
 * instead of relying on {@link DelayHandler}.</p>
 * <p>Right now this only handles StartNotBefore but not StartNoLaterThan element in the
 * TransferRequirement element of the schema</p>
 *
 * @author hnguyen
 */
@MessageEndpoint("brokerJobSubmissionChannel")
public class DtsJobScheduler {

    /** A reference to the internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsJobScheduler.class);

    private static final String TIME_IN_THE_PAST = "2010-03-11T17:50:00";

    /** Extracting delay information from the message payload object*/
    @Autowired
    @Qualifier("xmlPayloadDelayExtractor")
    private DelayExtractor mExtractor;

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

        final String auditableRequest = SchemaUtils.getAuditableString(message.getPayload());

        LOG.debug(auditableRequest);

        String delay = mExtractor.extractDelay(message.getPayload());
        delay = (delay != null)? delay : TIME_IN_THE_PAST;

        LOG.debug("Scheduled time to start the job " + delay);
        final Calendar cal = DatatypeConverter.parseDateTime(delay);
        final Date tillDate = cal.getTime();
        return buildMessageWithNewHeader(message, tillDate);

    }

    /* Constructs a new message with approriate header name/value for {@link DelayHandler} to process */
    private Message<?> buildMessageWithNewHeader(final Message<?> message, final Object headerValue) {
        return MessageBuilder.fromMessage(message).setHeader(delayHeaderName, headerValue).build();
    }

}
