/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.batch;

/**
 * Strategy for creating a DTS jobs.
 *
 * @author Alex Arana
 */
public interface DtsJobFactory {

    /**
     * Factory method for creating an instance of {@link DtsJob} based on a given object criteria.
     *
     * @param jobId Unique job identifier string
     * @param criteria Criteria for creating a DTS Job
     * @return A new instance of {@link DtsJob}
     */
    DtsJob createJob(String jobId, Object criteria);
}
