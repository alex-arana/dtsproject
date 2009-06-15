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

@Endpoint
public class DataTransferServiceEndpoint {

	private DataTransferService dataTransferService;
	private final ObjectFactory objectFactory = new ObjectFactory();

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

		String status = dataTransferService.getJobStatus(request.getJobId());

		GetJobStatusResponse response = new GetJobStatusResponse();
		StateType state = new StateType();
		state.setValue(StatusValueEnumeration.fromValue(status));
		response.setState(state);
		return response;
	}

}
