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

import static org.dataminx.dts.common.util.TestFileChooser.getTestFilePostfix;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.StringWriter;
import java.util.UUID;

import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.batch.DtsFileTransferJob;
import org.dataminx.dts.batch.DtsJobFactory;
import org.dataminx.dts.common.jms.DtsMessagePayloadTransformer;
import org.dataminx.dts.common.util.CredentialStoreImpl;
import org.dataminx.dts.common.validator.DtsJobDefinitionValidator;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.channel.MessageChannelTemplate;
import org.springframework.oxm.Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.xml.transform.StringResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for {@link DtsMessageConverter}.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
@ContextConfiguration(locations = {"/org/dataminx/dts/wn/integration-context.xml"})
@Test(groups = {"unit-test"})
public class DtsMessageConverterTest {

    private DtsJobFactory mockJobFactory;

    private DtsMessagePayloadTransformer mockPayloadTransformer;

    private Marshaller mockMarshaller;

    private DtsMessageConverter mConverter;

    private TextMessage mockMessage;

    private MessageChannelTemplate mockChannelTemplate;

    private DtsJobDefinitionValidator mockDtsJobDefinitionValidator;

    @BeforeClass
    public void init() {
        mockJobFactory = mock(DtsJobFactory.class);
        mockPayloadTransformer = mock(DtsMessagePayloadTransformer.class);
        mockMarshaller = mock(Marshaller.class);
        mockMessage = mock(TextMessage.class);
        mockChannelTemplate = mock(MessageChannelTemplate.class);
        mockDtsJobDefinitionValidator = mock(DtsJobDefinitionValidator.class);

    }

    /**
     * Test method for {@link org.dataminx.dts.wn.jms.DtsMessageConverter#fromMessage(javax.jms.Message)}.
     */
    /*
    @Test
    public void testFromMessage() throws Exception {
        mConverter = new DtsMessageConverter();
        mConverter.setJobFactory(mockJobFactory);
        mConverter.setMarshaller(mockMarshaller);
        mConverter.setChannelTemplate(mockChannelTemplate);
        mConverter.setTransformer(mockPayloadTransformer);
        mConverter.setDtsJobDefinitionValidator(mockDtsJobDefinitionValidator);

        final File f = new ClassPathResource(
            "/org/dataminx/dts/wn/util/minx-dts" + getTestFilePostfix()
                + ".xml").getFile();

        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        final SubmitJobRequestDocument jobRequest = SubmitJobRequestDocument.Factory
            .parse(f);

        final DtsFileTransferJob dtsJob = new DtsFileTransferJob(jobId, jobTag,
            jobRequest, null, new CredentialStoreImpl());
        when(mockPayloadTransformer.transformPayload(anyObject())).thenReturn(
            jobRequest);
        when(mockJobFactory.createJob(anyString(), anyString(), anyObject()))
            .thenReturn(dtsJob);
        when(mockMessage.getText()).thenReturn(jobRequest.xmlText());

        final Object result = mConverter.fromMessage(mockMessage);
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof JobLaunchRequest);
        Assert.assertSame(((JobLaunchRequest) result).getJob(), dtsJob);
    }
    */

    /**
     * Test method for {@link org.dataminx.dts.wn.jms.DtsMessageConverter#fromMessage(javax.jms.Message)}.
     * The implemetation of the fromMessage method will not return a JobLaunchRequest but call DtsJobLauncher.run() method diretly.
     * /

    /**
     * Test method for
     * {@link org.dataminx.dts.wn.jms.DtsMessageConverter#toMessage(java.lang.Object, javax.jms.Session)}.
     */
    @Test(enabled = false)
    public void testToMessage() throws Exception {
        mConverter = new DtsMessageConverter();
        mConverter.setMarshaller(mockMarshaller);
        final Session mockSession = mock(Session.class);

        final File f = new ClassPathResource(
            "/org/dataminx/dts/wn/util/minx-dts" + getTestFilePostfix()
                + ".xml").getFile();

        final SubmitJobRequestDocument jobRequest = SubmitJobRequestDocument.Factory
            .parse(f);

        when(mockMarshaller.supports((Class) anyObject())).thenReturn(true);

        // modify the internal state when the marshal() method is invoked
        doAnswer(new Answer() {
            public Object answer(final InvocationOnMock invocation) {
                final Object[] args = invocation.getArguments();
                final XmlObject xmlObject = (XmlObject) args[0];
                final StringResult result = (StringResult) args[1];
                Assert.assertTrue(false, "false");
                Assert.assertNotNull(result, "result is null here");
                final StringWriter writer = new StringWriter();
                writer.write(xmlObject.xmlText());
                result.setWriter(writer);
                return null;
            }
        }).when(mockMarshaller).marshal(jobRequest, null);

        when(mockSession.createTextMessage(jobRequest.xmlText())).thenReturn(
            mockMessage);

        final Message result = mConverter.toMessage(jobRequest, mockSession);

        // TODO: figure out how to get the returned result not null
    }
}
