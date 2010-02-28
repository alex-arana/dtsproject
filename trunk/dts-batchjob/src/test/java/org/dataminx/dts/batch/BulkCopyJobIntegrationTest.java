package org.dataminx.dts.batch;

import java.io.File;
import java.util.UUID;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

@ContextConfiguration(locations = { "/org/dataminx/dts/batch/client-context.xml",
        "/org/dataminx/dts/batch/batch-context.xml" })
@Test(groups = { "integration-test" })
public class BulkCopyJobIntegrationTest extends AbstractTestNGSpringContextTests {

    private JobDefinitionDocument mDtsJob;

    private static final Log LOGGER = LogFactory.getLog(BulkCopyJobIntegrationTest.class);

    @Autowired
    private DtsJobLauncher mJobLauncher;

    @BeforeTest
    public void init() throws Exception {
        final File f = new ClassPathResource("/org/dataminx/dts/batch/testjob.xml").getFile();
        mDtsJob = JobDefinitionDocument.Factory.parse(f);
        Assert.assertNotNull(mDtsJob);
    }

    @Test
    public void testRunJob() throws Exception {
        final String jobId = UUID.randomUUID().toString();
        final JobExecution jobExecution = mJobLauncher.run(jobId, mDtsJob);
        Assert.assertTrue(jobExecution.getStatus() == BatchStatus.COMPLETED
                || jobExecution.getStatus() == BatchStatus.FAILED);
        Assert.assertEquals(jobId, jobExecution.getJobInstance().getJobName());
    }

}
