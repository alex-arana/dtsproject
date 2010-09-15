package org.dataminx.dts.broker;

import static org.apache.commons.lang.SystemUtils.LINE_SEPARATOR;
import static org.dataminx.dts.common.broker.DtsBrokerConstants.ROUTING_HEADER_KEY;
import static org.dataminx.dts.common.util.TestFileChooser.getTestFilePostfix;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.dataminx.dts.common.jms.JobQueueSender;
/*
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxSourceTargetType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxTransferRequirementsType;
*/
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
/*
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.CreationFlagEnumeration;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobIdentificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
*/
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/**
 * Integration tests of brokering a job submission message.
 * @author hnguyen
 */
@ContextConfiguration
public class BrokeringDtsJobMessageIntegrationTest extends
    AbstractTestNGSpringContextTests {

    @Autowired
    @Qualifier("mQueueSender")
    private JobQueueSender mQueueSender;

    @Test
    public void submitDtsJobAsText() throws Exception {
        final Resource xml = new ClassPathResource("/job"
            + getTestFilePostfix() + ".xml");
        final SubmitJobRequestDocument root = SubmitJobRequestDocument.Factory
            .parse(xml.getInputStream());       
        final String dtsJobId = generateNewJobId();
        final Map<String, Object> jmsParameterMap = new HashMap<String, Object>();
        jmsParameterMap.put(ROUTING_HEADER_KEY, "ANSTO");
        mQueueSender
            .doSend(dtsJobId + "ANSTO", jmsParameterMap, root.xmlText());

    }
/*
    @Test
    public void submitConstructedJob() throws Exception {
        final SubmitJobRequestDocument root = SubmitJobRequestDocument.Factory.newInstance();
        final SubmitJobRequest submitJobRequest = root.addNewSubmitJobRequest();
        final JobDefinitionType jobDefinition = submitJobRequest.addNewJobDefinition();

        final MinxJobDescriptionType jobDescription = MinxJobDescriptionType.Factory.newInstance();
        final DataTransferType dataTransfer = jobDescription.addNewDataTransfer();
        final MinxSourceTargetType source = MinxSourceTargetType.Factory.newInstance();
        source.setURI("http://wiki.arcs.org.au/pub/DataMINX/DataMINX/minnie_on_the_run.jpg");
        final MinxSourceTargetType target = MinxSourceTargetType.Factory.newInstance();
        target.setURI("F:/DataMINX_Logo2.jpg");

        final MinxTransferRequirementsType transferRequirements =
            MinxTransferRequirementsType.Factory.newInstance();
        transferRequirements.setMaxAttempts(0L);
        transferRequirements.setCreationFlag(CreationFlagEnumeration.OVERWRITE);

        jobDescription.setTransferRequirements(transferRequirements);
        dataTransfer.setSource(source);
        dataTransfer.setTarget(target);

        final JobIdentificationType jobIdentification = jobDescription.addNewJobIdentification();
        final String dtsJobId = generateNewJobId();
        jobIdentification.setJobName(dtsJobId);
        jobIdentification
            .setDescription("Copies the DataMINX Logo from a HTTP source to a local folder");
        jobDescription.setJobIdentification(jobIdentification);
        jobDescription.setDataTransferArray(new DataTransferType[] {dataTransfer});
        jobDefinition.setJobDescription(jobDescription);

        final Logger logger = LoggerFactory.getLogger(getClass());
        if (logger.isDebugEnabled()) {
            final String dtsJobRequest = root.xmlText();
            logger.debug(String.format("submitDtsJobAsText ['%s']:%s%s", dtsJobId, LINE_SEPARATOR,
                dtsJobRequest));
        }
        final Map<String, Object> jmsParameterMap = new HashMap<String, Object>();
        jmsParameterMap.put(ROUTING_HEADER_KEY, "ANSTO");
        mQueueSender.doSend(dtsJobId + "ANSTO", jmsParameterMap, root.xmlText());
    }
*/
    private String generateNewJobId() {
        return "DTSTestJob_" + UUID.randomUUID();
    }

}
