package org.dataminx.dts.batch;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.DataCopyActivityDocument;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * An integration test which submits a real DTS job as a batch job. This
 * requires a working database so Spring Batch can store the job's details onto
 * it.
 * 
 * @author Gerson Galang
 * @author David Meredith (modifications)
 */
@ContextConfiguration(locations = {
    "/org/dataminx/dts/batch/client-context.xml",
    "/org/dataminx/dts/batch/batch-context.xml"})
public class QuickBulkCopyJobIntegrationTest extends
    AbstractTestNGSpringContextTests {


    private DataCopyActivityDocument mDtsJob;

    private static final Log LOGGER = LogFactory
        .getLog(QuickBulkCopyJobIntegrationTest.class);

    @Autowired
    private DtsJobLauncher mJobLauncher;

    public QuickBulkCopyJobIntegrationTest(){
        TestUtils.assertTestEnvironmentOk();
    }

    @BeforeClass
    public void init() throws Exception {
        //System.setProperty("dataminx.dir", "/home/djm76/.dataminx");
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/testjob"+ getTestFilePostfix() + ".xml").getFile();
        //mDtsJob = JobDefinitionDocument.Factory.parse(f);
        final File f = new ClassPathResource("/org/dataminx/dts/batch/testjob.xml").getFile();
        mDtsJob = TestUtils.getTestDataCopyActivityDocument(f);
        
        assertNotNull(mDtsJob);
    }

    @Test
    public void testRunJob() throws Exception {
        final String jobId = UUID.randomUUID().toString();
        final JobExecution jobExecution = mJobLauncher.run(jobId, mDtsJob);
        assertTrue(jobExecution.getStatus() == BatchStatus.COMPLETED);
            //|| jobExecution.getStatus() == BatchStatus.FAILED);
        assertEquals(jobId, jobExecution.getJobInstance().getJobName());
    }


}
