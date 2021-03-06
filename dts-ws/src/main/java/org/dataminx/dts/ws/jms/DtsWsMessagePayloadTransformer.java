/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.ws.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.xml.transformer.XmlPayloadUnmarshallingTransformer;
import org.springframework.oxm.Unmarshaller;

/**
 * Deserialises an incoming Message payload into an object graph using an XMLBeans unmarshaller.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
public class DtsWsMessagePayloadTransformer extends
    XmlPayloadUnmarshallingTransformer {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory
        .getLogger(DtsWsMessagePayloadTransformer.class);

    /**
     * Constructs a new instance of <code>DtsMessageTransformer</code> using the specified unmarshaller
     * to deserialize a given XML Stream to an Object graph.
     *
     * @param unmarshaller Unmarshaller used to deserialise the given {@link javax.xml.transform.Source}
     *        into an object graph.
     */
    public DtsWsMessagePayloadTransformer(final Unmarshaller unmarshaller) {
        super(unmarshaller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transformer
    public Object transformPayload(final Object payload) {
        LOG.info("transforming incoming message payload: " + payload);
        return super.transformPayload(payload);
    }
}
