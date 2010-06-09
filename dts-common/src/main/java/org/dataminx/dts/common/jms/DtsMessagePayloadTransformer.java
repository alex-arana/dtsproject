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
package org.dataminx.dts.common.jms;

import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.xml.transformer.XmlPayloadUnmarshallingTransformer;
import org.springframework.oxm.Unmarshaller;
import org.springframework.util.ClassUtils;

/**
 * Deserialises an incoming Message payload into an object graph using a schema 
 * unmarshaller (XML -to-> content objects).
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
    public Object transformPayload(final Object payload) {
        if (ClassUtils.isAssignableValue(XmlObject.class, payload)) {
            return payload;
        }
        return super.transformPayload(payload);
    }
}
