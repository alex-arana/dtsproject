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

import org.dataminx.dts.common.util.SchemaUtils;

import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

/**
 * This is the Spring Batch Launcher for the DTS Worker Node.
 * 
 * @author Alex Arana
 * @author Gerson Galang
 */
public class DtsJobLauncher extends SimpleJobLauncher {

    private static final Log LOGGER = LogFactory.getLog(DtsJobLauncher.class);

    private DtsJobFactory mJobFactory;

    /**
     * Constructs a new instance of {@link DtsJobLauncher}.
     */
    public DtsJobLauncher() {

    }

    public JobExecution run(final String jobId, final JobDefinitionDocument job)
            throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException {
        final SubmitJobRequestDocument dtsJobRequest = SubmitJobRequestDocument.Factory.newInstance();
        //SubmitJobRequest submitJobRequest =
        dtsJobRequest.addNewSubmitJobRequest();

        // replace JobDefinition with the one read from the input file
        dtsJobRequest.getSubmitJobRequest().setJobDefinition(job.getJobDefinition());

        // TODO: filter out the credential info from the logs using the one that WN uses
        final String auditableRequest = SchemaUtils.getAuditableString(dtsJobRequest);
        LOGGER.debug("request payload:\n" + auditableRequest);

        // invoke the job factory to create a new job instance
        final DtsJob dtsJob = mJobFactory.createJob(jobId, dtsJobRequest);

        return run(dtsJob, new DefaultJobParametersConverter().getJobParameters(new Properties()));
    }

    public void setDtsJobFactory(final DtsJobFactory jobFactory) {
        mJobFactory = jobFactory;
    }
}
