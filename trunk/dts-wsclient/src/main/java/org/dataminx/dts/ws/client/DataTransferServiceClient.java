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

import org.dataminx.schemas.dts.x2009.x07.messages.JobDetailsType;
//import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument;
import org.springframework.ws.client.core.WebServiceMessageCallback;

/**
 * The Data Transfer Service WS Client.
 * 
 * @author Gerson Galang
 */
public interface DataTransferServiceClient {

    /**
     * Submit the DTS Job.
     * 
     * @param dtsJob the DTS Job definition
     * @return the Universally Unique Identifier for the submitted job or null
     *         if an error occurred during the job submission
     */
    String submitJob(SubmitJobRequestDocument dtsJob);

    /**
     * Cancel the job.
     * 
     * @param jobResourceKey the Universally Unique Identifier (aka Global
     *        Unique Identifier) of the job to cancel
     */
    void cancelJob(String jobResourceKey);

    /**
     * Suspend the job.
     * 
     * @param jobResourceKey the Universally Unique Identifier (aka Global
     *        Unique Identifier) of the job to suspend
     */
    void suspendJob(String jobResourceKey);

    /**
     * Resume the job.
     * 
     * @param jobResourceKey the Universally Unique Identifier (aka Global
     *        Unique Identifier) of the job to resume
     */
    void resumeJob(String jobResourceKey);

    /**
     * Gets the job status.
     * 
     * @param jobResourceKey the Universally Unique Identifier (aka Global
     *        Unique Identifier) of the job being queried about
     * @return the job status
     */
    String getJobStatus(String jobResourceKey);

    /**
     * Get the job details.
     * 
     * @param jobResourceKeythe Universally Unique Identifier (aka Global Unique
     *        Identifier) of the job being queried about
     * @return the job details
     */
    JobDetailsType getJobDetails(String jobResourceKey);

    /**
     * Sets the web service message callback if further processing is required
     * on the web service message.
     * 
     * @param wsMessageCallback the web service message callback
     */
    void setWebServiceMessageCallback(WebServiceMessageCallback wsMessageCallback);
}
