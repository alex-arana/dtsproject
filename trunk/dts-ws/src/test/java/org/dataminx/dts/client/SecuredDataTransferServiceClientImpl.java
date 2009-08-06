package org.dataminx.dts.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.ws.security.DtsWsUsernameAuthenticationCallback;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SuspendJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument.CancelJobRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument.GetJobStatusRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument.ResumeJobRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.SuspendJobRequestDocument.SuspendJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * The Secured Data Transfer Service WS Client Implementation.
 *
 * @author Gerson Galang
 */
public class SecuredDataTransferServiceClientImpl implements DataTransferServiceClient {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(SecuredDataTransferServiceClientImpl.class);

    /** The Spring-WS web service template. */
    private WebServiceTemplate mWebServiceTemplate;

    private DtsWsUsernameAuthenticationCallback mRequestCallback;

    /**
     * Instantiates a SecuredDataTransferServiceClientImpl.
     */
    public SecuredDataTransferServiceClientImpl(String username, String password) {
        mRequestCallback =
            new DtsWsUsernameAuthenticationCallback(username, password);
    }

    /**
     * {@inheritDoc}
     */
    public String submitJob(JobDefinitionDocument dtsJob) {
        SubmitJobRequestDocument request = SubmitJobRequestDocument.Factory.newInstance();
        SubmitJobRequest submitJobRequest = request.addNewSubmitJobRequest();

        // replace JobDefinition with the one read from the input file
        request.getSubmitJobRequest().setJobDefinition(dtsJob.getJobDefinition());

        // TODO: filter out the credential info from the logs using the one that WN uses
        LOGGER.debug("request payload:\n" + request);

        // do the actual WS call here...
        SubmitJobResponseDocument response =
            (SubmitJobResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request, mRequestCallback);

        LOGGER.debug("response payload:\n" + response);

        return response.getSubmitJobResponse().getJobResourceKey();
    }

    /**
     * {@inheritDoc}
     */
    public void cancelJob(String jobResourceKey) {
        CancelJobRequestDocument request = CancelJobRequestDocument.Factory.newInstance();
        CancelJobRequest cancelJobRequest = request.addNewCancelJobRequest();
        cancelJobRequest.setJobResourceKey(jobResourceKey);

        // TODO: will this really not return anything back? how about just a confirmation that cancelJobRequest
        // was successful?
        mWebServiceTemplate.marshalSendAndReceive(request, mRequestCallback);
    }

    /**
     * {@inheritDoc}
     */
    public void suspendJob(String jobResourceKey) {
        SuspendJobRequestDocument request = SuspendJobRequestDocument.Factory.newInstance();
        SuspendJobRequest suspendJobRequest = request.addNewSuspendJobRequest();
        suspendJobRequest.setJobResourceKey(jobResourceKey);

        // TODO: will this really not return anything back? how about just a confirmation that suspendJobRequest
        // was successful?
        mWebServiceTemplate.marshalSendAndReceive(request, mRequestCallback);
    }

    /**
     * {@inheritDoc}
     */
    public void resumeJob(String jobResourceKey) {
        ResumeJobRequestDocument request = ResumeJobRequestDocument.Factory.newInstance();
        ResumeJobRequest resumeJobRequest = request.addNewResumeJobRequest();
        resumeJobRequest.setJobResourceKey(jobResourceKey);

        // TODO: will this really not return anything back? how about just a confirmation that resumeJobRequest
        // was successful?
        mWebServiceTemplate.marshalSendAndReceive(request, mRequestCallback);
    }

    /**
     * {@inheritDoc}
     */
    public String getJobStatus(String jobResourceKey) {
        GetJobStatusRequestDocument request = GetJobStatusRequestDocument.Factory.newInstance();
        GetJobStatusRequest getJobStatusRequest = request.addNewGetJobStatusRequest();
        getJobStatusRequest.setJobResourceKey(jobResourceKey);

        // do the actual WS call here...
        GetJobStatusResponseDocument response =
            (GetJobStatusResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request, mRequestCallback);

        LOGGER.debug("response payload:\n" + response);

        return response.getGetJobStatusResponse().getState().toString();
    }

    /**
     * Sets the web service template.
     *
     * @param webServiceTemplate the new web service template
     */
    public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        mWebServiceTemplate = webServiceTemplate;
    }

}
