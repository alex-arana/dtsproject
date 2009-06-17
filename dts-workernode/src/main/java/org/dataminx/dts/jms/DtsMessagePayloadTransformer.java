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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.xml.transformer.XmlPayloadUnmarshallingTransformer;
import org.springframework.oxm.Unmarshaller;

/**
 * Deserialises an incoming Message payload into an object graph using a JAXB-2 unmarshaller.
 *
 * @author Alex Arana
 */
public class DtsMessagePayloadTransformer extends XmlPayloadUnmarshallingTransformer {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsMessagePayloadTransformer.class);

    /**
     * Constructs a new instance of <code>DtsMessageTransformer</code> using the specified unmarshaller
     * to deserialize a given XML Stream to an Object graph.
     *
     * @param unmarshaller Unmarshaller used to deserialise the given {@link javax.xml.transform.Source}
     *        into an object graph.
     */
    public DtsMessagePayloadTransformer(final Unmarshaller unmarshaller) {
        super(unmarshaller);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transformer
    public Object transformPayload(Object payload) {
        LOG.info("transforming incoming message payload: " + payload);
        return super.transformPayload(payload);
    }
}
