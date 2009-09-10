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
package org.dataminx.dts.client.sws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.ws.DtsFaultException;
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
import org.dataminx.schemas.dts.x2009.x07.messages.SuspendJobRequestDocument.SuspendJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;

/**
 * The Data Transfer Service WS Client Implementation.
 *
 * @author Gerson Galang
 */
public class DataTransferServiceClientImpl implements DataTransferServiceClient {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DataTransferServiceClientImpl.class);

    /** The Spring-WS web service template. */
    private WebServiceTemplate mWebServiceTemplate;

    /** The message callback to use if provided. */
    private WebServiceMessageCallback mWsMessageCallback;

    /**
     * Instantiates a new data transfer service client impl.
     */
    public DataTransferServiceClientImpl() {
    }

    /**
     * {@inheritDoc}
     */
    public String submitJob(JobDefinitionDocument dtsJob) {
        SubmitJobRequestDocument request = SubmitJobRequestDocument.Factory.newInstance();
        //SubmitJobRequest submitJobRequest =
        request.addNewSubmitJobRequest();

        // replace JobDefinition with the one read from the input file
        request.getSubmitJobRequest().setJobDefinition(dtsJob.getJobDefinition());

        // TODO: filter out the credential info from the logs using the one that WN uses
        LOGGER.debug("request payload:\n" + request);

        // do the actual WS call here...
        SubmitJobResponseDocument response = null;

        try {
            if (mWsMessageCallback != null) {
                // do authenticated connection to the WS
                response =
                    (SubmitJobResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request, mWsMessageCallback);
            }
            else {
                response = (SubmitJobResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request);
            }
        }
        // we won't try and catch SoapFaultClientException anymore as having a FaultMessageResolver would mean
        // that the resolve would handle all the faults thrown by the WS and map them to their respective exception
        // classes. so make things simple, we'll just catch the generic DtsFaultException here and throw it again...
        catch (DtsFaultException e) {
            LOGGER.error("A SOAPFault was thrown by the DTS Web Service. " + e.getMessage());
            throw e;
        }
        catch (WebServiceIOException e) {
            LOGGER.error("A WebServiceIOException was thrown by the DTS Web Service. " + e.getMessage());
            throw e;
        }

        LOGGER.debug("response payload:\n" + response);

        if (response == null)
            return null;

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
        if (mWsMessageCallback != null) {
            // do authenticated connection to the WS
            mWebServiceTemplate.marshalSendAndReceive(request, mWsMessageCallback);
        }
        else {
            mWebServiceTemplate.marshalSendAndReceive(request);
        }
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
        if (mWsMessageCallback != null) {
            // do authenticated connection to the WS
            mWebServiceTemplate.marshalSendAndReceive(request, mWsMessageCallback);
        }
        else {
            mWebServiceTemplate.marshalSendAndReceive(request);
        }
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
        if (mWsMessageCallback != null) {
            // do authenticated connection to the WS
            mWebServiceTemplate.marshalSendAndReceive(request, mWsMessageCallback);
        }
        else {
            mWebServiceTemplate.marshalSendAndReceive(request);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getJobStatus(String jobResourceKey) {
        LOGGER.debug("DataTransferServiceClientImpl getJobStatus()");
        LOGGER.debug("Getting job status of " + jobResourceKey);
        GetJobStatusRequestDocument request = GetJobStatusRequestDocument.Factory.newInstance();
        GetJobStatusRequest getJobStatusRequest = request.addNewGetJobStatusRequest();
        getJobStatusRequest.setJobResourceKey(jobResourceKey);

        // do the actual WS call here...
        GetJobStatusResponseDocument response = null;

        try {
            if (mWsMessageCallback != null) {
                // do authenticated connection to the WS
                response =
                    (GetJobStatusResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request, mWsMessageCallback);
            }
            else {
                response = (GetJobStatusResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request);
            }
        }
        // we won't try and catch SoapFaultClientException anymore as having a FaultMessageResolver would mean
        // that the resolve would handle all the faults thrown by the WS and map them to their respective exception
        // classes. so make things simple, we'll just catch the generic DtsFaultException here and throw it again...
        catch (DtsFaultException e) {
            LOGGER.error("A SOAPFault was thrown by the DTS Web Service. " + e.getMessage());
            throw e;
        }
        catch (WebServiceIOException e) {
            LOGGER.error("A WebServiceIOException was thrown by the DTS Web Service. " + e.getMessage());
            throw e;
        }

        LOGGER.debug("response payload:\n" + response);

        return response.getGetJobStatusResponse().getState().getValue().toString();
    }

    /**
     * Sets the web service template.
     *
     * @param webServiceTemplate the new web service template
     */
    public void setWebServiceTemplate(WebServiceTemplate webServiceTemplate) {
        mWebServiceTemplate = webServiceTemplate;
    }


    public void setWebServiceMessageCallback(WebServiceMessageCallback wsMessageCallback) {
        mWsMessageCallback = wsMessageCallback;
    }

}
