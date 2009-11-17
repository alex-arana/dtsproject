/**
 * Copyright (c) 2009, VeRSI Consortium
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
