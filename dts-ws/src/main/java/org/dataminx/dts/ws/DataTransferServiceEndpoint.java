package org.dataminx.dts.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.service.DataTransferService;
import org.dataminx.schemas.dts._2009._05.dts.CancelJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.GetJobStatusRequest;
import org.dataminx.schemas.dts._2009._05.dts.GetJobStatusResponse;
import org.dataminx.schemas.dts._2009._05.dts.ObjectFactory;
import org.dataminx.schemas.dts._2009._05.dts.ResumeJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.StateType;
import org.dataminx.schemas.dts._2009._05.dts.StatusValueEnumeration;
import org.dataminx.schemas.dts._2009._05.dts.SubmitJobRequest;
import org.dataminx.schemas.dts._2009._05.dts.SubmitJobResponse;
import org.dataminx.schemas.dts._2009._05.dts.SuspendJobRequest;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

/**
 * The DataTransferServiceEndpoint processes the incoming XML messages and produces a response from/to
 * the DTS client.
 *
 * @author Gerson Galang
 */
@Endpoint
public class DataTransferServiceEndpoint {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DataTransferServiceEndpoint.class);

    /** The data transfer service. */
    private DataTransferService mDataTransferService;

    /** The object factory. */
    private final ObjectFactory mObjectFactory = new ObjectFactory();

    /**
     * Sets the DataTransferService for this endpoint.
     *
     * @param dataTransferService the data transfer service
     */
    public void setDataTransferService(DataTransferService dataTransferService) {
        mDataTransferService = dataTransferService;
    }

    /**
     * Handles the submit job request.
     *
     * @param request the submit job request
     * @return the response to the submit job call
     */
    @PayloadRoot(localPart = "submitJobRequest", namespace = "http://schemas.dataminx.org/dts/2009/05/dts")
    public SubmitJobResponse doit(SubmitJobRequest request) {
        //DataTransferResponseType response = objectFactory.createDataTransferResponseType();

        String jobName =
            request.getJobDefinition().getJobDescription().getJobIdentification().getJobName();
        LOGGER.debug("In DataTransferServiceEndpoint.doit(SubmitJobRequest), job name " + jobName
            + " has arrived.");

        //String jobId = mDataTransferService.submitJob(request.getJobDefinition());
        String jobId = mDataTransferService.submitJob(request);
        LOGGER.debug("In DataTransferServiceEndpoint.doit(SubmitJobRequest), returned jobId is "
            + jobId);

        SubmitJobResponse response = new SubmitJobResponse();
        response.setJobId(jobId);
        return response;
    }

    /**
     * Handles the cancel job request.
     *
     * @param request the cancel job request
     */
    @PayloadRoot(localPart = "cancelJobRequest", namespace = "http://schemas.dataminx.org/dts/2009/05/dts")
    public void doit(CancelJobRequest request) {
        LOGGER.debug("In DataTransferServiceEndpoint.doit(CancelJobRequest), cancel jobid: "
            + request.getJobId());

        mDataTransferService.cancelJob(request.getJobId());
    }

    /**
     * Handles the suspend job request.
     *
     * @param request the suspend job request
     */
    @PayloadRoot(localPart = "suspendJobRequest", namespace = "http://schemas.dataminx.org/dts/2009/05/dts")
    public void doit(SuspendJobRequest request) {
        LOGGER.debug("In DataTransferServiceEndpoint.doit(SuspendJobRequest), suspend jobid: "
            + request.getJobId());

        mDataTransferService.suspendJob(request.getJobId());
    }

    /**
     * Handles the resume job request.
     *
     * @param request the resume job request
     */
    @PayloadRoot(localPart = "resumeJobRequest", namespace = "http://schemas.dataminx.org/dts/2009/05/dts")
    public void doit(ResumeJobRequest request) {
        LOGGER.debug("In DataTransferServiceEndpoint.doit(ResumeJobRequest), resume jobid: "
            + request.getJobId());

        mDataTransferService.resumeJob(request.getJobId());
    }

    /**
     * Handles the get job status request.
     *
     * @param request the get job status request
     * @return the response to the get job status call
     */
    @PayloadRoot(localPart = "getJobStatusRequest", namespace = "http://schemas.dataminx.org/dts/2009/05/dts")
    public GetJobStatusResponse doit(GetJobStatusRequest request) {
        LOGGER.debug("In DataTransferServiceEndpoint.doit(GetJobStatusRequest), getJobStatus of job: "
                + request.getJobId());

        String status = mDataTransferService.getJobStatus(request.getJobId());

        GetJobStatusResponse response = mObjectFactory.createGetJobStatusResponse();
        StateType state = new StateType();
        state.setValue(StatusValueEnumeration.fromValue(status));
        response.setState(state);
        return response;
    }

}
