package org.dataminx.dts.broker;

import java.util.UUID;
import org.dataminx.dts.broker.util.JobQueueSender;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class TestBrokeringDtsJobMessage extends AbstractTestNGSpringContextTests{

    @Autowired
    @Qualifier("mQueueSender")
    private JobQueueSender mQueueSender;

    @Test
    public void submitDtsJobAsText() throws Exception {
        Resource xml = new ClassPathResource("/job.xml");
        Resource anstoXml1 = new ClassPathResource("/job-ansto-1.xml");
        Resource anstoXml2 = new ClassPathResource("/job-ansto-2.xml");
        Resource asfXml1 = new ClassPathResource("/job-asf-1.xml");
        Resource asfXml2 = new ClassPathResource("/job-asf-2.xml");

        final SubmitJobRequestDocument root = SubmitJobRequestDocument.Factory.parse(xml.getInputStream());;
        final SubmitJobRequestDocument root1 = SubmitJobRequestDocument.Factory.parse(anstoXml1.getInputStream());;
        final SubmitJobRequestDocument root2 = SubmitJobRequestDocument.Factory.parse(anstoXml2.getInputStream());;
        final SubmitJobRequestDocument root3 = SubmitJobRequestDocument.Factory.parse(asfXml1.getInputStream());;
        final SubmitJobRequestDocument root4 = SubmitJobRequestDocument.Factory.parse(asfXml2.getInputStream());;

        final String dtsJobId = generateNewJobId();

        final Logger logger = LoggerFactory.getLogger(getClass());
//        if (logger.isDebugEnabled()) {
//            final String dtsJobRequest = root.xmlText();
//            logger.debug(String.format("submitDtsJobAsText ['%s']:%s%s", dtsJobId, LINE_SEPARATOR, dtsJobRequest));
//        }

        mQueueSender.doSend(dtsJobId+"ANSTO-delayed", "ANSTO", root.xmlText());
        mQueueSender.doSend(dtsJobId+"ANSTO-1", "ANSTO", root1.xmlText());
        mQueueSender.doSend(dtsJobId+"ANSTO-2", "ANSTO", root2.xmlText());
        mQueueSender.doSend(dtsJobId+"ASF-1", "ASF", root3.xmlText());
        mQueueSender.doSend(dtsJobId+"ASF-2", "ASF", root4.xmlText());
    }

    private String generateNewJobId() {
        return "DTSTestJob_" + UUID.randomUUID();
    }

}
