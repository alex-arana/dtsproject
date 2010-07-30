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
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
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
 * @author David Meredith (modifications)
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

    @Autowired
    private JobOperator mJobOperator;

    private boolean doesJobStepExists(final String jobResourceKey) {
        final File jobStepDirectory = new File(System
            .getProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY));
        final File[] jobStepFiles = jobStepDirectory
            .listFiles(new FilenameFilter() {
                public boolean accept(final File dir, final String name) {
                    if (/*name.startsWith(jobResourceKey) &&*/ name.endsWith("dts")) {
                        return true;
                    }
                    return false;
                }
            });
        if (jobStepFiles != null && jobStepFiles.length > 0) {
            return true;
        }
        return false;
    }

    @Test
    public void testExistenceOfPropertiesFromExecutionContextAfterJobFailed()
        throws Exception {
        final File f = new ClassPathResource(
            "/org/dataminx/dts/batch/failedjob.xml").getFile();
        //mDtsJob = JobDefinitionDocument.Factory.parse(f);
        String docString = TestUtils.readFileAsString(f.getAbsolutePath());
        String homeDir = System.getProperty("user.home").replaceAll("\\\\", "/");
        docString = docString.replaceAll("@home.dir.replacement@", homeDir);
        mDtsJob = JobDefinitionDocument.Factory.parse(docString);
        assertNotNull(mDtsJob);

        final String jobId = UUID.randomUUID().toString();
        final JobExecution jobExecution = mJobLauncher.run(jobId, mDtsJob);
        assertTrue(jobExecution.getStatus() == BatchStatus.FAILED);

        // we're checking for null as only a successful jobscoping step would write the DTS_JOB_DETAILS
        // to the JobExecutionContext. since this test fails on the job scoping step, we can safely
        // assume that the DTS_JOB_DETAILS hasn't been saved to the DB.
        assertNull(jobExecution.getExecutionContext().get(DTS_JOB_DETAILS));

        assertNotNull(jobExecution.getExecutionContext().get(DTS_SUBMIT_JOB_REQUEST_KEY));
    }

    @Test
    public void testNonExistenceOfPropertiesFromExecutionContextAfterJobFinishedSuccessfully()
        throws Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()+ ".xml").getFile();
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();

        //mDtsJob = JobDefinitionDocument.Factory.parse(f);
        String docString = TestUtils.readFileAsString(f.getAbsolutePath());
        String homeDir = System.getProperty("user.home").replaceAll("\\\\", "/");
        docString = docString.replaceAll("@home.dir.replacement@", homeDir);
        mDtsJob = JobDefinitionDocument.Factory.parse(docString);

        assertNotNull(mDtsJob);

        final String jobId = UUID.randomUUID().toString();
        final JobExecution jobExecution = mJobLauncher.run(jobId, mDtsJob);
        assertTrue(jobExecution.getStatus() == BatchStatus.COMPLETED);


        assertNull(jobExecution.getExecutionContext().get(DTS_JOB_DETAILS));

        final List<JobExecution> jobExecutions = mJobExplorer
            .getJobExecutions(jobExecution.getJobInstance());

        for (final JobExecution jobEx : jobExecutions) {
            assertNull(jobEx.getExecutionContext().get(DTS_JOB_DETAILS));
            assertNull(jobEx.getExecutionContext().get(DTS_SUBMIT_JOB_REQUEST_KEY));

            for (final StepExecution stepExecution : jobEx.getStepExecutions()) {
                assertNull(stepExecution.getExecutionContext().get(
                    DTS_DATA_TRANSFER_STEP_KEY));
            }
        }

        // assert that the jobSteps have been deleted ok after job finished.
        // TODO: need to get a handle on the job tag rather than jobId and so
        // may have to overload a new launch method in order to pass the tag
        assertEquals(doesJobStepExists(jobId), false);
    }

    // TODO: need to enable this test later on and think of a way to make it
    // access a file on the deployment machien that's about 80MB in size which
    // this test will use for the suspend resume capability
    @Test(enabled = false)
    public void testSuspendResume() throws Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-suspend" + getTestFilePostfix()+ ".xml").getFile();
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-suspend.xml").getFile();

        //mDtsJob = JobDefinitionDocument.Factory.parse(f);
        String docString = TestUtils.readFileAsString(f.getAbsolutePath());
        String homeDir = System.getProperty("user.home").replaceAll("\\\\", "/");
        docString = docString.replaceAll("@home.dir.replacement@", homeDir);
        mDtsJob = JobDefinitionDocument.Factory.parse(docString);

        assertNotNull(mDtsJob);

        final String jobId = UUID.randomUUID().toString();

        JobExecution jobExecution = null;

        final Thread jobLauncherThread = new Thread() {
            @Override
            public void run() {

                try {
                    mJobLauncher.run(jobId, mDtsJob);
                }
                catch (final JobExecutionAlreadyRunningException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (final JobRestartException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (final JobInstanceAlreadyCompleteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        jobLauncherThread.start();

        // let's wait for a while and let the job start before we get
        // the details of the job in the database.
        Thread.sleep(10000);

        long executionId = 0;

        // now that the job is running and has persisted itself in the DB,
        // let's stop (suspend) its execution
        for (final String jobName : mJobOperator.getJobNames()) {
            if (jobName.equals(jobId)) {
                LOGGER.debug("Found the running job to be suspended");

                for (final Long execId : mJobOperator
                    .getRunningExecutions(jobName)) {
                    mJobOperator.stop(execId);
                    jobExecution = mJobExplorer.getJobExecution(execId);
                    executionId = execId;
                }
            }
        }

        // join the master thread so this doesn't run on its own and let
        // the master thread finish before the actual job finishes
        jobLauncherThread.join();

        assertTrue(jobExecution != null
            && jobExecution.getStatus() == BatchStatus.STOPPING,
            "Job hasn't stopped");

        assertTrue(executionId != 0);

        final Long restartedJobExecutionId = mJobOperator.restart(executionId);
        jobExecution = mJobExplorer.getJobExecution(restartedJobExecutionId);

        assertTrue(jobExecution != null
            && jobExecution.getStatus() == BatchStatus.COMPLETED);

    }

    @Test(enabled = false)
    public void testSuspendResumeSuspendResume() throws Exception {
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-suspend.xml").getFile();
        //mDtsJob = JobDefinitionDocument.Factory.parse(f);
        String docString = TestUtils.readFileAsString(f.getAbsolutePath());
        String homeDir = System.getProperty("user.home").replaceAll("\\\\", "/");
        docString = docString.replaceAll("@home.dir.replacement@", homeDir);
        mDtsJob = JobDefinitionDocument.Factory.parse(docString);

        assertNotNull(mDtsJob);

        final String jobId = UUID.randomUUID().toString();

        JobExecution jobExecution = null;

        final Thread jobLauncherThread = new Thread() {
            @Override
            public void run() {

                try {
                    mJobLauncher.run(jobId, mDtsJob);
                }
                catch (final JobExecutionAlreadyRunningException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (final JobRestartException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (final JobInstanceAlreadyCompleteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        jobLauncherThread.start();

        // let's wait for a while and let the job start before we get
        // the details of the job in the database.
        Thread.sleep(10000);

        long executionId = 0;

        // now that the job is running and has persisted itself in the DB,
        // let's stop (suspend) its execution
        for (final String jobName : mJobOperator.getJobNames()) {
            if (jobName.equals(jobId)) {
                LOGGER.debug("Found the running job to be suspended");

                for (final Long execId : mJobOperator
                    .getRunningExecutions(jobName)) {
                    mJobOperator.stop(execId);
                    jobExecution = mJobExplorer.getJobExecution(execId);
                    executionId = execId;
                }
            }
        }

        // join the master thread so this doesn't run on its own and let
        // the master thread finish before the actual job finishes
        jobLauncherThread.join();

        assertTrue(jobExecution != null
            && jobExecution.getStatus() == BatchStatus.STOPPING,
            "Job hasn't stopped");

        assertTrue(executionId != 0);

        final long jobResumedExecId = executionId;

        final Thread jobRelauncherThread = new Thread() {
            @Override
            public void run() {

                try {
                    mJobOperator.restart(jobResumedExecId);
                }
                catch (final JobRestartException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (final JobInstanceAlreadyCompleteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (final NoSuchJobExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (final NoSuchJobException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };

        jobRelauncherThread.start();

        // let's wait for a while and let the job start before we get
        // the details of the job in the database.
        Thread.sleep(6000);

        // now that the job is running and has persisted itself in the DB,
        // let's stop (suspend) its execution
        for (final String jobName : mJobOperator.getJobNames()) {
            if (jobName.equals(jobId)) {
                LOGGER.debug("Found the running job to be suspended again");

                for (final Long execId : mJobOperator
                    .getRunningExecutions(jobName)) {
                    mJobOperator.stop(execId);
                    jobExecution = mJobExplorer.getJobExecution(execId);
                    executionId = execId;
                }
            }
        }

        // join the master thread so this doesn't run on its own and let
        // the master thread finish before the actual job finishes
        jobRelauncherThread.join();

        assertTrue(jobExecution != null
            && jobExecution.getStatus() == BatchStatus.STOPPING,
            "Job hasn't stopped");

        assertTrue(executionId != 0);

        final Long restartedJobExecutionId = mJobOperator.restart(executionId);
        jobExecution = mJobExplorer.getJobExecution(restartedJobExecutionId);

        assertTrue(jobExecution != null
            && jobExecution.getStatus() == BatchStatus.COMPLETED);

    }



}
