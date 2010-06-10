/**
 * Copyright (c) 2009, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
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
package org.dataminx.dts.batch;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.common.util.SchemaUtils;
import org.dataminx.dts.common.validator.DtsJobDefinitionValidator;
import org.dataminx.dts.common.ws.InvalidJobDefinitionException;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.MessageSource;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;

/**
 * This is the Spring Batch Launcher for the DTS Worker Node.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
public class DtsJobLauncher extends SimpleJobLauncher {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DtsJobLauncher.class);

    /** A reference to the DtsJobFactory. */
    private DtsJobFactory mJobFactory;

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

    /**
     * Constructs a new instance of {@link DtsJobLauncher}.
     */
    public DtsJobLauncher() {

    }

    /**
     * An overloaded version of the {@link SimpleJobLauncher#run(org.springframework.batch.core.Job,
     * org.springframework.batch.core.JobParameters) run} method which launches
     * a DTS Job.
     *
     * @param jobId the jobResourceKey
     * @param job the JobDefinitionDocument
     * @return the JobExecution if job launch is successful
     * @throws JobExecutionAlreadyRunningException if the job is already running
     * @throws JobRestartException if the job is not allowed to be restarted
     * @throws JobInstanceAlreadyCompleteException if the job has already completed
     */
    public JobExecution run(final String jobId, final JobDefinitionDocument job)
        throws JobExecutionAlreadyRunningException, JobRestartException,
        JobInstanceAlreadyCompleteException, InvalidJobDefinitionException {

        final MapBindingResult errors = new MapBindingResult(new HashMap(),
            "jobDefinitionErrors");

        mDtsJobDefinitionValidator.validate(job.getJobDefinition(), errors);

        if (errors.hasErrors()) {
            //FieldError error = errors.getFieldError("jobIdentification.jobName");
            final List<FieldError> fieldErrors = errors.getFieldErrors();
            final StringBuffer validationErrors = new StringBuffer();
            String validationErrorMessage = "";
            for (final FieldError fieldError : fieldErrors) {
                validationErrorMessage = mMessageSource.getMessage(fieldError,
                    Locale.getDefault());
                validationErrors.append(validationErrorMessage).append("\n");
            }
            throw new InvalidJobDefinitionException("Invalid job request\n"
                + validationErrors);
        }

        final SubmitJobRequestDocument dtsJobRequest = SubmitJobRequestDocument.Factory
            .newInstance();
        //SubmitJobRequest submitJobRequest =
        dtsJobRequest.addNewSubmitJobRequest();

        // replace JobDefinition with the one read from the input file
        dtsJobRequest.getSubmitJobRequest().setJobDefinition(
            job.getJobDefinition());

        // TODO: filter out the credential info from the logs using the one that WN uses
        final String auditableRequest = SchemaUtils
            .getAuditableString(dtsJobRequest);
        LOGGER.debug("request payload:\n" + auditableRequest);

        final long maxAttempts = SchemaUtils.getMaxAttempts(dtsJobRequest
            .getSubmitJobRequest());
        // invoke the job factory to create a new job instance
        final DtsFileTransferJob dtsJob = mJobFactory.createJob(jobId,
            dtsJobRequest);
        final JobParameters paras = new JobParametersBuilder().addLong(
            "maxAttempts", maxAttempts).toJobParameters();
        return run(dtsJob, paras);
    }

    public void setDtsJobFactory(final DtsJobFactory jobFactory) {
        mJobFactory = jobFactory;
    }

    /**
     * Sets the {@link DtsJobDefinitionValidator}.
     *
     * @param dtsJobDefinitionValidator the {@link DtsJobDefinitionValidator} to
     *        use
     */
    public void setDtsJobDefinitionValidator(
        final DtsJobDefinitionValidator dtsJobDefinitionValidator) {
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
}
