package org.dataminx.dts.service;

import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;

public interface DataTransferService {

	public abstract String submitJob(JobDefinitionType job);
	public abstract void cancelJob(String jobId);
	public abstract void suspendJob(String jobId);
	public abstract void resumeJob(String jobId);
	public abstract String getJobStatus(String jobId);

}
