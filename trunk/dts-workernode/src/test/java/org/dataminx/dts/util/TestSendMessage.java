/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.util;

import static org.apache.commons.lang.SystemUtils.JAVA_IO_TMPDIR;
import static org.apache.commons.lang.SystemUtils.LINE_SEPARATOR;

import java.io.File;
import java.util.UUID;
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
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.xpath.XPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;
import org.w3c.dom.Document;

/**
 * Test that a DTS job is launched when a JMS message is posted on the DTS Job Submission queue.
 *
 * @author Alex Arana
 */
@ContextConfiguration(locations = { "/test-context.xml", "/activemq/jms-context.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class TestSendMessage {
    @Autowired
    private JobSubmitQueueSender mJmsQueueSender;

    @Autowired
    private Jaxb2Marshaller mMarshaller;

    @Test
    public void submitDtsJobAsDocument() throws Exception {
        File file = new ClassPathResource("minx-dts.xml").getFile();
        final SAXBuilder builder = new SAXBuilder();
        final org.jdom.Document dtsJob = builder.build(file);

        XPath xpathEvaluator = XPath.newInstance(
            "/pre:submitJobRequest/pre:JobDefinition/pre:JobDescription/pre:JobIdentification/pre:JobName");
        xpathEvaluator.addNamespace("pre", "http://schemas.dataminx.org/dts/2009/05/dts");
        Element node = (Element) xpathEvaluator.selectSingleNode(dtsJob);
        Assert.notNull(node);
        node.setText(generateNewJobId());

        xpathEvaluator = XPath.newInstance(
            "/minx:submitJobRequest/minx:JobDefinition/minx:JobDescription/minx:DataTransfer/minx:Target/minx:URI");
        xpathEvaluator.addNamespace("minx", "http://schemas.dataminx.org/dts/2009/05/dts");
        node = (Element) xpathEvaluator.selectSingleNode(dtsJob);
        Assert.notNull(node);
        file = new File(JAVA_IO_TMPDIR, "DataMINX_Logo.jpg");
        node.setText(file.getAbsolutePath());

        //logger.info(client.submitJob(dtsJob));
        final DOMOutputter outputter = new DOMOutputter();
        mJmsQueueSender.doSend(outputter.output(dtsJob));
    }

    @Test
    public void submitDtsJobAsText() throws Exception {
        final ObjectFactory factory = new ObjectFactory();

        final SourceTargetType source = factory.createSourceTargetType();
        source.setURI("http://wiki.arcs.org.au/pub/DataMINX/DataMINX/minnie_on_the_run.jpg");

        final SourceTargetType target = factory.createSourceTargetType();
        final File file = new File(JAVA_IO_TMPDIR, "DataMINX_Logo2.jpg");
        target.setURI(file.getAbsolutePath());

        final TransferRequirementsType transferRequirements = factory.createTransferRequirementsType();
        transferRequirements.setMaxAttempts(0L);
        transferRequirements.setCreationFlag(CreationFlagEnumeration.OVERWRITE);

        final DataTransferType dataTransfer = factory.createDataTransferType();
        dataTransfer.setTransferRequirements(transferRequirements);
        dataTransfer.setSource(source);
        dataTransfer.setTarget(target);

        final JobIdentificationType jobIdentification = factory.createJobIdentificationType();
        jobIdentification.setJobName(generateNewJobId());
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
        logger.debug("DTS Job Request:" + LINE_SEPARATOR + dtsJobRequest);
        mJmsQueueSender.doSend(dtsJobRequest);
    }

    private String generateNewJobId() {
        return "DTSJob_" + UUID.randomUUID();
    }
}
