package org.dataminx.dts.domain.jparepo;

import java.util.List;
import java.util.UUID;
import junit.framework.Assert;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.domain.repo.JobDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The JobJpaDaoImpl Unit Test.
 */
@ContextConfiguration(locations = { "/applicationContext.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class JobJpaDaoImplTest {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JobJpaDaoImplTest.class);

    /** The Global Unique Id to be searched for. */
    private static final String SEARCH_GUID = UUID.randomUUID().toString();

    /** The job repository. */
    @Autowired
    private JobDao mJobRepository;

    /**
     * Creates the job entry.
     */
    @Test
    public void createJobEntry() {
        Job job = null;
        job = new Job();
        job.setName("job1");
        job.setResourceKey(SEARCH_GUID);
        job.setStatus(JobStatus.CREATED);
        job.setSubjectName("TEST_DN");
        mJobRepository.saveOrUpdate(job);
        Assert.assertNotNull(job.getJobId());
        for (int i = 2; i <= 10; i++) {
            job = new Job();
            job.setName("job" + i);
            job.setResourceKey(UUID.randomUUID().toString());
            job.setStatus(JobStatus.CREATED);
            job.setSubjectName("OTHER_DN");
            mJobRepository.saveOrUpdate(job);
        }
    }

    /**
     * Find job by resource key.
     */
    @Test
    public void findJobByResourceKey() {
        Job job = mJobRepository.findByResourceKey(SEARCH_GUID);
        Assert.assertEquals("job1", job.getName());
    }

    /**
     * Find and update.
     */
    @Test
    public void findAndUpdate() {
        Job job = mJobRepository.findByResourceKey(SEARCH_GUID);
        Assert.assertEquals("TEST_DN", job.getSubjectName());
        job.setSubjectName("TEST_NEW_DN");
        job.setStatus(JobStatus.DONE);
        mJobRepository.saveOrUpdate(job);

        // try and get job again
        job = mJobRepository.findByResourceKey(SEARCH_GUID);
        Assert.assertEquals("TEST_NEW_DN", job.getSubjectName());
    }

    /**
     * Find job by user.
     */
    @Test
    public void findJobByUser() {
        List<Job> jobs = mJobRepository.findByUser("TEST_NEW_DN");
        Assert.assertEquals(1, jobs.size());
    }

    /**
     * Find job by user and status.
     */
    @Test
    public void findJobByUserAndStatus() {
        List<Job> jobs = mJobRepository.findByUserAndStatus("TEST_NEW_DN", JobStatus.DONE);
        Assert.assertEquals(1, jobs.size());
    }




}
