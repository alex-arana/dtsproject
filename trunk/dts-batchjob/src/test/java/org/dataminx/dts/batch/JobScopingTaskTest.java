package org.dataminx.dts.batch;

import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_JOB_DETAILS;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.util.UUID;

import org.dataminx.dts.batch.service.JobNotificationService;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A unit test for the JobScopingTask class.
 * 
 * @author Gerson Galang
 * @author David Meredith (modifications)
 */
@Test(groups = {"unit-test"})
public class JobScopingTaskTest {

    private JobNotificationService mJobNotificationService;
    private JobPartitioningStrategy mJobPartitioningStrategy;
    private SubmitJobRequest mSubmitJobRequest;
    private ChunkContext mChunkContext;

    @BeforeClass
    public void init() {
        mJobNotificationService = mock(JobNotificationService.class);
        mJobPartitioningStrategy = mock(JobPartitioningStrategy.class);
        mSubmitJobRequest = mock(SubmitJobRequest.class);
        mChunkContext = mock(ChunkContext.class);
    }

    @Test(groups = {"local-file-transfer-test"})
    public void testExecute() throws Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()+ ".xml").getFile();
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();

        //final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);

        String docString = TestUtils.readFileAsString(f.getAbsolutePath());
        String homeDir = System.getProperty("user.home").replaceAll("\\\\", "/");
        docString = docString.replaceAll("@home.dir.replacement@", homeDir);
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(docString);

        final JobScopingTask jobScopingTask = new JobScopingTask();
        jobScopingTask.setJobNotificationService(mJobNotificationService);
        jobScopingTask.setJobPartitioningStrategy(mJobPartitioningStrategy);
        jobScopingTask.setSubmitJobRequest(mSubmitJobRequest);

        final DtsJobDetails dtsJobDetails = new DtsJobDetails();
        final String jobId = UUID.randomUUID().toString();
        dtsJobDetails.setJobId(jobId);
        dtsJobDetails.setJobTag(jobId);
        dtsJobDetails.setTotalBytes(123);
        dtsJobDetails.setTotalFiles(123);

        jobScopingTask.setJobTag(jobId);
        jobScopingTask.setJobResourceKey(jobId);

        final StepExecution stepExecution = new StepExecution("123",
            new JobExecution(new Long(123)));

        final StepContext stepContext = new StepContext(stepExecution);

        when(mChunkContext.getStepContext()).thenReturn(stepContext);
        when(
            mJobPartitioningStrategy.partitionTheJob(
                (JobDefinitionType) anyObject(), anyString(), anyString()))
            .thenReturn(dtsJobDetails);

        final RepeatStatus taskStatus = jobScopingTask.execute(null,
            mChunkContext);

        verify(mJobNotificationService).notifyJobScope(anyString(), anyInt(),
            anyLong(), (StepExecution) anyObject());

        assertNotNull(stepContext.getStepExecution().getExecutionContext().get(
            DTS_JOB_DETAILS));
        assertEquals(taskStatus, RepeatStatus.FINISHED);
    }

}
