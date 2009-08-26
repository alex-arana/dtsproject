package org.dataminx.dts.ws;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.service.DataTransferService;
import org.dataminx.dts.ws.validator.DtsJobDefinitionValidator;
import org.dataminx.schemas.dts.x2009.x07.messages.CancelJobRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusRequestDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.GetJobStatusResponseDocument;
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
    @Autowired
    private DataTransferService mDataTransferService;

    /** The validator to be used in checking for the validity of the submitted DTS job. */
    private DtsJobDefinitionValidator mDtsJobDefinitionValidator;

    /** The reference to the message resolver so that validation error messages are taken from a ResourceBundle. */
    private MessageSource mMessageSource;

    /** The DTS Messages namespace. */
    private static final String DTS_MESSAGES_NS = "http://schemas.dataminx.org/dts/2009/07/messages";

    private static final String SUBMIT_JOB_REQUEST_LOCAL_NAME = "submitJobRequest";
    private static final String CANCEL_JOB_REQUEST_LOCAL_NAME = "cancelJobRequest";
    private static final String SUSPEND_JOB_REQUEST_LOCAL_NAME = "suspendJobRequest";
    private static final String RESUME_JOB_REQUEST_LOCAL_NAME = "resumeJobRequest";
    private static final String GET_JOB_STATUS_REQUEST_LOCAL_NAME = "getJobStatusRequest";


    /**
     * Handles the submit job request.
     *
     * @param request the submit job request
     *
     * @return the response to the submit job call
     */
    @PayloadRoot(localPart = SUBMIT_JOB_REQUEST_LOCAL_NAME, namespace = DTS_MESSAGES_NS)
    public SubmitJobResponseDocument doit(SubmitJobRequestDocument request) {
        LOGGER.debug("DataTransferServiceEndpoint doit(SubmitJobRequest)");

        MapBindingResult errors = new MapBindingResult(new HashMap(), "jobDefinitionErrors");

        mDtsJobDefinitionValidator.validate(
                request.getSubmitJobRequest().getJobDefinition(), errors);

        if (errors.hasErrors()) {
            //FieldError error = errors.getFieldError("jobIdentification.jobName");
            List<FieldError> fieldErrors = errors.getFieldErrors();
            StringBuffer validationErrors = new StringBuffer();
            String validationErrorMessage = "";
            for (FieldError fieldError : fieldErrors) {
                validationErrorMessage = mMessageSource.getMessage(fieldError, Locale.getDefault());
                validationErrors.append(validationErrorMessage).append("\n");
            }
            throw new InvalidJobDefinitionException("Invalid job request\n" + validationErrors);
        }

        String jobResourceKey = mDataTransferService.submitJob(request);
        LOGGER.info("New job '" + jobResourceKey + "' created.");

        SubmitJobResponseDocument response = SubmitJobResponseDocument.Factory.newInstance();
        response.addNewSubmitJobResponse().setJobResourceKey(jobResourceKey);
        return response;
    }

    /**
     * Handles the cancel job request.
     *
     * @param request the cancel job request
     */
    @PayloadRoot(localPart = CANCEL_JOB_REQUEST_LOCAL_NAME, namespace = DTS_MESSAGES_NS)
    public void doit(CancelJobRequestDocument request) {
        LOGGER.debug("DataTransferServiceEndpoint doit(CancelJobRequest)");
        mDataTransferService.cancelJob(request);
    }

    /**
     * Handles the suspend job request.
     *
     * @param request the suspend job request
     */
    @PayloadRoot(localPart = SUSPEND_JOB_REQUEST_LOCAL_NAME, namespace = DTS_MESSAGES_NS)
    public void doit(SuspendJobRequestDocument request) {
        LOGGER.debug("DataTransferServiceEndpoint doit(SuspendJobRequest)");
        mDataTransferService.suspendJob(request);
    }

    /**
     * Handles the resume job request.
     *
     * @param request the resume job request
     */
    @PayloadRoot(localPart = RESUME_JOB_REQUEST_LOCAL_NAME, namespace = DTS_MESSAGES_NS)
    public void doit(ResumeJobRequestDocument request) {
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
    public GetJobStatusResponseDocument doit(GetJobStatusRequestDocument request) {
        LOGGER.debug("DataTransferServiceEndpoint doit(GetJobStatusRequest)");
        String status = mDataTransferService.getJobStatus(request);
        GetJobStatusResponseDocument response = GetJobStatusResponseDocument.Factory.newInstance();
        response.addNewGetJobStatusResponse().addNewState().setValue(StatusValueType.Enum.forString(status));
        return response;
    }

    /**
     * Sets the {@link DtsJobDefinitionValidator}.
     *
     * @param dtsJobDefinitionValidator the {@link DtsJobDefinitionValidator} to use
     */
    public void setDtsJobDefinitionValidator(DtsJobDefinitionValidator dtsJobDefinitionValidator) {
        mDtsJobDefinitionValidator = dtsJobDefinitionValidator;
    }

    /**
     * Sets the {@link MessageSource}.
     *
     * @param messageSource the {@link MessageSource} to use
     */
    public void setMessageSource(MessageSource messageSource) {
        mMessageSource = messageSource;
    }

}
