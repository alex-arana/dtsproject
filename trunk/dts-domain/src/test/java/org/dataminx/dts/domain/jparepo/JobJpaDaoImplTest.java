package org.dataminx.dts.domain.jparepo;

import java.util.List;
import java.util.Random;
import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.domain.repo.JobDao;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = { "/applicationContext.xml" })
@RunWith(SpringJUnit4ClassRunner.class)
public class JobJpaDaoImplTest {

    protected final Log logger = LogFactory.getLog(getClass());

    private final Random r = new Random();

    @Autowired
    private JobDao jobRepository;

    @Test
    public void createJobEntry() {
        Job job = null;
        job = new Job();
        job.setJobName("job1");
        job.setJobResourceKey("http://testdomain");
        job.setStatus(JobStatus.CREATED);
        job.setSubjectName("TEST_DN");
        jobRepository.saveOrUpdate(job);
        Assert.assertNotNull(job.getJobId());
        for (int i = 2; i <= 10; i++) {
            job = new Job();
            job.setJobName("job" + i);
            job.setJobResourceKey("http://" + Long.toString(Math.abs(r.nextLong()), 36));
            job.setStatus(JobStatus.CREATED);
            job.setSubjectName("OTHER_DN");
            jobRepository.saveOrUpdate(job);
        }

    }

    @Test
    public void findJobByResourceKey() {
        Job job = jobRepository.findByResourceKey("http://testdomain");
        Assert.assertEquals("job1", job.getJobName());
    }

    @Test
    public void findAndUpdate() {
        Job job = jobRepository.findByResourceKey("http://testdomain");
        Assert.assertEquals("TEST_DN", job.getSubjectName());
        job.setSubjectName("TEST_NEW_DN");
        job.setStatus(JobStatus.DONE);
        jobRepository.saveOrUpdate(job);

        // try and get job again
        job = jobRepository.findByResourceKey("http://testdomain");
        Assert.assertEquals("TEST_NEW_DN", job.getSubjectName());
    }

    @Test
    public void findJobByUser() {
        List<Job> jobs = jobRepository.findByUser("TEST_NEW_DN");
        Assert.assertEquals(1, jobs.size());
    }

    @Test
    public void findJobByUserAndStatus() {
        List<Job> jobs = jobRepository.findByUserAndStatus("TEST_NEW_DN", JobStatus.DONE);
        Assert.assertEquals(1, jobs.size());
    }




}
