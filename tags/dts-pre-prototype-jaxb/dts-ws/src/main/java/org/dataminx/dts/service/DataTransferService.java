package org.dataminx.dts.service;

import org.dataminx.schemas.dts._2009._05.dts.CancelJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.GetJobStatusRequest;
import org.dataminx.schemas.dts._2009._05.dts.ResumeJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.SubmitJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.SuspendJobRequest;

/**
 * This specifies the operations supported by the Data Transfer Service WS. The reason why the
 * methods take in the Request objects is because they are needed by the Worker Node and there's no
 * easy way of getting the Request objects from their child elements.
 *
 * @author Gerson Galang
 */
public interface DataTransferService {

    /**
     * Process the submit job request.
     *
     * @param submitJobRequest the submit job request
     * @return the Universally Unique Identifier (aka Global Unique Identifier) for the submitted job
     */
    String submitJob(SubmitJobRequest submitJobRequest);


    /**
     * Process the cancel job request.
     *
     * @param cancelJobRequest the cancel job request
     */
    void cancelJob(CancelJobRequest cancelJobRequest);


    /**
     * Process the suspend job request.
     *
     * @param suspendJobRequest the suspend job request
     */
    void suspendJob(SuspendJobRequest suspendJobRequest);


    /**
     * Process the resume job request.
     *
     * @param resumeJobRequest the resume job request
     */
    void resumeJob(ResumeJobRequest resumeJobRequest);


    /**
     * Process the get job status request. The GetJobStatusRequest object doesn't get passed down to
     * the Worker Node. The only reason why this method has been defined this way is so it uses the same
     * standard as the rest of the methods provided by the web service.
     *
     * @param getJobStatusRequest the get job status request
     * @return the job status
     */
    String getJobStatus(GetJobStatusRequest getJobStatusRequest);


}
