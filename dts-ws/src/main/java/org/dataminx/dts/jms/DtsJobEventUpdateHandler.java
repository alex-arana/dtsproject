package org.dataminx.dts.jms;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.domain.repo.JobDao;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpJobErrorEventDocument;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpStepFailureEventDocument;
import org.dataminx.schemas.dts.x2009.x07.jms.JobErrorEventDetailType;
import org.dataminx.schemas.dts.x2009.x07.jms.JobEventDetailType;
import org.dataminx.schemas.dts.x2009.x07.jms.JobEventUpdateRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.jms.FireUpJobErrorEventDocument.FireUpJobErrorEvent;
import org.dataminx.schemas.dts.x2009.x07.jms.JobEventUpdateRequestDocument.JobEventUpdateRequest;
import org.ogf.schemas.dmi.x2008.x05.dmi.StatusValueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.Message;


/**
 * The Handler for all the Job Event Update messages coming from the Worker Node.
 *
 * @author Gerson Galang
 *
 */
public class DtsJobEventUpdateHandler {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DtsJobEventUpdateHandler.class);

    /** The job repository for this DTS implementation. */
    @Autowired
    private JobDao mJobRepository;

    /**
     * Updates the job entity based on the details provided by the worker node.
     *
     * @param message the event update message
     */
    public void handleEvent(Message<?> message) {
        Object payload = message.getPayload();

        if (payload instanceof JobEventUpdateRequestDocument) {
            JobEventUpdateRequest request = ((JobEventUpdateRequestDocument) payload).getJobEventUpdateRequest();

            // TODO: probably need to look at making the resourcekey to WN job ID mapping clear (same names?)
            // later on

            // TODO: need to sync this with whatever is supported by the WN

            // get the details of the job entry to be updated
            String updatedJobResourceKey = request.getJobResourceKey();
            JobEventDetailType updatedJobDetail = request.getJobEventDetail();

            // job to update
            Job job = mJobRepository.findByResourceKey(updatedJobResourceKey);

            switch(updatedJobDetail.getStatus().intValue()) {
                case StatusValueType.INT_TRANSFERRING:
                    job.setWorkerNodeHost(updatedJobDetail.getWorkerNodeHost());
                    job.setActiveTime(updatedJobDetail.getActiveTime().getTime());
                    job.setStatus(JobStatus.TRANSFERRING);
                    break;
                case StatusValueType.INT_DONE:
                    job.setFinishedFlag(updatedJobDetail.getFinishedFlag());
                    job.setWorkerTerminatedTime(
                        updatedJobDetail.getWorkerTerminatedTime().getTime());
                    job.setStatus(JobStatus.DONE);

                    // also set the WS specific fields..
                    job.setJobAllDoneTime(new Date());

                    // TODO: need to think of how to handle error messages from WN so the success flag
                    // can be set

                    break;
                default:
                    break;
            }
            mJobRepository.saveOrUpdate(job);
        }
        else if (payload instanceof FireUpJobErrorEventDocument) {
            LOGGER.info("DtsJobEventUpdateHandler received a FireUpJobErrorEvent.");

            FireUpJobErrorEvent errorEvent = ((FireUpJobErrorEventDocument) payload).getFireUpJobErrorEvent();
            String jobWithErrorResourceKey = errorEvent.getJobResourceKey();
            JobErrorEventDetailType jobErrorDetail = errorEvent.getJobErrorEventDetail();

            // job to update
            Job job = mJobRepository.findByResourceKey(jobWithErrorResourceKey);
            job.setStatus(JobStatus.FAILED);

            // TODO: handle other 'Failed' status variations and the jobErrorDetail

            mJobRepository.saveOrUpdate(job);
        }
        else if (payload instanceof FireUpStepFailureEventDocument) {
            LOGGER.info("DtsJobEventUpdateHandler received a FireUpStepFailureEvent.");

            // TODO: handle the step failure event
        }
        else {
            LOGGER.error("DtsJobEventUpdateHandler received an unknown update event from a WN.");

            // TODO: provide an implementation
        }
    }
}
