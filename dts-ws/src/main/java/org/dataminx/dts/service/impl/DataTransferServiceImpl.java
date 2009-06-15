package org.dataminx.dts.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.service.DataTransferService;
import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;
import org.dataminx.schemas.dts._2009._05.dts.StatusValueEnumeration;

public class DataTransferServiceImpl implements DataTransferService {

	protected final Log logger = LogFactory.getLog(getClass());

	private final Random r = new Random();

	private final Map<String,TestJob> testJobs;

	// just for testing...
	public DataTransferServiceImpl() {
	    testJobs = new HashMap<String,TestJob>();
	    TestJob job;
	    job = new TestJob();
	    job.setId("http://testjob");
	    job.setName("Job0002");
	    job.setStatus(StatusValueEnumeration.CREATED);
	    testJobs.put(job.getId(), job);
	    for (int i = 3; i < 7; i++) {
	        job = new TestJob();
	        job.setId("http://" + Long.toString(Math.abs(r.nextLong()), 36));
	        job.setName("Job000" + i);
	        job.setStatus(StatusValueEnumeration.CREATED);
	        testJobs.put(job.getId(), job);
	    }
	}

	public String submitJob(JobDefinitionType job) {
		logger.debug("In DataTransferServiceImpl.submitJob, running job " +
				job.getJobDescription().getJobIdentification().getJobName() + "... ");
		TestJob testJob = new TestJob();
		testJob.setId("http://" + Long.toString(Math.abs(r.nextLong()), 36));
		testJob.setName(job.getJobDescription().getJobIdentification().getJobName());
		testJob.setStatus(StatusValueEnumeration.CREATED);
		testJobs.put(testJob.getId(), testJob);
		return testJob.getId();
	}

	public void cancelJob(String jobId) {
		logger.debug("In DataTransferServiceImpl.cancelJob, cancelling job " +
				jobId + "... ");
		TestJob job = testJobs.get(jobId);
		job.setStatus(StatusValueEnumeration.DONE);

	}

	public void suspendJob(String jobId) {
		logger.debug("In DataTransferServiceImpl.suspendJob, suspending job " +
				jobId + "... ");
		TestJob job = testJobs.get(jobId);
        job.setStatus(StatusValueEnumeration.SUSPENDED);
	}

	public void resumeJob(String jobId) {
		logger.debug("In DataTransferServiceImpl.resumeJob, resuming job " +
				jobId + "... ");
		TestJob job = testJobs.get(jobId);
		job.setStatus(StatusValueEnumeration.TRANSFERRING);
	}

	public String getJobStatus(String jobId) {
		logger.debug("In DataTransferServiceImpl.cancelJob, getting job status for " +
				jobId + "... ");
		logger.debug("job status = " + testJobs.get(jobId).getStatus());
		return testJobs.get(jobId).getStatus().value();
	}

	public class TestJob {
	    private String id;
	    private String name;
	    private StatusValueEnumeration status;
	    public String getId() {
	        return id;
	    }
	    public void setId(String id) {
	        this.id = id;
	    }
	    public String getName() {
	        return name;
	    }
	    public void setName(String name) {
	        this.name = name;
	    }
	    public StatusValueEnumeration getStatus() {
	        return status;
	    }
	    public void setStatus(StatusValueEnumeration status) {
	        this.status = status;
	    }
	}
}
