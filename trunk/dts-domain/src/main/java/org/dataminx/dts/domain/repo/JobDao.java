package org.dataminx.dts.domain.repo;

import java.util.List;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;

public interface JobDao extends Dao {

	public abstract Job findById(Long id);

	public abstract Job findByResourceKey(String resourceKey);

	public abstract void saveOrUpdate(Job job);

	public abstract List<Job> findByUser(String subjectName);

	public abstract List<Job> findByUserAndStatus(String subjectName, JobStatus status);

	// find all pending jobs by user
	// find all jobs owned by user
	// find all running jobs by user

}
