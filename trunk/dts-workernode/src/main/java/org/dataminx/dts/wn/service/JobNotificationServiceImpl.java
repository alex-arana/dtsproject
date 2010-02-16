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
package org.dataminx.dts.wn.service;

import static org.dataminx.dts.wn.common.util.DateUtils.toCalendar;

import org.dataminx.dts.batch.service.JobNotificationService;

import org.dataminx.dts.batch.service.DtsWorkerNodeInformationService;

import org.dataminx.dts.batch.DtsJob;

import java.math.BigInteger;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.wn.jms.JobEventQueueSender;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpJobErrorEventDocument;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpStepFailureEventDocument;
import org.dataminx.schemas.dts.x2009.x07.jms.JobErrorEventDetailType;
import org.dataminx.schemas.dts.x2009.x07.jms.JobEventDetailType;
import org.dataminx.schemas.dts.x2009.x07.jms.JobEventUpdateRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpJobErrorEventDocument.FireUpJobErrorEvent;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpStepFailureEventDocument.FireUpStepFailureEvent;
import org.dataminx.schemas.dts.x2009.x07.jms.JobEventUpdateRequestDocument.JobEventUpdateRequest;
import org.ogf.schemas.dmi.x2008.x05.dmi.StatusValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Default implementation of the DTS Worker Node's
 * {@link JobNotificationService}.
 * 
 * @author Alex Arana
 * @author Gerson Galang
 */
@Service("jobNotificationService")
@Scope("singleton")
public class JobNotificationServiceImpl implements JobNotificationService {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(JobNotificationServiceImpl.class);

    /** A reference to the DTS Worker Node information service. */
    @Autowired
    private DtsWorkerNodeInformationService mdtsWorkerNodeInformationService;

    /** A reference to the Job Event Queue sender object. */
    @Autowired
    private JobEventQueueSender mJobEventQueueSender;

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyJobError(final String jobId, final JobExecution jobExecution) {
        Assert.notNull(jobId);
        Assert.notNull(jobExecution);
        LOG.info(String.format("DTS Job '%s' error message notification", jobId));

        if (jobExecution.getStatus().isUnsuccessful()) {
            // convert to the relevant JAXB2 entity (FireUpJobErrorEvent)
            final ExitStatus exitStatus = jobExecution.getExitStatus();

            final FireUpJobErrorEventDocument document = FireUpJobErrorEventDocument.Factory.newInstance();
            final FireUpJobErrorEvent jobErrorEvent = document.addNewFireUpJobErrorEvent();
            final JobErrorEventDetailType errorDetails = jobErrorEvent.addNewJobErrorEventDetail();
            jobErrorEvent.setJobResourceKey(jobId);
            errorDetails.setWorkerNodeHost(mdtsWorkerNodeInformationService.getInstanceId());
            errorDetails.setTimeOfOccurrence(toCalendar(mdtsWorkerNodeInformationService.getCurrentTime()));
            errorDetails.setErrorMessage(exitStatus.getExitDescription());

            // add all failure stack traces to the outgoing message
            for (final Throwable failure : jobExecution.getAllFailureExceptions()) {
                errorDetails.setClassExceptionName(failure.getClass().getName());
                errorDetails.addFailureTrace(ExceptionUtils.getFullStackTrace(failure));
            }

            mJobEventQueueSender.doSend(jobId, document);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyStepFailures(final String jobId, final StepExecution stepExecution) {
        Assert.notNull(jobId);
        Assert.notNull(stepExecution);
        if (stepExecution.getStatus().isUnsuccessful()) {
            // convert to the relevant schema entity (FireUpStepFailureEvent)
            final ExitStatus exitStatus = stepExecution.getExitStatus();
            final FireUpStepFailureEventDocument document = FireUpStepFailureEventDocument.Factory.newInstance();
            final FireUpStepFailureEvent stepFailureEvent = document.addNewFireUpStepFailureEvent();
            final JobErrorEventDetailType errorDetails = stepFailureEvent.addNewJobErrorEventDetail();
            stepFailureEvent.setJobResourceKey(jobId);
            errorDetails.setWorkerNodeHost(mdtsWorkerNodeInformationService.getInstanceId());
            errorDetails.setTimeOfOccurrence(toCalendar(stepExecution.getStartTime()));
            errorDetails.setErrorMessage(String.format("An error has occurred during the execution of"
                    + " DTS Job step '%s': %s", stepExecution.getStepName(), exitStatus.getExitDescription()));

            for (final Throwable failure : stepExecution.getFailureExceptions()) {
                errorDetails.setClassExceptionName(failure.getClass().getName());
                errorDetails.addFailureTrace(ExceptionUtils.getFullStackTrace(failure));
            }

            mJobEventQueueSender.doSend(jobId, document);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyJobProgress(final DtsJob dtsJob, final String message) {
        // TODO Implement this method or get rid of it
        throw new UnsupportedOperationException("Method notifyJobProgress() not yet implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyJobStatus(final DtsJob dtsJob, final JobStatus jobStatus) {
        Assert.notNull(dtsJob);
        final String jobId = dtsJob.getJobId();
        LOG.info(String.format("DTS Job '%s' status notification: %s", jobId, jobStatus));

        // convert to the relevant schema entity
        final JobEventUpdateRequestDocument document = JobEventUpdateRequestDocument.Factory.newInstance();
        final JobEventUpdateRequest jobEventUpdate = document.addNewJobEventUpdateRequest();
        final JobEventDetailType jobEventDetail = jobEventUpdate.addNewJobEventDetail();
        jobEventUpdate.setJobResourceKey(jobId);
        jobEventDetail.setWorkerNodeHost(mdtsWorkerNodeInformationService.getInstanceId());
        jobEventDetail.setActiveTime(toCalendar(dtsJob.getStartTime()));
        switch (jobStatus) {
        case TRANSFERRING:
            jobEventDetail.setStatus(StatusValueType.TRANSFERRING);
            break;
        case DONE:
            jobEventDetail.setFinishedFlag(true);
            jobEventDetail.setWorkerTerminatedTime(toCalendar(dtsJob.getCompletedTime()));
            jobEventDetail.setStatus(StatusValueType.DONE);
            //TODO use the job's ExitStatus flag
            //jobEventDetail.setSuccessFlag(dtsJob.getXXX());
            break;
        default:
            break;
        }

        mJobEventQueueSender.doSend(jobId, document);
    }

    @Override
    public void notifyJobProgress(final String jobId, final int filesTransferred, final long volumeTransferred) {
        Assert.notNull(jobId);
        LOG.info(String.format("DTS Job '%s' progress notification", jobId));

        // convert to the relevant schema entity
        final JobEventUpdateRequestDocument document = JobEventUpdateRequestDocument.Factory.newInstance();
        final JobEventUpdateRequest jobEventUpdate = document.addNewJobEventUpdateRequest();
        final JobEventDetailType jobEventDetail = jobEventUpdate.addNewJobEventDetail();
        jobEventUpdate.setJobResourceKey(jobId);
        jobEventDetail.setWorkerNodeHost(mdtsWorkerNodeInformationService.getInstanceId());
        jobEventDetail.setFilesTransferred(BigInteger.valueOf(filesTransferred));
        jobEventDetail.setVolumeTransferred(BigInteger.valueOf(volumeTransferred));
        jobEventDetail.setStatus(StatusValueType.TRANSFERRING);
        mJobEventQueueSender.doSend(jobId, document);

    }

    @Override
    public void notifyJobScope(final String jobId, final int filesTotal, final long volumeTotal) {
        Assert.notNull(jobId);
        LOG.info(String.format("DTS Job '%s' job scope notification", jobId));

        // convert to the relevant schema entity
        final JobEventUpdateRequestDocument document = JobEventUpdateRequestDocument.Factory.newInstance();
        final JobEventUpdateRequest jobEventUpdate = document.addNewJobEventUpdateRequest();
        final JobEventDetailType jobEventDetail = jobEventUpdate.addNewJobEventDetail();
        jobEventUpdate.setJobResourceKey(jobId);
        jobEventDetail.setWorkerNodeHost(mdtsWorkerNodeInformationService.getInstanceId());
        jobEventDetail.setFilesTotal(BigInteger.valueOf(filesTotal));
        jobEventDetail.setVolumeTotal(BigInteger.valueOf(volumeTotal));
        jobEventDetail.setStatus(StatusValueType.TRANSFERRING);
        mJobEventQueueSender.doSend(jobId, document);
    }
}
