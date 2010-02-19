package org.dataminx.dts.broker;

import static org.apache.commons.lang.SystemUtils.LINE_SEPARATOR;

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

        final Logger logger = LoggerFactory.getLogger(getClass());
        if (logger.isDebugEnabled()) {
            final String dtsJobRequest = root.xmlText();
            logger.debug(String.format("submitDtsJobAsText ['%s']:%s%s", dtsJobId, LINE_SEPARATOR, dtsJobRequest));
        }

        mQueueSender.doSend(dtsJobId, root.xmlText());
    }

    private String generateNewJobId() {
        return "DTSJob_" + UUID.randomUUID();
    }

}
