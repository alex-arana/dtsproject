package org.dataminx.dts.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

import org.dataminx.dts.service.*;
import org.dataminx.schemas.dts._2009._05.dts.*;

@Endpoint
public class DataTransferServiceEndpoint {

	private DataTransferService dataTransferService;
	private ObjectFactory objectFactory = new ObjectFactory();

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Sets the "business service" to delegate to.
	 */
	public void setDataTransferService(DataTransferService dataTransferService) {
		this.dataTransferService = dataTransferService;
	}

	@PayloadRoot(localPart= "submitJobRequest", namespace="http://schemas.dataminx.org/dts/2009/05/dts")
	public SubmitJobResponse doit(SubmitJobRequest request) {
		//DataTransferResponseType response = objectFactory.createDataTransferResponseType();

		String jobName = request.getJobDefinition().getJobDescription().getJobIdentification().getJobName();
		logger.debug("In DataTransferServiceEndpoint.doit(SubmitJobRequest), job name " + jobName + " has arrived.");

		String jobId = dataTransferService.submitJob(request.getJobDefinition());
		logger.debug("In DataTransferServiceEndpoint.doit(SubmitJobRequest), returned jobId is " + jobId);

		SubmitJobResponse response = new SubmitJobResponse();
		response.setJobId(jobId);
		return response;
	}

	@PayloadRoot(localPart= "cancelJobRequest", namespace="http://schemas.dataminx.org/dts/2009/05/dts")
	public void doit(CancelJobRequest request) {
		logger.debug("In DataTransferServiceEndpoint.doit(CancelJobRequest), cancel jobid: " + request.getJobId());

		dataTransferService.cancelJob(request.getJobId());
	}

	@PayloadRoot(localPart= "suspendJobRequest", namespace="http://schemas.dataminx.org/dts/2009/05/dts")
	public void doit(SuspendJobRequest request) {
		logger.debug("In DataTransferServiceEndpoint.doit(SuspendJobRequest), suspend jobid: " + request.getJobId());

		dataTransferService.suspendJob(request.getJobId());
	}

	@PayloadRoot(localPart= "resumeJobRequest", namespace="http://schemas.dataminx.org/dts/2009/05/dts")
	public void doit(ResumeJobRequest request) {
		logger.debug("In DataTransferServiceEndpoint.doit(ResumeJobRequest), resume jobid: " + request.getJobId());

		dataTransferService.resumeJob(request.getJobId());
	}

	@PayloadRoot(localPart= "getJobStatusRequest", namespace="http://schemas.dataminx.org/dts/2009/05/dts")
	public GetJobStatusResponse doit(GetJobStatusRequest request) {
		logger.debug("In DataTransferServiceEndpoint.doit(GetJobStatusRequest), getJobStatus of job: " + request.getJobId());

		GetJobStatusResponse response = new GetJobStatusResponse();
		StateType state = new StateType();
		state.setValue(StatusValueEnumeration.DONE);
		response.setState(state);
		return response;
	}

}
