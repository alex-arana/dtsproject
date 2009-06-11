package org.dataminx.dts.domain.repo;

import org.dataminx.dts.domain.model.Job;

public interface JobDao extends Dao {

	public abstract Job findById(Long id);

	public abstract Job findByResourceKey(String resourceKey);

	public abstract void saveOrUpdate(Job job);

	// find all pending jobs by user
	// find all jobs owned by user
	// find all running jobs by user

}
