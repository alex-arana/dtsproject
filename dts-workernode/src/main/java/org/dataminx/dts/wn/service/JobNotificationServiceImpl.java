/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.service;

import static org.dataminx.dts.wn.common.util.DateUtils.toCalendar;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.wn.batch.DtsJob;
import org.dataminx.dts.wn.jms.JobEventQueueSender;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpJobErrorEventDocument;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpJobErrorEventDocument.FireUpJobErrorEvent;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpStepFailureEventDocument;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpStepFailureEventDocument.FireUpStepFailureEvent;
import org.dataminx.schemas.dts.x2009.x07.jms.JobErrorEventDetailType;
import org.dataminx.schemas.dts.x2009.x07.jms.JobEventDetailType;
import org.dataminx.schemas.dts.x2009.x07.jms.JobEventUpdateRequestDocument;
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
 * Default implementation of the DTS Worker Node's {@link JobNotificationService}.
 *
 * @author Alex Arana
 */
@Service("jobNotificationService")
@Scope("singleton")
public class JobNotificationServiceImpl implements JobNotificationService {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(JobNotificationServiceImpl.class);

    /** A reference to the DTS Worker Node information service. */
    @Autowired
    private DtsWorkerNodeInformationService mdtsWorkerNodeInformationService;

    /**
     * A reference to the Job Event Queue sender object.
     */
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

            mJobEventQueueSender.doSend(jobId, stepFailureEvent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyJobProgress(final DtsJob dtsJob, final String message) {
        // TODO Implement this method
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
}
