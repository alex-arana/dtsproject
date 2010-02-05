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
package org.dataminx.dts.ws;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.service.DataTransferService;
import org.dataminx.dts.ws.validator.DtsJobDefinitionValidator;
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
import org.ogf.schemas.dmi.x2008.x05.dmi.StatusValueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;

/**
 * The DataTransferServiceEndpoint processes the incoming XML messages and
 * produces a response from/to the DTS client.
 * 
 * @author Gerson Galang
 */
@Endpoint
public class DataTransferServiceEndpoint {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DataTransferServiceEndpoint.class);

    /** The data transfer service. */
    @Autowired
    private DataTransferService mDataTransferService;

    /**
     * The validator to be used in checking for the validity of the submitted
     * DTS job.
     */
    private DtsJobDefinitionValidator mDtsJobDefinitionValidator;

    /**
     * The reference to the message resolver so that validation error messages
     * are taken from a ResourceBundle.
     */
    private MessageSource mMessageSource;

    /** The DTS Messages namespace. */
    private static final String DTS_MESSAGES_NS = "http://schemas.dataminx.org/dts/2009/07/messages";

    private static final String SUBMIT_JOB_REQUEST_LOCAL_NAME = "submitJobRequest";
    private static final String CANCEL_JOB_REQUEST_LOCAL_NAME = "cancelJobRequest";
    private static final String SUSPEND_JOB_REQUEST_LOCAL_NAME = "suspendJobRequest";
    private static final String RESUME_JOB_REQUEST_LOCAL_NAME = "resumeJobRequest";
    private static final String GET_JOB_STATUS_REQUEST_LOCAL_NAME = "getJobStatusRequest";
    private static final String GET_JOB_DETAILS_REQUEST_LOCAL_NAME = "getJobDetailsRequest";

    /**
     * Handles the submit job request.
     * 
     * @param request the submit job request
     * 
     * @return the response to the submit job call
     */
    @PayloadRoot(localPart = SUBMIT_JOB_REQUEST_LOCAL_NAME, namespace = DTS_MESSAGES_NS)
    public SubmitJobResponseDocument doit(final SubmitJobRequestDocument request) {
        LOGGER.debug("DataTransferServiceEndpoint doit(SubmitJobRequest)");

        final MapBindingResult errors = new MapBindingResult(new HashMap(), "jobDefinitionErrors");

        mDtsJobDefinitionValidator.validate(request.getSubmitJobRequest().getJobDefinition(), errors);

        if (errors.hasErrors()) {
            //FieldError error = errors.getFieldError("jobIdentification.jobName");
            final List<FieldError> fieldErrors = errors.getFieldErrors();
            final StringBuffer validationErrors = new StringBuffer();
            String validationErrorMessage = "";
            for (final FieldError fieldError : fieldErrors) {
                validationErrorMessage = mMessageSource.getMessage(fieldError, Locale.getDefault());
                validationErrors.append(validationErrorMessage).append("\n");
            }
            throw new InvalidJobDefinitionException("Invalid job request\n" + validationErrors);
        }

        final String jobResourceKey = mDataTransferService.submitJob(request);
        LOGGER.info("New job '" + jobResourceKey + "' created.");

        final SubmitJobResponseDocument response = SubmitJobResponseDocument.Factory.newInstance();
        response.addNewSubmitJobResponse().setJobResourceKey(jobResourceKey);
        return response;
    }

    /**
     * Handles the cancel job request.
     * 
     * @param request the cancel job request
     */
    @PayloadRoot(localPart = CANCEL_JOB_REQUEST_LOCAL_NAME, namespace = DTS_MESSAGES_NS)
    public void doit(final CancelJobRequestDocument request) {
        LOGGER.debug("DataTransferServiceEndpoint doit(CancelJobRequest)");
        mDataTransferService.cancelJob(request);
    }

    /**
     * Handles the suspend job request.
     * 
     * @param request the suspend job request
     */
    @PayloadRoot(localPart = SUSPEND_JOB_REQUEST_LOCAL_NAME, namespace = DTS_MESSAGES_NS)
    public void doit(final SuspendJobRequestDocument request) {
        LOGGER.debug("DataTransferServiceEndpoint doit(SuspendJobRequest)");
        mDataTransferService.suspendJob(request);
    }

    /**
     * Handles the resume job request.
     * 
     * @param request the resume job request
     */
    @PayloadRoot(localPart = RESUME_JOB_REQUEST_LOCAL_NAME, namespace = DTS_MESSAGES_NS)
    public void doit(final ResumeJobRequestDocument request) {
        LOGGER.debug("DataTransferServiceEndpoint doit(ResumeJobRequest)");
        mDataTransferService.resumeJob(request);
    }

    /**
     * Handles the get job status request.
     * 
     * @param request the get job status request
     * 
     * @return the response to the get job status call
     */
    @PayloadRoot(localPart = GET_JOB_STATUS_REQUEST_LOCAL_NAME, namespace = DTS_MESSAGES_NS)
    public GetJobStatusResponseDocument doit(final GetJobStatusRequestDocument request) {
        LOGGER.debug("DataTransferServiceEndpoint doit(GetJobStatusRequest)");
        final String status = mDataTransferService.getJobStatus(request);
        final GetJobStatusResponseDocument response = GetJobStatusResponseDocument.Factory.newInstance();
        response.addNewGetJobStatusResponse().addNewState().setValue(StatusValueType.Enum.forString(status));
        return response;
    }

    /**
     * Handles the get job details request.
     * 
     * @param request the get job status request
     * 
     * @return the response to the get job status call
     */
    @PayloadRoot(localPart = GET_JOB_DETAILS_REQUEST_LOCAL_NAME, namespace = DTS_MESSAGES_NS)
    public GetJobDetailsResponseDocument doit(final GetJobDetailsRequestDocument request) {
        LOGGER.debug("DataTransferServiceEndpoint doit(GetJobDetailsRequest)");
        final Job foundJob = mDataTransferService.getJobDetails(request);

        final GetJobDetailsResponseDocument response = GetJobDetailsResponseDocument.Factory.newInstance();

        final JobDetailsType jobDetails = response.addNewGetJobDetailsResponse().addNewJobDetails();
        fillInJobDetails(foundJob, jobDetails);

        return response;
    }

    /**
     * Sets the {@link DtsJobDefinitionValidator}.
     * 
     * @param dtsJobDefinitionValidator the {@link DtsJobDefinitionValidator} to
     *        use
     */
    public void setDtsJobDefinitionValidator(final DtsJobDefinitionValidator dtsJobDefinitionValidator) {
        mDtsJobDefinitionValidator = dtsJobDefinitionValidator;
    }

    /**
     * Sets the {@link MessageSource}.
     * 
     * @param messageSource the {@link MessageSource} to use
     */
    public void setMessageSource(final MessageSource messageSource) {
        mMessageSource = messageSource;
    }

    /**
     * Fills in the JobDetails object with information about the job taken from
     * the WS DB.
     * 
     * @param job
     * @param jobDetails
     */
    private void fillInJobDetails(final Job job, final JobDetailsType jobDetails) {
        jobDetails.setJobResourceKey(job.getResourceKey());
        if (job.getName() != null) {
            jobDetails.setJobName(job.getName());
        }
        jobDetails.setStatus(StatusValueType.Enum.forString(job.getStatus().getStringValue()));
        if (job.getSubjectName() != null) {
            jobDetails.setOwner(job.getSubjectName());
        }
        if (job.getCreationTime() != null) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(job.getCreationTime());
            jobDetails.setCreationTime(cal);
        }
        if (job.getQueuedTime() != null) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(job.getCreationTime());
            jobDetails.setQueuedTime(cal);
        }
        if (job.getActiveTime() != null) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(job.getActiveTime());
            jobDetails.setQueuedTime(cal);
        }
        if (job.getSuccessFlag() != null) {
            jobDetails.setSuccessFlag(job.getSuccessFlag());
        }
        if (job.getFinishedFlag() != null) {
            jobDetails.setFinishedFlag(job.getFinishedFlag());
        }
        if (job.getWorkerTerminatedTime() != null) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(job.getWorkerTerminatedTime());
            jobDetails.setWorkerTerminatedTime(cal);
        }
        if (job.getJobAllDoneTime() != null) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(job.getJobAllDoneTime());
            jobDetails.setJobAllDoneTime(cal);
        }
        if (job.getFilesTotal() != null) {
            jobDetails.setFilesTotal(BigInteger.valueOf(job.getFilesTotal()));
        }
        if (job.getFilesTransferred() != null) {
            jobDetails.setFilesTransferred(BigInteger.valueOf(job.getFilesTransferred()));
        }
        if (job.getVolumeTotal() != null) {
            jobDetails.setVolumeTotal(BigInteger.valueOf(job.getVolumeTotal()));
        }
        if (job.getVolumeTransferred() != null) {
            jobDetails.setVolumeTransferred(BigInteger.valueOf(job.getVolumeTransferred()));
        }
    }

}
