package org.dataminx.dts.batch;

import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_DATA_TRANSFER_STEP_KEY;
import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_JOB_DETAILS;
import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_SUBMIT_JOB_REQUEST_KEY;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.batch.common.DtsBatchJobConstants;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

/**
 * An integration test which submits a real DTS job as a batch job. This requires a working database so Spring Batch can
 * store the job's details onto it.
 * 
 * @author Gerson Galang
 */
@ContextConfiguration(locations = {
        "/org/dataminx/dts/batch/client-context.xml",
        "/org/dataminx/dts/batch/batch-context.xml"})
@Test(groups = {"integration-test"})
public class BulkCopyJobIntegrationTest extends
        AbstractTestNGSpringContextTests {

    private JobDefinitionDocument mDtsJob;

    private static final Log LOGGER = LogFactory
            .getLog(BulkCopyJobIntegrationTest.class);

    @Autowired
    private DtsJobLauncher mJobLauncher;

    @Autowired
    private JobExplorer mJobExplorer;

    private boolean doesJobStepExists(final String jobResourceKey) {
        final File jobStepDirectory = new File(System
                .getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY));
        final File[] jobStepFiles = jobStepDirectory
                .listFiles(new FilenameFilter() {
                    public boolean accept(final File dir, final String name) {
                        if (name.startsWith(jobResourceKey)
                                && name.endsWith("dts")) {
                            return true;
                        }
                        return false;
                    }
                });
        if ((jobStepFiles != null) && (jobStepFiles.length > 0)) {
            return true;
        }
        return false;
    }

    @Test(enabled = false)
    public void testExistenceOfPropertiesFromExecutionContextAfterJobFailed()
            throws Exception {
        final File f = new ClassPathResource(
                "/org/dataminx/dts/batch/failedjob.xml").getFile();
        mDtsJob = JobDefinitionDocument.Factory.parse(f);
        assertNotNull(mDtsJob);

        final String jobId = UUID.randomUUID().toString();
        final JobExecution jobExecution = mJobLauncher.run(jobId, mDtsJob);
        assertTrue(jobExecution.getStatus() == BatchStatus.FAILED);

        // we're checking for null as only a successful jobscoping step would write the DTS_JOB_DETAILS
        // to the JobExecutionContext. since this test fails on the job scoping step, we can safely
        // assume that the DTS_JOB_DETAILS hasn't been saved to the DB.
        assertNull(jobExecution.getExecutionContext().get(DTS_JOB_DETAILS));

        assertNotNull(jobExecution.getExecutionContext().get(
                DTS_SUBMIT_JOB_REQUEST_KEY));
    }

    @Test(enabled = false)
    public void testNonExistenceOfPropertiesFromExecutionContextAfterJobFinishedSuccessfully()
            throws Exception {
        final File f = new ClassPathResource(
                "/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        mDtsJob = JobDefinitionDocument.Factory.parse(f);
        assertNotNull(mDtsJob);

        final String jobId = UUID.randomUUID().toString();
        final JobExecution jobExecution = mJobLauncher.run(jobId, mDtsJob);
        assertTrue(jobExecution.getStatus() == BatchStatus.COMPLETED);

        final List<JobExecution> jobExecutions = mJobExplorer
                .getJobExecutions(jobExecution.getJobInstance());

        for (final JobExecution jobEx : jobExecutions) {
            assertNull(jobEx.getExecutionContext().get(DTS_JOB_DETAILS));
            assertNull(jobEx.getExecutionContext().get(
                    DTS_SUBMIT_JOB_REQUEST_KEY));

            for (final StepExecution stepExecution : jobEx.getStepExecutions()) {
                assertNull(stepExecution.getExecutionContext().get(
                        DTS_DATA_TRANSFER_STEP_KEY));
            }
        }
        assertEquals(doesJobStepExists(jobId), false);
    }

}
