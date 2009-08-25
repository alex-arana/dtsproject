/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
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
