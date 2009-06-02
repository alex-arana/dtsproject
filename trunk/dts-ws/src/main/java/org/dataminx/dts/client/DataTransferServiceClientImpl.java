package org.dataminx.dts.client;

import java.io.IOException;

import org.dataminx.schemas.dts._2009._05.dts.*;
import org.springframework.ws.client.core.WebServiceTemplate;

public class DataTransferServiceClientImpl implements DataTransferServiceClient {

	private WebServiceTemplate webServiceTemplate;

	public String submitJob(String jobName) throws IOException {
		JobDefinitionType jobDefinition = new JobDefinitionType();
		JobDescriptionType jobDescription = new JobDescriptionType();
		JobIdentificationType jobIdentification = new JobIdentificationType();
		jobDescription.setJobIdentification(jobIdentification);
		jobDefinition.setJobDescription(jobDescription);

		SubmitJobRequest request = new SubmitJobRequest();
		request.setJobDefinition(jobDefinition);

		jobIdentification.setJobName(jobName);

		SubmitJobResponse response =
			(SubmitJobResponse)webServiceTemplate.marshalSendAndReceive(request);

		return response.getJobId();
	}

	public void setWebServiceTemplate(
	    WebServiceTemplate webServiceTemplate) {
	  this.webServiceTemplate = webServiceTemplate;
	}


}
