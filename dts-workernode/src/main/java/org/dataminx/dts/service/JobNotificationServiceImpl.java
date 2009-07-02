/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

import static org.dataminx.dts.common.util.DateUtils.toXmlGregorianCalendar;

import org.dataminx.dts.batch.DtsJob;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.jms.JobEventQueueSender;
import org.dataminx.schemas.dts._2009._05.dts.JobEventDetailType;
import org.dataminx.schemas.dts._2009._05.dts.JobEventUpdateRequest;
import org.dataminx.schemas.dts._2009._05.dts.ObjectFactory;
import org.dataminx.schemas.dts._2009._05.dts.StatusValueEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public void notifyJobError(final DtsJob dtsJob, final String message, final Throwable error) {
        // TODO Implement this method
        throw new UnsupportedOperationException("Method notifyJobProgress() not yet implemented");
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
