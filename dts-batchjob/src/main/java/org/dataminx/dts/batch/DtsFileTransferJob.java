/**
 * Copyright (c) 2009, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
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
import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_JOB_RESOURCE_KEY;
import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_JOB_TAG;
import static org.dataminx.dts.batch.common.DtsBatchJobConstants.DTS_SUBMIT_JOB_REQUEST_KEY;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.batch.service.JobNotificationService;
import org.dataminx.dts.common.model.JobStatus;
import org.dataminx.dts.common.util.CredentialStore;
import org.dataminx.dts.common.util.StopwatchTimer;
//import org.dataminx.schemas.dts.x2009.x07.jsdl.CredentialKeyPointerDocument;
//import org.dataminx.schemas.dts.x2009.x07.jsdl.CredentialType;
//import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
//import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;

import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
//import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
//import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDescriptionType;
//import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobIdentificationType;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.CopyType;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.CredentialKeyPointerDocument;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.CredentialType;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.StartLimitExceededException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * DTS Job that performs a file copy operation.
 *
 * @author Alex Arana
 * @author Gerson Galang
 * @author David Meredith
 */
public class DtsFileTransferJob extends SimpleJob implements InitializingBean {

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(DtsFileTransferJob.class);

    /** The unique DTS job identifier. */
    private final String mJobId;

    /** Job tag. */
    private final String mTag;

    /** The time in milliseconds when the job started executing. */
    private long mStartTime;

    /** The time in milliseconds when the job completed executing. */
    private long mCompletedTime;

    /** A reference to the application's job notification service.  */
    // should this be wired in config rather than autowired. 
    @Autowired
    private JobNotificationService mJobNotificationService;

    /** A reference to the JobPartitioningStrategy. */
    private JobPartitioningStrategy mJobPartitioningStrategy;

    /** Holds the information about the job request. */
    private final SubmitJobRequest mJobRequest;

    /** The partitioning step acts as the master step over all of the fileCopyingSteps. */
    private Step mPartitioningStep;

    /** A reference to the MaxStreamCounterTask step. */
    private Step mMaxStreamCountingStep;

    /** A reference to the JobScopingTask step. */
    private Step mJobScopingStep;

    /** A reference to the CheckRequirementsTask step. */
    private Step mCheckRequirementsStep;

    /** A reference to the StopwatchTimer. */
    private StopwatchTimer mStopwatchTimer;

    /**
     * Constructs a new instance of <code>DtsSubmitJob</code> using the specified job request details.
     *
     * @param jobId Unique job identifier
     * @param tag Unique tag that maps to the original job identifier. The tag is used to get around
     *        the naming scheme restrictions on the jobId
     * @param jobRequest Job request details
     * @param jobRepository Job repository
     * @param credentialStore the credential store
     */
    public DtsFileTransferJob(final String jobId, final String tag,
        final SubmitJobRequestDocument jobRequest,
        final JobRepository jobRepository, final CredentialStore credentialStore) {

        mJobId = jobId;
        mTag = tag;
        Assert
            .notNull(jobRequest,
                "Cannot construct a DTS submit job without the required job details.");
        mJobRequest = jobRequest.getSubmitJobRequest();
        setJobRepository(jobRepository);

        // replace the credentials in the mJobRequest with pointers. The credentials
        // are then stored within the given credentialStore
        applyCredentialFiltering(credentialStore);
    }

    /**
     * Returns the time when this job started executing.
     *
     * @return job's execution start time in milliseconds
     */
    public long getStartTime() {
        return mStartTime;
    }

    /**
     * Convenience method that sets the job start time using the DTS
     * WorkerNode's current time.
     */
    public void registerStartTime() {
        mStartTime = Calendar.getInstance().getTime().getTime();
    }

    public long getCompletedTime() {
        return mCompletedTime;
    }

    /**
     * Convenience method that sets the job completed time using the DTS
     * WorkerNode's current time.
     */
    public void registerCompletedTime() {
        mCompletedTime = Calendar.getInstance().getTime().getTime();
    }

    /**
     * Returns the ID that uniquely identifies this job.
     *
     * @return Job ID
     */
    public String getJobId() {
        return mJobId;
    }

    /**
     * Returns the job tag that does not have the naming restriction that the job ID has.
     *
     * @return the job tag
     */
    public String getTag() {
        return mTag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRestartable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JobParametersIncrementer getJobParametersIncrementer() {
        return null;
    }

    public JobNotificationService getJobNotificationService() {
        return mJobNotificationService;
    }

    public JobPartitioningStrategy getJobPartitioningStrategy() {
        return mJobPartitioningStrategy;
    }

    public void setJobPartitioningStrategy(
        final JobPartitioningStrategy jobPartitioningStrategy) {
        mJobPartitioningStrategy = jobPartitioningStrategy;
    }

    /**
     * Apply the credential filtering over all the credentials in the job submission
     * document. This replaces the credentials in the mJobRequest with unique pointers. The credentials
     * are then stored within the given credentialStore under that pointer.
     *
     * @param credentialStore the credential store
     */
    private void applyCredentialFiltering(final CredentialStore credentialStore) {

        // get the job's JobRequest document
        //final DataTransferType[] dataTransfers = ((MinxJobDescriptionType) mJobRequest
        //    .getJobDefinition().getJobDescription()).getDataTransferArray();
        final CopyType[] dataTransfers = mJobRequest.getDataCopyActivity().getCopyArray();

        for (int i = 0; i < dataTransfers.length; i++) {
            //final CredentialType sourceCredential = dataTransfers[i].getSource().getCredential();
            //final CredentialType targetCredential = dataTransfers[i].getTarget().getCredential();
            final CredentialType sourceCredential = dataTransfers[i].getSource().getData().getCredentials();
            final CredentialType targetCredential = dataTransfers[i].getSink().getData().getCredentials();


            // these are the credentials that need to be stored in the CredentialStore
            if (sourceCredential != null) {
                final CredentialType sourceCredentialToStore = (CredentialType) sourceCredential.copy();
                // modify the credential by removing the original cred and inserting a key pointer
                final String credUUID = replaceCredentialWithKeyPointer(sourceCredential);

                // store a copy of the original source credential on the credential store
                // TODO: find out when we should write to memory or to database
                credentialStore
                    .writeToMemory(credUUID, sourceCredentialToStore);
            }

            if (targetCredential != null) {
                final CredentialType targetCredentialToStore = (CredentialType) targetCredential
                    .copy();

                final String credUUID = replaceCredentialWithKeyPointer(targetCredential);

                // store the original target credential on the credential store
                // TODO: find out when we should write to memory or to database
                credentialStore
                    .writeToMemory(credUUID, targetCredentialToStore);
            }

        }
    }

    /**
     * Replace the current child of the Credential element with CredentialKeyPointer element wrapped by
     * OtherCredentialToken.
     *
     * @param sourceOrTargetCredential the source or target Credential
     * @return the key to be used in storing the given credential to the CredentialStore
     */
    private String replaceCredentialWithKeyPointer(
        final CredentialType sourceOrTargetCredential) {
        // unset the child element of the sourceCredentialCopy
        //if (sourceOrTargetCredential.isSetUsernameToken()) {
        //    sourceOrTargetCredential.unsetUsernameToken();
        //}
        if(sourceOrTargetCredential.isSetUsernamePasswordToken()){
            sourceOrTargetCredential.unsetUsernamePasswordToken();
        }
        else if (sourceOrTargetCredential.isSetMyProxyToken()) {
            sourceOrTargetCredential.unsetMyProxyToken();
        }
        else if (sourceOrTargetCredential.isSetOtherCredentialToken()) {
            sourceOrTargetCredential.unsetOtherCredentialToken();
        }

        // the credential key uuid
        final String credentialUUID = UUID.randomUUID().toString();

        final CredentialKeyPointerDocument credentialKeyPointer = CredentialKeyPointerDocument.Factory
            .newInstance();
        credentialKeyPointer.setCredentialKeyPointer(credentialUUID);

        // replace the child element of the CredentialType with the new CredentialKeyPointer element
        // wrapped by the OtherCredentialToken
        sourceOrTargetCredential.addNewOtherCredentialToken().set(
            credentialKeyPointer);
        return credentialUUID;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(mPartitioningStep != null,
            "PartitioningStep has not been set.");
        Assert.state(mMaxStreamCountingStep != null,
            "MaxStreamCountingStep has not been set.");
        Assert.state(mJobScopingStep != null,
            "JobScopingStep has not been set.");
        Assert.state(mCheckRequirementsStep != null,
            "CheckRequirementsStep has not been set.");
        super.afterPropertiesSet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute(final JobExecution execution)
        throws JobInterruptedException, JobRestartException,
        StartLimitExceededException {

        boolean checkRequirementsFailed = false;

        // first set the job start time
        registerStartTime();

        // TODO determine exactly what job notifications we need to return and
        // when
        getJobNotificationService().notifyJobStatus(this,
            JobStatus.CREATED, execution); // this was JobStatus.TRANSFERRING

        // first, store the credential-filtered DTS job request object in the job execution context
        final ExecutionContext context = execution.getExecutionContext();
        context.put(DTS_SUBMIT_JOB_REQUEST_KEY, mJobRequest);
        context.put(DTS_JOB_RESOURCE_KEY, getJobId());
        context.put(DTS_JOB_TAG, getTag());

        LOGGER.info("Started the CheckRequirementsTask step at "
            + mStopwatchTimer.getFormattedElapsedTime());

        // check the job requirements.
        StepExecution stepExecution = handleStep(mCheckRequirementsStep,
            execution);

        // we'll skip the other steps if the check requirements task step fails
        if (!stepExecution.getStatus().equals(BatchStatus.COMPLETED)) {
            checkRequirementsFailed = true;
        }

        if (!checkRequirementsFailed) {
            LOGGER.info("Started the JobScopingTask step at "
                + mStopwatchTimer.getFormattedElapsedTime());

            // TODO convert to application exceptions.
            // jobScoping will store the jobDetails in the Job execution context,
            // it generates list of DTUs and creates dir structure on remote
            // sinks.
            stepExecution = handleStep(mJobScopingStep, execution);

            LOGGER.info("Finished the JobScopingTask step at "
                + mStopwatchTimer.getFormattedElapsedTime());
        }

        // we'll skip the other steps if the job scoping task step fails
        if (!checkRequirementsFailed
            && stepExecution.getStatus().equals(BatchStatus.COMPLETED)) {

            // let's check if there's anything to transfer
            final DtsJobDetails dtsJobDetails = (DtsJobDetails) context
                .get(DTS_JOB_DETAILS);
            // TODO: check the jobDetails step count rather than its job-step list
            // as this list is to be removed. 
            if (!dtsJobDetails.getJobSteps().isEmpty()) {

                LOGGER.info("Started the MaxStreamCounting step at "
                    + mStopwatchTimer.getFormattedElapsedTime());
                stepExecution = handleStep(mMaxStreamCountingStep, execution);
                LOGGER.info("Finished the MaxStreamCounting step at "
                    + mStopwatchTimer.getFormattedElapsedTime());

                // we'll only run the FileCopyTask and the master PartitioningStep if MaxStreamCounting step
                // completed successfully
                if (stepExecution.getStatus().equals(BatchStatus.COMPLETED)) {
                    LOGGER.info("Started the FileCopying process at "
                        + mStopwatchTimer.getFormattedElapsedTime());
                    stepExecution = handleStep(mPartitioningStep, execution);
                    LOGGER.info("Finished the FileCopying process at "
                        + mStopwatchTimer.getFormattedElapsedTime());
                }

            }

            // update the job status to have the same status as the master step
            if (stepExecution != null) {
                logger.debug("Upgrading JobExecution status: " + stepExecution);
                execution.upgradeStatus(stepExecution.getStatus());
                execution.setExitStatus(stepExecution.getExitStatus());
            }

            if (stepExecution.getStatus().isUnsuccessful()) {
                getJobNotificationService().notifyJobError(getJobId(),
                    execution);
                return;
            }

            // TODO move this somewhere it always gets called
            registerCompletedTime();
            getJobNotificationService().notifyJobStatus(this, JobStatus.DONE,
                execution);
        }
        else {
            execution.setStatus(BatchStatus.FAILED);
            getJobNotificationService().notifyJobError(getJobId(), execution);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getDescription() {
        /*String description = null;
        final JobIdentificationType jobIdentification = getJobIdentification();
        if (jobIdentification != null) {
            description = jobIdentification.getDescription();
        }
        return description;*/
        return "No description available";
    }

    /**
     * Returns the job description, containing all details about the underlying job.
     *
     * @return Job request details
     */
    /*protected JobDescriptionType getJobDescription() {
        JobDescriptionType result = null;
        final JobDefinitionType jobDefinition = mJobRequest.getJobDefinition();
        if (jobDefinition != null) {
            result = jobDefinition.getJobDescription();
        }
        return result;
    }*/

    /**
     * Returns the job identification, containing all details about the underlying job.
     *
     * @return Job identification details
     */
    /*protected JobIdentificationType getJobIdentification() {
        JobIdentificationType result = null;
        final JobDescriptionType jobDescription = getJobDescription();
        if (jobDescription != null) {
            result = jobDescription.getJobIdentification();
        }
        return result;
    }*/

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return getJobId();
    }

    public void setCheckRequirementsStep(final Step checkRequirementsStep) {
        mCheckRequirementsStep = checkRequirementsStep;
    }

    /**
     * Sets the List of JobExecutionListeners.
     *
     * @param jobExecutionListeners the List of JobExecutionListeners
     */
    public void setJobExecutionListeners(
        final List<JobExecutionListener> jobExecutionListeners) {
        setJobExecutionListeners(jobExecutionListeners
            .toArray(new JobExecutionListener[0]));
    }

    public void setJobScopingStep(final Step jobScopingStep) {
        mJobScopingStep = jobScopingStep;
    }

    public void setMaxStreamCountingStep(final Step maxStreamCountingStep) {
        mMaxStreamCountingStep = maxStreamCountingStep;
    }

    public void setPartitioningStep(final Step partitioningStep) {
        mPartitioningStep = partitioningStep;
    }

    public void setStopwatchTimer(final StopwatchTimer stopwatchTimer) {
        mStopwatchTimer = stopwatchTimer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(ObjectUtils.identityToString(this));
        buffer.append(" [jobId").append("='").append(getJobId()).append("' ");
        buffer.append("jobDescription").append("='").append(getDescription())
            .append("']");
        return buffer.toString();
    }
}
