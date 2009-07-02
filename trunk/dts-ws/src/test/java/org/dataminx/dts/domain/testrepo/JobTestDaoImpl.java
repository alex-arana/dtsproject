package org.dataminx.dts.domain.testrepo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.domain.repo.JobDao;

/**
 * A Job repository implementation to be used by the WS layer if it runs on standalone mode.
 *
 * @author Gerson Galang
 */
public class JobTestDaoImpl implements JobDao {

    /** The pre-generated UUID used for testing purposes. */
    private static final String PRE_GENERATED_UUID = "66296c5e-9a34-4e24-8fa2-4fef329e084e";

    /** The test jobs. */
    private final Map<String, Job> mDummyJobs;

    public JobTestDaoImpl() {

        // create a map of dummy jobs being run by the WS which could be queried later on.
        mDummyJobs = new HashMap<String, Job>();

        // then, create other dummy jobs to be run by the DTS WS
        // for convenience sake, there will be other 9 dummy jobs
        for (int i = 0; i < 9; i++) {
            Job dummyJob = new Job();
            dummyJob.setResourceKey(UUID.randomUUID().toString());
            dummyJob.setName("job_000" + i);
            dummyJob.setStatus(JobStatus.CREATED);
            mDummyJobs.put(dummyJob.getResourceKey(), dummyJob);
        }
    }

    public Job findById(Long id) {
        return null;
    }


    public Job findByResourceKey(String resourceKey) {
        return mDummyJobs.get(resourceKey);
    }


    public void saveOrUpdate(Job job) {
        if (!mDummyJobs.containsKey(job.getResourceKey())) {
            // save the job in the Map..

            // reset the resource key to the one that the test expects
            job.setResourceKey(PRE_GENERATED_UUID);
            mDummyJobs.put(job.getResourceKey(), job);
        }
        // else don't do anything else since changes to the job will get reflected
        // on the object in the map anyway
    }


    public List<Job> findByUser(String subjectName) {
        return null;
    }


    public List<Job> findByUserAndStatus(String subjectName, JobStatus status) {
        return null;
    }
}
