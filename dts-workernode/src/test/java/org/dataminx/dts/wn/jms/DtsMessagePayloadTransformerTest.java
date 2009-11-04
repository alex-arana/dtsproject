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

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.common.xml.XmlUtils;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.core.MessagingException;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.xmlbeans.XmlBeansMarshaller;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.unitils.mock.MockUnitils;
import org.w3c.dom.Document;

/**
 * Test for {@link DtsMessagePayloadTransformer} class.
 *
 * @author Alex Arana
 */
@Test(groups = "testng-unit-tests")
public class DtsMessagePayloadTransformerTest {
    private Resource mResource;
    private DtsMessagePayloadTransformer mTransformer;

    /**
     * Test constructor {@link DtsMessagePayloadTransformer#DtsMessagePayloadTransformer(Unmarshaller)}.
     */
    @Test
    public void testDtsMessagePayloadTransformer() {
        final Unmarshaller unmarshaller = MockUnitils.createDummy(Unmarshaller.class);
        final DtsMessagePayloadTransformer transformer = new DtsMessagePayloadTransformer(unmarshaller);
        assertNotNull(transformer);
    }

    @BeforeMethod
    public void setUp() {
        mResource = new ClassPathResource("/org/dataminx/dts/wn/util/minx-dts.xml");
        mTransformer = new DtsMessagePayloadTransformer(new XmlBeansMarshaller());
    }

    /**
     * Tests support for conversions of type: {@link Document} to Schema.
     */
    @Test
    public void testTransformPayloadDocument() throws Exception {
        final Document document = XmlUtils.newDocument();
        XmlUtils.transform(new StreamSource(mResource.getInputStream()), new DOMResult(document));
        assertThat("Unable to transform a payload of type 'org.w3c.Document' to a DTS schema type",
            mTransformer.transformPayload(document), instanceOf(XmlObject.class));
    }

    /**
     * Tests support for conversions of type: XML {@link String} to Schema.
     */
    @Test
    public void testTransformPayloadString() throws Exception {
        final String xmlAsString = IOUtils.toString(mResource.getInputStream());
        assertThat("Unable to transform a payload of type 'java.lang.String' to a DTS schema type",
            mTransformer.transformPayload(xmlAsString), instanceOf(XmlObject.class));
    }

    /**
     * Tests support for payloads containing schema instances.
     */
    @Test
    public void testTransformPayloadXmlBeans() throws Exception {
        final SubmitJobRequestDocument xmlBean = SubmitJobRequestDocument.Factory.parse(mResource.getInputStream());
        assertThat(mTransformer.transformPayload(xmlBean), instanceOf(XmlObject.class));
    }

    /**
     * Tests support for conversions of type: XMLBeans DOM {@link Document} to Schema.
     * <p>
     * This is purely an academic test without real practical value as instances of XMLBeans DOM documents
     * are not {@link java.io.Serializable} and thus cannot be marshalled as JMS message payloads.
     */
    @Test
    public void testTransformPayloadXmlBeansDom() throws Exception {
        final SubmitJobRequestDocument xmlBean = SubmitJobRequestDocument.Factory.parse(mResource.getInputStream());
        assertThat(mTransformer.transformPayload(xmlBean.getDomNode()), instanceOf(XmlObject.class));
    }

    /**
     * Tests that exceptions are raised when an unexpected payload type is handled.
     */
    @Test(expectedExceptions = MessagingException.class)
    public void testTransformPayloadError() throws Exception {
        final byte[] bytes = IOUtils.toByteArray(mResource.getInputStream());
        assertThat(mTransformer.transformPayload(bytes), instanceOf(XmlObject.class));
    }
}
