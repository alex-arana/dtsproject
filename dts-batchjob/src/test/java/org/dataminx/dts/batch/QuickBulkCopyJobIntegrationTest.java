package org.dataminx.dts.batch;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.common.DtsConstants;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
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

    public QuickBulkCopyJobIntegrationTest(){
        // before upgrading to maven-surefire-plugin version 2.5, the following
        // system properties had to be set. Surefire 2.5 can accept any value from
        // Maven's properties that can be converted to String value !!
        // can therefore specify the -Ddataminx.dir=/path/to/dataminx/dir on the
        // command line when running tests
        if (!System.getProperties().containsKey(DtsConstants.DATAMINX_CONFIGURATION_KEY)) {
            throw new IllegalStateException("DataMINX configuration directory is not set for tests - please specify");
        }
        File configdir = new File(System.getProperty(DtsConstants.DATAMINX_CONFIGURATION_KEY));
        if (!configdir.exists() || !configdir.isDirectory() || !configdir.canWrite()) {
            throw new IllegalStateException(
                    String.format(" Invalid DataMINX configuration folder: '%s'.  Check your configuration",
                    configdir.getAbsolutePath()));
        }
        //System.setProperty(DtsConstants.DATAMINX_CONFIGURATION_KEY, "/home/djm76/.dataminxes");             
    }


    private JobDefinitionDocument mDtsJob;

    private static final Log LOGGER = LogFactory
        .getLog(QuickBulkCopyJobIntegrationTest.class);

    @Autowired
    private DtsJobLauncher mJobLauncher;

    @BeforeClass
    public void init() throws Exception {
        //System.setProperty("dataminx.dir", "/home/djm76/.dataminx");
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/testjob"+ getTestFilePostfix() + ".xml").getFile();
        //mDtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/testjob.xml").getFile();
        mDtsJob = TestUtils.getTestJobDefinitionDocument(f);
        
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
