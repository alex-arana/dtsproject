/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.util;

import static org.apache.commons.lang.SystemUtils.FILE_SEPARATOR;
import static org.apache.commons.lang.SystemUtils.JAVA_IO_TMPDIR;
import static org.apache.commons.lang.SystemUtils.LINE_SEPARATOR;

import java.io.File;
import java.util.List;
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
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
public class TestProcessDtsJobMessage {
    @Autowired
    private JobSubmitQueueSender mJmsQueueSender;

    @Autowired
    private Jaxb2Marshaller mMarshaller;

    @Test
    @SuppressWarnings("unchecked")
    public void submitDtsJobAsDocument() throws Exception {
        final File file = new ClassPathResource("minx-dts.xml").getFile();
        final SAXBuilder builder = new SAXBuilder();
        final org.jdom.Document dtsJob = builder.build(file);

        XPath xpathEvaluator = createXPathInstance(
            "/minx:submitJobRequest/minx:JobDefinition/minx:JobDescription/minx:JobIdentification/minx:JobName");
        Element node = (Element) xpathEvaluator.selectSingleNode(dtsJob);
        Assert.notNull(node, "unable to select //JobName");
        final String dtsJobId = generateNewJobId();
        node.setText(dtsJobId);

        xpathEvaluator = createXPathInstance(
            "/minx:submitJobRequest/minx:JobDefinition/minx:JobDescription/minx:DataTransfer");
        final List<Element> nodes = xpathEvaluator.selectNodes(dtsJob);
        Assert.notEmpty(nodes, "unable to select //DataTransfer");

        // replace the placeholders in the XML template
        for (final Element element : nodes) {
            final Namespace namespace = Namespace.getNamespace("minx", "http://schemas.dataminx.org/dts/2009/05/dts");
            final Element sourceNode = element.getChild("Source", namespace).getChild("URI", namespace);
            final Element targetNode = element.getChild("Target", namespace).getChild("URI", namespace);
            final Resource source = new UrlResource(sourceNode.getTextTrim());
            final File target = generateTemporaryFile(source.getFilename());
            targetNode.setText(target.getAbsolutePath());
        }

        final Logger logger = LoggerFactory.getLogger(getClass());
        if (logger.isDebugEnabled()) {
            final XMLOutputter formatter = new XMLOutputter(Format.getPrettyFormat());
            logger.debug(String.format("submitDtsJobAsDocument [id='%s']:%s%s",
                dtsJobId, LINE_SEPARATOR, formatter.outputString(dtsJob)));
        }

        //logger.info(client.submitJob(dtsJob));
        final DOMOutputter outputter = new DOMOutputter();
        mJmsQueueSender.doSend(dtsJobId, outputter.output(dtsJob));
    }

    @Test
    public void submitDtsJobAsText() throws Exception {
        final ObjectFactory factory = new ObjectFactory();

        final SourceTargetType source = factory.createSourceTargetType();
        source.setURI("http://wiki.arcs.org.au/pub/DataMINX/DataMINX/minnie_on_the_run.jpg");

        final SourceTargetType target = factory.createSourceTargetType();
        final File file = generateTemporaryFile("DataMINX_Logo2.jpg");
        target.setURI(file.getAbsolutePath());

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
        logger.debug(String.format("submitDtsJobAsText ['%s']:%s%s",
            dtsJobId, LINE_SEPARATOR, dtsJobRequest));
        mJmsQueueSender.doSend(dtsJobId, dtsJobRequest);
    }

    private XPath createXPathInstance(final String selector) throws JDOMException {
        final XPath xpath = XPath.newInstance(selector);
        xpath.addNamespace("minx", "http://schemas.dataminx.org/dts/2009/05/dts");
        return xpath;
    }

    private File generateTemporaryFile(final String filename) {
        String temporaryDirectory = JAVA_IO_TMPDIR;
        if (!temporaryDirectory.endsWith(FILE_SEPARATOR)) {
            temporaryDirectory += FILE_SEPARATOR;
        }
        temporaryDirectory += "DataMINX";
        return new File(temporaryDirectory, filename);
    }

    private String generateNewJobId() {
        return "DTSJob_" + UUID.randomUUID();
    }
}
