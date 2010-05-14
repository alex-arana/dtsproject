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

import static org.apache.commons.lang.SystemUtils.LINE_SEPARATOR;
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
import org.dataminx.dts.batch.DtsJob;
import org.dataminx.dts.batch.DtsJobFactory;
import org.dataminx.dts.common.xml.XmlUtils;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument.CancelJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        final CancelJobRequestDocument doc = CancelJobRequestDocument.Factory.newInstance();
        final CancelJobRequest request = doc.addNewCancelJobRequest();
        final String dtsJobId = "dts_001";
        request.setJobResourceKey(dtsJobId);
        final Logger logger = LoggerFactory.getLogger(getClass());
        if (logger.isDebugEnabled()) {
            final String dtsJobRequest = doc.xmlText();
            logger.debug(String.format("submitJobCancelRequestAsText ['%s']:%s%s",
                dtsJobId, LINE_SEPARATOR, dtsJobRequest));
        }

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
