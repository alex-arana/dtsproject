package org.dataminx.dts.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

import javax.xml.bind.JAXBElement;

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
		logger.info("In DataTransferServiceEndpoint, job name " + jobName + " has arrived.");

		String jobId = dataTransferService.submitJob(request.getJobDefinition());
		logger.info("In DataTransferServiceEndpoint, returned jobId is " + jobId);

		SubmitJobResponse response = new SubmitJobResponse();
		response.setJobId(jobId);
		return response;
	}

}
