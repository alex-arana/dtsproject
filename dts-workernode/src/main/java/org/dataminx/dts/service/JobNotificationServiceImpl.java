/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

import static org.dataminx.dts.common.util.DateUtils.toXmlGregorianCalendar;

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.dataminx.dts.batch.DtsJob;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.jms.JobEventQueueSender;
import org.dataminx.schemas.dts._2009._05.dts.FireUpJobErrorEvent;
import org.dataminx.schemas.dts._2009._05.dts.JobErrorEventDetailType;
import org.dataminx.schemas.dts._2009._05.dts.JobEventDetailType;
import org.dataminx.schemas.dts._2009._05.dts.JobEventUpdateRequest;
import org.dataminx.schemas.dts._2009._05.dts.ObjectFactory;
import org.dataminx.schemas.dts._2009._05.dts.StatusValueEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
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

    /** A reference to the JAXB2 object factory helper. */
    private final ObjectFactory mJaxbObjectFactory = new ObjectFactory();

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
    public void notifyJobError(final DtsJob dtsJob, final String message, final Throwable cause) {
        Assert.notNull(dtsJob);
        final String jobId = dtsJob.getJobId();
        LOG.info(String.format("DTS Job '%s' error message notification [message='%s']", jobId, message));

        // convert to the relevant JAXB2 entity (FireUpJobErrorEvent)
        final JobErrorEventDetailType errorDetails = mJaxbObjectFactory.createJobErrorEventDetailType();
        errorDetails.setWorkerNodeHost(mdtsWorkerNodeInformationService.getInstanceId());
        errorDetails.setTimeOfOccurrence(toXmlGregorianCalendar(dtsJob.getStartTime()));
        errorDetails.setErrorMessage(message);
        if (cause != null) {
            errorDetails.setClassExceptionName(cause.getClass().getName());
            final String stackTrace = ExceptionUtils.getFullStackTrace(cause);
            if (StringUtils.isNotBlank(stackTrace)) {
                errorDetails.setStackTrace(stackTrace);
            }
        }

        final FireUpJobErrorEvent jobErrorEvent = mJaxbObjectFactory.createFireUpJobErrorEvent();
        jobErrorEvent.setJobId(jobId);
        jobErrorEvent.setJobErrorEventDetail(errorDetails);
        mJobEventQueueSender.doSend(jobId, jobErrorEvent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyStepFailures(final String jobId, final StepExecution stepExecution) {
        Assert.notNull(jobId);
        Assert.notNull(stepExecution);
        final ExitStatus exitStatus = stepExecution.getExitStatus();
        if (exitStatus.compareTo(ExitStatus.FAILED) == 0) {
            // convert to the relevant JAXB2 entity (FireUpJobErrorEvent)
            final JobErrorEventDetailType errorDetails = mJaxbObjectFactory.createJobErrorEventDetailType();
            errorDetails.setWorkerNodeHost(mdtsWorkerNodeInformationService.getInstanceId());
            errorDetails.setTimeOfOccurrence(toXmlGregorianCalendar(stepExecution.getStartTime()));
            errorDetails.setErrorMessage(String.format("An error has occurred during the execution of"
                + " DTS Job step '%s': %s", stepExecution.getStepName(), exitStatus.getExitDescription()));

            final List<Throwable> failures = stepExecution.getFailureExceptions();
            if (CollectionUtils.isNotEmpty(failures)) {
                //TODO what to do about multiple failures...multiple messages?
                final Throwable failure = failures.get(0);
                errorDetails.setClassExceptionName(failure.getClass().getName());
                errorDetails.setStackTrace(ExceptionUtils.getFullStackTrace(failure));
            }

            final FireUpJobErrorEvent stepFailureEvent = mJaxbObjectFactory.createFireUpJobErrorEvent();
            stepFailureEvent.setJobId(jobId);
            stepFailureEvent.setJobErrorEventDetail(errorDetails);
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

        // convert to the relevant JAXB2 entity (JobEventUpdateRequest?)
        final JobEventDetailType jobEventDetail = mJaxbObjectFactory.createJobEventDetailType();
        jobEventDetail.setWorkerNodeHost(mdtsWorkerNodeInformationService.getInstanceId());
        jobEventDetail.setActiveTime(toXmlGregorianCalendar(dtsJob.getStartTime()));

        switch (jobStatus) {
            case TRANSFERRING:
                jobEventDetail.setStatus(StatusValueEnumeration.TRANSFERRING);
                break;
            case DONE:
                jobEventDetail.setFinishedFlag(true);
                jobEventDetail.setWorkerTerminatedTime(toXmlGregorianCalendar(dtsJob.getCompletedTime()));
                jobEventDetail.setStatus(StatusValueEnumeration.DONE);
                //TODO use the job's ExitStatus flag
                //jobEventDetail.setSuccessFlag(dtsJob.getXXX());
                break;
            default:
                break;
        }

        final JobEventUpdateRequest jobEventUpdate = mJaxbObjectFactory.createJobEventUpdateRequest();
        jobEventUpdate.setJobId(jobId);
        jobEventUpdate.setJobEventDetail(jobEventDetail);
        mJobEventQueueSender.doSend(jobId, jobEventUpdate);
    }
}
