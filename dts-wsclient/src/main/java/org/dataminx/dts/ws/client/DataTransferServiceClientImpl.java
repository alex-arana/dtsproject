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
package org.dataminx.dts.ws.client;

import org.dataminx.dts.common.ws.DtsFaultException;

import org.dataminx.dts.common.util.SchemaUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobDetailsRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobDetailsResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.JobDetailsType;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobResponseDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SuspendJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument.CancelJobRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobDetailsRequestDocument.GetJobDetailsRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument.GetJobStatusRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.ResumeJobRequestDocument.ResumeJobRequest;
import org.dataminx.schemas.dts.x2009.x07.messages.SuspendJobRequestDocument.SuspendJobRequest;
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
    public String submitJob(final SubmitJobRequestDocument request) {
        
        // TODO: filter out the credential info from the logs using the one that WN uses
        final String auditableRequest = SchemaUtils.getAuditableString(request);
        LOGGER.debug("request payload:\n" + auditableRequest);

        // do the actual WS call here...
        SubmitJobResponseDocument response = null;

        try {
            if (mWsMessageCallback != null) {
                // do authenticated connection to the WS
                response = (SubmitJobResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request,
                        mWsMessageCallback);
            }
            else {
                response = (SubmitJobResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request);
            }
        }
        // we won't try and catch SoapFaultClientException anymore as having a FaultMessageResolver would mean
        // that the resolve would handle all the faults thrown by the WS and map them to their respective exception
        // classes. so make things simple, we'll just catch the generic DtsFaultException here and throw it again...
        catch (final DtsFaultException e) {
            LOGGER.error("A SOAPFault was thrown by the DTS Web Service. " + e.getMessage());
            throw e;
        } catch (final WebServiceIOException e) {
            LOGGER.error("A WebServiceIOException was thrown by the DTS Web Service. " + e.getMessage());
            throw e;
        }

        LOGGER.debug("response payload:\n" + response);

        if (response == null) {
            return null;
        }

        return response.getSubmitJobResponse().getJobResourceKey();
    }

    /**
     * {@inheritDoc}
     */
    public void cancelJob(final String jobResourceKey) {
        final CancelJobRequestDocument request = CancelJobRequestDocument.Factory.newInstance();
        final CancelJobRequest cancelJobRequest = request.addNewCancelJobRequest();
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
    public void suspendJob(final String jobResourceKey) {
        final SuspendJobRequestDocument request = SuspendJobRequestDocument.Factory.newInstance();
        final SuspendJobRequest suspendJobRequest = request.addNewSuspendJobRequest();
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
    public void resumeJob(final String jobResourceKey) {
        final ResumeJobRequestDocument request = ResumeJobRequestDocument.Factory.newInstance();
        final ResumeJobRequest resumeJobRequest = request.addNewResumeJobRequest();
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
    public String getJobStatus(final String jobResourceKey) {
        LOGGER.debug("DataTransferServiceClientImpl getJobStatus()");
        LOGGER.debug("Getting job status of job " + jobResourceKey);
        final GetJobStatusRequestDocument request = GetJobStatusRequestDocument.Factory.newInstance();
        final GetJobStatusRequest getJobStatusRequest = request.addNewGetJobStatusRequest();
        getJobStatusRequest.setJobResourceKey(jobResourceKey);

        // do the actual WS call here...
        GetJobStatusResponseDocument response = null;

        try {
            if (mWsMessageCallback != null) {
                // do authenticated connection to the WS
                response = (GetJobStatusResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request,
                        mWsMessageCallback);
            }
            else {
                response = (GetJobStatusResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request);
            }
        }
        // we won't try and catch SoapFaultClientException anymore as having a FaultMessageResolver would mean
        // that the resolve would handle all the faults thrown by the WS and map them to their respective exception
        // classes. so make things simple, we'll just catch the generic DtsFaultException here and throw it again...
        catch (final DtsFaultException e) {
            LOGGER.error("A SOAPFault was thrown by the DTS Web Service. " + e.getMessage());
            throw e;
        } catch (final WebServiceIOException e) {
            LOGGER.error("A WebServiceIOException was thrown by the DTS Web Service. " + e.getMessage());
            throw e;
        }

        LOGGER.debug("response payload:\n" + response);

        return response.getGetJobStatusResponse().getState().getValue().toString();
    }

    /**
     * {@inheritDoc}
     */
    public JobDetailsType getJobDetails(final String jobResourceKey) {
        LOGGER.debug("DataTransferServiceClientImpl jobResourceKey()");
        LOGGER.debug("Getting job details of job " + jobResourceKey);
        final GetJobDetailsRequestDocument request = GetJobDetailsRequestDocument.Factory.newInstance();
        final GetJobDetailsRequest getJobDetailsRequest = request.addNewGetJobDetailsRequest();
        getJobDetailsRequest.setJobResourceKey(jobResourceKey);

        // do the actual WS call here...
        GetJobDetailsResponseDocument response = null;

        try {
            if (mWsMessageCallback != null) {
                // do authenticated connection to the WS
                response = (GetJobDetailsResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request,
                        mWsMessageCallback);
            }
            else {
                response = (GetJobDetailsResponseDocument) mWebServiceTemplate.marshalSendAndReceive(request);
            }
        }
        // we won't try and catch SoapFaultClientException anymore as having a FaultMessageResolver would mean
        // that the resolve would handle all the faults thrown by the WS and map them to their respective exception
        // classes. so make things simple, we'll just catch the generic DtsFaultException here and throw it again...
        catch (final DtsFaultException e) {
            LOGGER.error("A SOAPFault was thrown by the DTS Web Service. " + e.getMessage());
            throw e;
        } catch (final WebServiceIOException e) {
            LOGGER.error("A WebServiceIOException was thrown by the DTS Web Service. " + e.getMessage());
            throw e;
        }

        LOGGER.debug("response payload:\n" + response);

        // TODO Auto-generated method stub
        return response.getGetJobDetailsResponse().getJobDetails();
    }

    /**
     * Sets the web service template.
     * 
     * @param webServiceTemplate the new web service template
     */
    public void setWebServiceTemplate(final WebServiceTemplate webServiceTemplate) {
        mWebServiceTemplate = webServiceTemplate;
    }

    public void setWebServiceMessageCallback(final WebServiceMessageCallback wsMessageCallback) {
        mWsMessageCallback = wsMessageCallback;
    }

}
