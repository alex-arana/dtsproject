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
package org.dataminx.dts.wn.util;

import static org.apache.commons.lang.SystemUtils.LINE_SEPARATOR;
import static org.dataminx.dts.common.xml.XmlUtils.documentToString;

import java.io.File;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.dataminx.dts.wn.jms.JobSubmitQueueSender;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxSourceTargetType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxTransferRequirementsType;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.CreationFlagEnumeration;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobIdentificationType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

/**
 * Test that a DTS job is launched when a JMS message is posted on the DTS Job
 * Submission queue.
 * 
 * @author Alex Arana
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class TestProcessDtsJobMessage {
    @Autowired
    private JobSubmitQueueSender mJmsQueueSender;

    @Test
    public void submitDtsJobAsDocument() throws Exception {
        final File file = new ClassPathResource("minx-dts.xml", getClass()).getFile();
        final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        docFactory.setNamespaceAware(true);
        final DocumentBuilder builder = docFactory.newDocumentBuilder();
        final Document dtsJob = builder.parse(file);
        final Logger logger = LoggerFactory.getLogger(getClass());
        logger.debug(String.format("submitDtsJobAsDocument:\n%s", documentToString(dtsJob)));

        //logger.info(client.submitJob(dtsJob));
        mJmsQueueSender.doSend(generateNewJobId(), dtsJob);
    }

    @Test
    public void submitDtsJobAsText() throws Exception {
        final SubmitJobRequestDocument root = SubmitJobRequestDocument.Factory.newInstance();
        final SubmitJobRequest submitJobRequest = root.addNewSubmitJobRequest();
        final JobDefinitionType jobDefinition = submitJobRequest.addNewJobDefinition();

        final MinxJobDescriptionType jobDescription = MinxJobDescriptionType.Factory.newInstance();
        final DataTransferType dataTransfer = jobDescription.addNewDataTransfer();
        final MinxSourceTargetType source = MinxSourceTargetType.Factory.newInstance();
        source.setURI("http://wiki.arcs.org.au/pub/DataMINX/DataMINX/minnie_on_the_run.jpg");
        final MinxSourceTargetType target = MinxSourceTargetType.Factory.newInstance();
        target.setURI(generateTemporaryFilename("DataMINX_Logo2.jpg"));

        final MinxTransferRequirementsType transferRequirements = MinxTransferRequirementsType.Factory.newInstance();
        transferRequirements.setMaxAttempts(0L);
        transferRequirements.setCreationFlag(CreationFlagEnumeration.OVERWRITE);

        dataTransfer.setTransferRequirements(transferRequirements);
        dataTransfer.setSource(source);
        dataTransfer.setTarget(target);

        final JobIdentificationType jobIdentification = jobDescription.addNewJobIdentification();
        final String dtsJobId = generateNewJobId();
        jobIdentification.setJobName(dtsJobId);
        jobIdentification.setDescription("Copies the DataMINX Logo from a HTTP source to a local folder");
        jobDescription.setJobIdentification(jobIdentification);
        jobDescription.setDataTransferArray(new DataTransferType[] { dataTransfer });
        jobDefinition.setJobDescription(jobDescription);

        final Logger logger = LoggerFactory.getLogger(getClass());
        if (logger.isDebugEnabled()) {
            final String dtsJobRequest = root.xmlText();
            logger.debug(String.format("submitDtsJobAsText ['%s']:%s%s", dtsJobId, LINE_SEPARATOR, dtsJobRequest));
        }

        mJmsQueueSender.doSend(dtsJobId, root.xmlText());
    }

    private String generateTemporaryFilename(final String filename) {
        return "tmp://DataMINX/" + filename;
    }

    private String generateNewJobId() {
        return "DTSJob_" + UUID.randomUUID();
    }
}
