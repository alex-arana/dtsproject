/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.jms;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

import java.io.StringWriter;
import java.util.List;
import java.util.UUID;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.common.xml.XmlUtils;
import org.dataminx.dts.wn.batch.DtsJob;
import org.dataminx.dts.wn.batch.DtsJobFactory;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.Marshaller;
import org.springframework.xml.transform.StringResult;
import org.testng.annotations.Test;
import org.unitils.UnitilsTestNG;
import org.unitils.inject.annotation.InjectIntoByType;
import org.unitils.inject.annotation.TestedObject;
import org.unitils.mock.Mock;
import org.unitils.mock.MockUnitils;
import org.unitils.mock.mockbehavior.MockBehavior;
import org.unitils.mock.proxy.ProxyInvocation;
import org.w3c.dom.Document;

/**
 * Test class for {@link DtsMessageConverter}.
 *
 * @author Alex Arana
 */
@Test(groups = "testng-unit-tests")
public class DtsMessageConverterTest extends UnitilsTestNG {
    @InjectIntoByType
    private Mock<DtsJobFactory> mockJobFactory;

    @InjectIntoByType
    private Mock<DtsMessagePayloadTransformer> mockPayloadTransformer;

    @InjectIntoByType
    private Mock<Marshaller> mockMarshaller;

    @TestedObject
    private DtsMessageConverter mConverter;

    /**
     * Test method for {@link org.dataminx.dts.wn.jms.DtsMessageConverter#fromMessage(javax.jms.Message)}.
     */
    @Test
    public void testFromMessage() throws Exception {
        final Document document = XmlUtils.newDocument();
        final Resource xml = new ClassPathResource("/org/dataminx/dts/wn/util/minx-dts.xml");
        XmlUtils.transform(new StreamSource(xml.getInputStream()), new DOMResult(document));

        final String jobId = UUID.randomUUID().toString();
        final SubmitJobRequestDocument jobRequest = SubmitJobRequestDocument.Factory.newInstance();
        mockPayloadTransformer.onceReturns(jobRequest).transformPayload(document);
        final DtsJob dtsJob = MockUnitils.createDummy(DtsJob.class);
        mockJobFactory.returns(dtsJob).createJob(jobId, jobRequest);

        final Mock<ObjectMessage> message = MockUnitils.createMock(ObjectMessage.class);
        message.returns(jobId).getJMSCorrelationID();
        message.returns(document).getObject();
        final Object result = mConverter.fromMessage(message.getMock());
        assertNotNull(result);
        assertThat(result, instanceOf(JobLaunchRequest.class));
        assertSame(dtsJob, ((JobLaunchRequest) result).getJob());
    }

    /**
     * Test method for
     * {@link org.dataminx.dts.wn.jms.DtsMessageConverter#toMessage(java.lang.Object, javax.jms.Session)}.
     */
    @Test
    public void testToMessage() throws Exception {
        final Resource xml = new ClassPathResource("/org/dataminx/dts/wn/util/minx-dts.xml");
        final SubmitJobRequestDocument document = SubmitJobRequestDocument.Factory.parse(xml.getInputStream());
        mockMarshaller.returns(Boolean.TRUE).supports(document.getClass());

        // modify the internal state when the marshal() method is invoked
        final MockBehavior behaviour = new MockBehavior() {
            @Override
            public Object execute(final ProxyInvocation proxyInvocation) throws Throwable {
                final List<Object> arguments = proxyInvocation.getArguments();
                final XmlObject xmlObject = (XmlObject) arguments.get(0);
                final StringResult result = (StringResult) arguments.get(1);
                final StringWriter writer = new StringWriter();
                writer.write(xmlObject.xmlText());
                result.setWriter(writer);
                return null;
            }
        };

        mockMarshaller.oncePerforms(behaviour).marshal(document, null);
        final Mock<Session> session = MockUnitils.createMock(Session.class);
        final TextMessage message = MockUnitils.createDummy(TextMessage.class);
        session.returns(message).createTextMessage(document.xmlText());
        final Message result = mConverter.toMessage(document, session.getMock());

        assertNotNull(result);
        assertSame(result, message);
    }
}
