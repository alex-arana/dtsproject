package org.dataminx.dts.batch;

import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_JOB_DETAILS;

import org.apache.commons.vfs.FileSystemManager;
import org.dataminx.dts.batch.service.JobNotificationService;
import org.dataminx.dts.vfs.FileSystemManagerDispenser;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class JobScopingTask implements Tasklet, InitializingBean {

    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(JobScopingTask.class);

    /** A reference to the input DTS job request. */
    private SubmitJobRequest mSubmitJobRequest;

    private JobPartitioningStrategy mJobPartitioningStrategy;

    private FileSystemManagerDispenser mFileSystemManagerDispenser;

    /** A reference to the application's job notification service. */
    private JobNotificationService mJobNotificationService;

    private String mJobResourceKey;

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {

        Assert.state(mSubmitJobRequest != null, "Unable to find DTS Job Request in execution context.");

        final FileSystemManager fileSystemManager = mFileSystemManagerDispenser.getFileSystemManager();
        final DtsJobDetails jobDetails = mJobPartitioningStrategy.partitionTheJob(mSubmitJobRequest.getJobDefinition(),
                fileSystemManager, mJobResourceKey);

        // update the WS with the details gathered by the job scoping process
        mJobNotificationService.notifyJobScope(jobDetails.getJobId(), jobDetails.getTotalFiles(), jobDetails
                .getTotalBytes());

        // immediately close the file system manager so FileCopyTask will be able to use all of the 
        // available connections
        mFileSystemManagerDispenser.closeFileSystemManager();

        final ExecutionContext stepContext = chunkContext.getStepContext().getStepExecution().getExecutionContext();
        stepContext.put(DTS_JOB_DETAILS, jobDetails);

        //final Map<String, Object> jobExecutionContext = chunkContext.getStepContext().getJobExecutionContext();
        //jobExecutionContext.put(DTS_JOB_DETAILS, jobDetails);

        return RepeatStatus.FINISHED;
    }

    public void setSubmitJobRequest(final SubmitJobRequest submitJobRequest) {
        mSubmitJobRequest = submitJobRequest;
    }

    public void setJobPartitioningStrategy(final JobPartitioningStrategy jobPartitioningStrategy) {
        mJobPartitioningStrategy = jobPartitioningStrategy;
    }

    public void setFileSystemManagerDispenser(final FileSystemManagerDispenser fileSystemManagerDispenser) {
        mFileSystemManagerDispenser = fileSystemManagerDispenser;
    }

    public void setJobResourceKey(final String jobResourceKey) {
        mJobResourceKey = jobResourceKey;
    }

    public void setJobNotificationService(final JobNotificationService jobNotificationService) {
        mJobNotificationService = jobNotificationService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(mSubmitJobRequest != null, "Unable to find DTS Job Request in execution context.");
        Assert.state(mJobPartitioningStrategy != null, "JobPartitioningStrategy has not been set.");
        Assert.state(mFileSystemManagerDispenser != null, "FileSystemManagerDispenser has not been set.");
        Assert.state(mJobResourceKey != null, "Unable to find the Job Resource Key in the execution context.");
        Assert.state(mJobNotificationService != null, "JobNotificationService has not been set.");
    }

}