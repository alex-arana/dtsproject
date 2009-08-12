package org.dataminx.dts.client.sws;

import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
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
     * @return the Universally Unique Identifier for the submitted job or null if an error occurred during the job
     * submission
     */
    String submitJob(JobDefinitionDocument dtsJob);

    /**
     * Cancel the job.
     *
     * @param jobResourceKey the Universally Unique Identifier (aka Global Unique Identifier) of the job to cancel
     */
    void cancelJob(String jobResourceKey);

    /**
     * Suspend the job.
     *
     * @param jobResourceKey the Universally Unique Identifier (aka Global Unique Identifier) of the job to suspend
     */
    void suspendJob(String jobResourceKey);

    /**
     * Resume the job.
     *
     * @param jobResourceKey the Universally Unique Identifier (aka Global Unique Identifier) of the job to resume
     */
    void resumeJob(String jobResourceKey);

    /**
     * Gets the job status.
     *
     * @param jobResourceKey the Universally Unique Identifier (aka Global Unique Identifier) of the job being
     * queried about
     * @return the job status
     */
    String getJobStatus(String jobResourceKey);


    /**
     * Sets the web service message callback if further processing is required on the web service message.
     *
     * @param wsMessageCallback the web service message callback
     */
    void setWebServiceMessageCallback(WebServiceMessageCallback wsMessageCallback);
}
