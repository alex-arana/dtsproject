package org.dataminx.dts.jms;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.domain.repo.JobDao;
import org.dataminx.schemas.dts._2009._05.dts.JobEventDetailType;
import org.dataminx.schemas.dts._2009._05.dts.JobEventUpdateRequest;
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
     * @param message the jobEventUpdateRequest message
     */
    public void updateJob(Message<JobEventUpdateRequest> message) {
        JobEventUpdateRequest request = message.getPayload();

        // TODO: probably need to look at making the resourcekey to WN job ID mapping clear (same names?)
        // later on

        // TODO: need to sync this with whatever is supported by the WN

        // get the details of the job entry to be updated
        String updatedJobResourceKey = request.getJobId();
        JobEventDetailType updatedJobDetail = request.getJobEventDetail();

        // job to update
        Job job = mJobRepository.findByResourceKey(updatedJobResourceKey);

        switch(updatedJobDetail.getStatus()) {
            case TRANSFERRING:
                job.setWorkerNodeHost(updatedJobDetail.getWorkerNodeHost());
                job.setActiveTime(updatedJobDetail.getActiveTime().toGregorianCalendar().getTime());
                job.setStatus(JobStatus.TRANSFERRING);
                break;
            case DONE:
                job.setFinishedFlag(updatedJobDetail.isFinishedFlag());
                job.setWorkerTerminatedTime(updatedJobDetail.getWorkerTerminatedTime().toGregorianCalendar().getTime());
                job.setStatus(JobStatus.DONE);

                // also set the WS specific fields..
                job.setJobAllDoneTime(new Date());

                // TODO: need to think of how to handle error messages from WN to the success flag
                // can be set

                break;
            default:
                break;
        }
        mJobRepository.saveOrUpdate(job);
    }
}
