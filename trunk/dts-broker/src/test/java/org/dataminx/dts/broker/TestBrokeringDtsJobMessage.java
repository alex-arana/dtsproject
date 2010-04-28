package org.dataminx.dts.broker;

import java.util.UUID;
import org.dataminx.dts.broker.util.JobQueueSender;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
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
        final SubmitJobRequestDocument root = SubmitJobRequestDocument.Factory.parse(xml.getInputStream());;
        final String dtsJobId = generateNewJobId();
        mQueueSender.doSend(dtsJobId+"ANSTO", "ANSTO", root.xmlText());

    }

    private String generateNewJobId() {
        return "DTSTestJob_" + UUID.randomUUID();
    }

}
