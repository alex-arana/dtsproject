package org.dataminx.dts.client;

import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;

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
     * @return the Universally Unique Identifier for the submitted job
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
}
