/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.util;

import static org.apache.commons.lang.SystemUtils.LINE_SEPARATOR;
import static org.dataminx.dts.common.XmlUtils.documentToString;

import java.io.File;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMResult;
import org.dataminx.dts.common.XmlUtils;
import org.dataminx.dts.jms.JobSubmitQueueSender;
import org.dataminx.schemas.dts._2009._05.dts.CreationFlagEnumeration;
import org.dataminx.schemas.dts._2009._05.dts.DataTransferType;
import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;
import org.dataminx.schemas.dts._2009._05.dts.JobDescriptionType;
import org.dataminx.schemas.dts._2009._05.dts.JobIdentificationType;
import org.dataminx.schemas.dts._2009._05.dts.ObjectFactory;
import org.dataminx.schemas.dts._2009._05.dts.SourceTargetType;
import org.dataminx.schemas.dts._2009._05.dts.SubmitJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.TransferRequirementsType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

/**
 * Test that a DTS job is launched when a JMS message is posted on the DTS Job Submission queue.
 *
 * @author Alex Arana
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class TestProcessDtsJobMessage {
    @Autowired
    private JobSubmitQueueSender mJmsQueueSender;

    @Autowired
    private Jaxb2Marshaller mMarshaller;

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
        final ObjectFactory factory = new ObjectFactory();

        final SourceTargetType source = factory.createSourceTargetType();
        source.setURI("http://wiki.arcs.org.au/pub/DataMINX/DataMINX/minnie_on_the_run.jpg");

        final SourceTargetType target = factory.createSourceTargetType();
        target.setURI(generateTemporaryFilename("DataMINX_Logo2.jpg"));

        final TransferRequirementsType transferRequirements = factory.createTransferRequirementsType();
        transferRequirements.setMaxAttempts(0L);
        transferRequirements.setCreationFlag(CreationFlagEnumeration.OVERWRITE);

        final DataTransferType dataTransfer = factory.createDataTransferType();
        dataTransfer.setTransferRequirements(transferRequirements);
        dataTransfer.setSource(source);
        dataTransfer.setTarget(target);

        final String dtsJobId = generateNewJobId();
        final JobIdentificationType jobIdentification = factory.createJobIdentificationType();
        jobIdentification.setJobName(dtsJobId);
        jobIdentification.setDescription("Copies the DataMINX Logo from a HTTP source to a local folder");

        final JobDescriptionType jobDescription = factory.createJobDescriptionType();
        jobDescription.setJobIdentification(jobIdentification);
        jobDescription.getDataTransfer().add(dataTransfer);

        final JobDefinitionType jobDefinition = factory.createJobDefinitionType();
        jobDefinition.setJobDescription(jobDescription);

        final SubmitJobRequest submitJobRequest = factory.createSubmitJobRequest();
        submitJobRequest.setJobDefinition(jobDefinition);

        final DOMResult result = new DOMResult();
        mMarshaller.marshal(submitJobRequest, result);
        final Document document = (Document) result.getNode();

        final Logger logger = LoggerFactory.getLogger(getClass());
        final String dtsJobRequest = XmlUtils.documentToString(document);
        logger.debug(String.format("submitDtsJobAsText ['%s']:%s%s", dtsJobId, LINE_SEPARATOR, dtsJobRequest));
        mJmsQueueSender.doSend(dtsJobId, dtsJobRequest);
    }

    private String generateTemporaryFilename(final String filename) {
        return "tmp://DataMINX/" + filename;
    }

    private String generateNewJobId() {
        return "DTSJob_" + UUID.randomUUID();
    }
}
