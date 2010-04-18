package org.dataminx.dts.broker.si;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.integration.channel.ChannelInterceptor;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;
import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.xml.transformer.XmlPayloadUnmarshallingTransformer;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * A dual purposes {@link ChannelInterceptor} that verifies a if a message payload is Dts schema compatible.
 * It will try to convert the message payload to appropriate Dts schema request type. If one can not
 * be converted, depending on the configuration, one of two following actions will take place:
 * <ol>
 *   <li>send a error message to error channel (if configured)</li>
 *   <li>suppress the message</li>
 * </ol>
 * Otherwise a new message with XmlObject payload is returned
 * @author hnguyen
 */
public class DtsSchemaVerifier extends ChannelInterceptorAdapter implements InitializingBean {

    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsSchemaVerifier.class);

    private XmlPayloadUnmarshallingTransformer mTransformer;

    /**
     *
     * @return a message with converted dts schema compatible payload
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
             Message<?> transformed = mTransformer.transform(message);
             if (ClassUtils.isAssignableValue(XmlObject.class, transformed.getPayload())) {
                 return transformed;
             }
            return null;
        }
        catch (Exception e) {
            // suppress the message
            LOG.debug("This message is not schema convertible", e);
            return null;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mTransformer);
    }

    public void setTransformer(XmlPayloadUnmarshallingTransformer transformer) {
        this.mTransformer = transformer;
    }
}
