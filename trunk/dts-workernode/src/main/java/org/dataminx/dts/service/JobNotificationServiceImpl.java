/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

import static org.dataminx.dts.common.util.DateUtils.toXmlGregorianCalendar;

import java.util.List;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.dataminx.dts.batch.DtsJob;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.jms.JobEventQueueSender;
import org.dataminx.schemas.dts._2009._05.dts.FireUpJobErrorEvent;
import org.dataminx.schemas.dts._2009._05.dts.FireUpStepFailureEvent;
import org.dataminx.schemas.dts._2009._05.dts.JobErrorEventDetailType;
import org.dataminx.schemas.dts._2009._05.dts.JobEventDetailType;
import org.dataminx.schemas.dts._2009._05.dts.JobEventUpdateRequest;
import org.dataminx.schemas.dts._2009._05.dts.ObjectFactory;
import org.dataminx.schemas.dts._2009._05.dts.StatusValueEnumeration;
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
    public void notifyJobError(final String jobId, final JobExecution jobExecution) {
        Assert.notNull(jobId);
        Assert.notNull(jobExecution);
        LOG.info(String.format("DTS Job '%s' error message notification", jobId));

        if (jobExecution.getStatus().isUnsuccessful()) {
            // convert to the relevant JAXB2 entity (FireUpJobErrorEvent)
            final ExitStatus exitStatus = jobExecution.getExitStatus();
            final JobErrorEventDetailType errorDetails = mJaxbObjectFactory.createJobErrorEventDetailType();
            errorDetails.setWorkerNodeHost(mdtsWorkerNodeInformationService.getInstanceId());
            errorDetails.setTimeOfOccurrence(toXmlGregorianCalendar(mdtsWorkerNodeInformationService.getCurrentTime()));
            errorDetails.setErrorMessage(exitStatus.getExitDescription());

            // add all failure stack traces to the outgoing message
            final List<String> failures = errorDetails.getFailureTrace();
            for (final Throwable failure : jobExecution.getAllFailureExceptions()) {
                errorDetails.setClassExceptionName(failure.getClass().getName());
                failures.add(ExceptionUtils.getFullStackTrace(failure));
            }

            final FireUpJobErrorEvent jobErrorEvent = mJaxbObjectFactory.createFireUpJobErrorEvent();
            jobErrorEvent.setJobId(jobId);
            jobErrorEvent.setJobErrorEventDetail(errorDetails);
            mJobEventQueueSender.doSend(jobId, jobErrorEvent);
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
            // convert to the relevant JAXB2 entity (FireUpJobErrorEvent)
            final ExitStatus exitStatus = stepExecution.getExitStatus();
            final JobErrorEventDetailType errorDetails = mJaxbObjectFactory.createJobErrorEventDetailType();
            errorDetails.setWorkerNodeHost(mdtsWorkerNodeInformationService.getInstanceId());
            errorDetails.setTimeOfOccurrence(toXmlGregorianCalendar(stepExecution.getStartTime()));
            errorDetails.setErrorMessage(String.format("An error has occurred during the execution of"
                + " DTS Job step '%s': %s", stepExecution.getStepName(), exitStatus.getExitDescription()));

            final List<String> failures = errorDetails.getFailureTrace();
            for (final Throwable failure : stepExecution.getFailureExceptions()) {
                errorDetails.setClassExceptionName(failure.getClass().getName());
                failures.add(ExceptionUtils.getFullStackTrace(failure));
            }

            //TODO need to create a different type of schema entity to transmit Step failures...
            final FireUpStepFailureEvent stepFailureEvent = mJaxbObjectFactory.createFireUpStepFailureEvent();
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
