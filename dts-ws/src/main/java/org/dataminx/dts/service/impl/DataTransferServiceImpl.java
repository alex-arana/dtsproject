package org.dataminx.dts.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dataminx.dts.service.*;
import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;

import java.util.Random;

public class DataTransferServiceImpl implements DataTransferService {

	protected final Log logger = LogFactory.getLog(getClass());

	private Random r = new Random();

	public String submitJob(JobDefinitionType job) {
		logger.debug("In DataTransferServiceImpl.submitJob, running job " +
				job.getJobDescription().getJobIdentification().getJobName() + "... ");

		return "http://" + Long.toString(Math.abs(r.nextLong()), 36);
	}

	public void cancelJob(String jobId) {
		logger.debug("In DataTransferServiceImpl.cancelJob, cancelling job " +
				jobId + "... ");
	}

	public void suspendJob(String jobId) {
		logger.debug("In DataTransferServiceImpl.suspendJob, suspending job " +
				jobId + "... ");
	}

	public void resumeJob(String jobId) {
		logger.debug("In DataTransferServiceImpl.resumeJob, resuming job " +
				jobId + "... ");
	}

	public String getJobStatus(String jobId) {
		logger.debug("In DataTransferServiceImpl.cancelJob, getting job status for " +
				jobId + "... ");
		return "running";
	}


}
