/**
 * Copyright (c) 2010, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.batch;

import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_JOB_DETAILS;
import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_SUBMIT_JOB_REQUEST_KEY;

import org.dataminx.dts.batch.common.util.ExecutionContextCleaner;
import org.dataminx.dts.batch.service.JobNotificationService;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * The JobScopingTask is a tasklet step that performs the scoping of the job to find out how many files are to be
 * transferred by the job. This step will also partition the job into a number of steps which can be checkpointed
 * at any point while the job is running.
 *
 * @author Gerson Galang
 */
public class JobScopingTask implements Tasklet, StepExecutionListener,
    InitializingBean {

    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory
        .getLogger(JobScopingTask.class);

    /** A reference to the input DTS job request. */
    private SubmitJobRequest mSubmitJobRequest;

    /** A reference to the execution context cleaner. */
    private ExecutionContextCleaner mExecutionContextCleaner;

    /** A reference to the job partitioning strategy. */
    private JobPartitioningStrategy mJobPartitioningStrategy;

    /** A reference to the application's job notification service. */
    private JobNotificationService mJobNotificationService;

    /** The job resource key. */
    private String mJobResourceKey;

    /** The job tag. */
    private String mJobTag;

    /**
     * Scopes the job by partitioning it into a number steps that can be check-pointed.
     *
     * @param contribution mutable state to be passed back to update the current step execution.
     *        Its role is to collect updates to the current StepExecution without
     *        having to worry about concurrent modifications in another thread.
     * @param chunkContext attributes shared between invocations but not between restarts.
     *        A chunk-scoped bag of key-value pairs. The tasklet can use this to store
     *        intermediate results that will be preserved across a rollback.
     * @return a RepeatStatus indicating whether processing is continuable
     * @throws Exception on failure
     */
    public RepeatStatus execute(final StepContribution contribution,
        final ChunkContext chunkContext) throws Exception {

        Assert.state(mSubmitJobRequest != null,
            "Unable to find DTS Job Request in execution context.");

        // partition the job and return partitioning results in the jobDetails
        // The JobDetails will then be storred in the Job ExecutionContext after
        // the step completes (i.e. via promotion from the step ExecutionContext
        // to the Job ExeCtxt after the step completes).
        final DtsJobDetails jobDetails = mJobPartitioningStrategy
            .partitionTheJob(mSubmitJobRequest.getDataCopyActivity(),
                mJobResourceKey, mJobTag);

        final StepContext stepContext = chunkContext.getStepContext();

        // update the WS with the details gathered by the job scoping process
        mJobNotificationService.notifyJobScope(jobDetails.getJobId(),
            jobDetails.getTotalFiles(), jobDetails.getTotalBytes(), stepContext
                .getStepExecution());

        // store the jobDetails in the step execution context. these will get
        // promoted to the Job execution context by the jobScopingTaskPromotionListener
        // so that the jobDetails will be available from the job.
        final ExecutionContext stepExecutionContext = stepContext
            .getStepExecution().getExecutionContext();
        stepExecutionContext.put(DTS_JOB_DETAILS, jobDetails);

        return RepeatStatus.FINISHED;
    }

    public void setSubmitJobRequest(final SubmitJobRequest submitJobRequest) {
        mSubmitJobRequest = submitJobRequest;
    }

    public void setJobPartitioningStrategy(
        final JobPartitioningStrategy jobPartitioningStrategy) {
        mJobPartitioningStrategy = jobPartitioningStrategy;
    }

    public void setJobResourceKey(final String jobResourceKey) {
        mJobResourceKey = jobResourceKey;
    }

    public void setJobTag(final String jobTag) {
        mJobTag = jobTag;
    }

    public void setJobNotificationService(
        final JobNotificationService jobNotificationService) {
        mJobNotificationService = jobNotificationService;
    }

    public void setExecutionContextCleaner(
        final ExecutionContextCleaner executionContextCleaner) {
        mExecutionContextCleaner = executionContextCleaner;
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(mSubmitJobRequest != null,
            "Unable to find DTS Job Request in execution context.");
        Assert.state(mJobPartitioningStrategy != null,
            "JobPartitioningStrategy has not been set.");
        Assert.state(mJobResourceKey != null,
            "Unable to find the Job Resource Key in the execution context.");
        Assert.state(mJobTag != null,
            "Unable to find the Job Tag in the execution context.");
        Assert.state(mJobNotificationService != null,
            "JobNotificationService has not been set.");
        Assert.state(mExecutionContextCleaner != null,
            "ExecutionContextCleaner has not been set.");
    }

    /**
     * Cleans up the StepExecution DB table by removing job details that might have the user's credentials.
     *
     * @param stepExecution the StepExecution
     * @return the ExitStatus of this step
     */
    public ExitStatus afterStep(final StepExecution stepExecution) {
        if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
            mExecutionContextCleaner.removeStepExecutionContextEntry(
                stepExecution, DTS_SUBMIT_JOB_REQUEST_KEY);
            mExecutionContextCleaner.removeStepExecutionContextEntry(
                stepExecution, DTS_JOB_DETAILS);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void beforeStep(final StepExecution stepExecution) {
        // do nothing
    }

}
