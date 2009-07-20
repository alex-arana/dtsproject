package org.dataminx.dts.domain.repo;

import java.util.List;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;

/**
 * The Job Data Access Object. This provides operations for accessing and making changes to the Job entity.
 *
 * @author Gerson Galang
 */
public interface JobDao extends Dao {

    /**
     * Find the job given its id.
     *
     * @param id the job identifier
     * @return the job
     */
    Job findById(Long id);

    /**
     * Find the job given its resource key.
     *
     * @param resourceKey the job's resource key
     * @return the job
     */
    Job findByResourceKey(String resourceKey);

    /**
     * Save or update the changes to the Job.
     *
     * @param job the job to be saved or updated
     */
    void saveOrUpdate(Job job);

    /**
     * Find all the jobs owned by the user given his/her DN (Distinguished Name).
     *
     * @param subjectName the user's DN (Distinguished Name)
     * @return the list< job> of jobs that matched the selection criteria
     */
    List<Job> findByUser(String subjectName);

    /**
     * Find by jobs having the following status and owned by the user with DN subjectName.
     *
     * @param subjectName the user's DN (Distinguished Name)
     * @param status the status of the job
     * @return the list< job> of jobs that matched the selection criteria
     */
    List<Job> findByUserAndStatus(String subjectName, JobStatus status);

    // find all pending jobs by user
    // find all jobs owned by user
    // find all running jobs by user

}
