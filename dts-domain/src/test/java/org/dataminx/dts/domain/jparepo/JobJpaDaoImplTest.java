package org.dataminx.dts.domain.jparepo;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import junit.framework.Assert;
import org.dataminx.dts.domain.model.Job;
import org.dataminx.dts.domain.model.JobStatus;
import org.dataminx.dts.domain.repo.JobDao;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * The JobJpaDaoImpl Unit Test.
 *
 * @author Gerson Galang
 */
public class JobJpaDaoImplTest extends AbstractTransactionalDataSourceSpringContextTests {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JobJpaDaoImplTest.class);

    /** The Global Unique Id to be searched for. */
    private static final String SEARCH_GUID = "505f48c9-d31f-4c68-ba7b-1c69db06107a";

    /** The job repository. */
    private final JobDao mJobRepository;

    public JobJpaDaoImplTest() {
        super();
        LOGGER.info("setting up test in JobJpaDaoImplTest constructor");
        ApplicationContext ctx = super.getApplicationContext();
        mJobRepository = (JobDao) ctx.getBean("jobRepository");
        assertNotNull(mJobRepository);
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"testdomain-context.xml"};
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        DataSource dataSource = jdbcTemplate.getDataSource();
        Connection con = DataSourceUtils.getConnection(dataSource);
        IDatabaseConnection dbUnitCon = new DatabaseConnection(con);
        IDataSet dataSet = new FlatXmlDataSet(
            new FileInputStream("./src/test/resources/dbunit-test-data/JobJpaDaoImpl.xml"));
        try {
            DatabaseOperation.REFRESH.execute(dbUnitCon, dataSet);
        }
        finally {
            DataSourceUtils.releaseConnection(con, dataSource);
        }
    }

    /**
     * Find job by resource key.
     */
    @Test
    public void testFindJobByResourceKey() {
        Job job = mJobRepository.findByResourceKey(SEARCH_GUID);
        Assert.assertEquals("job1", job.getName());
    }

    /**
     * Find job by user.
     */
    @Test
    public void testFindJobByUser() {
        List<Job> jobs = mJobRepository.findByUser("NEW_USER_1");
        Assert.assertEquals("did not get expected number of entities", 3, jobs.size());
    }

    /**
     * Find job by user and status.
     */
    @Test
    public void testFindJobByUserAndStatus() {
        List<Job> jobs = mJobRepository.findByUserAndStatus("NEW_USER_1", JobStatus.DONE);
        Assert.assertEquals("did not get expected number of entities", 1, jobs.size());

        jobs = mJobRepository.findByUserAndStatus("NEW_USER_2", JobStatus.DONE);
        Assert.assertEquals("did not get expected number of entities", 0, jobs.size());
    }

    /**
     * Creates the job entry.
     */
    @Test
    public void testCreateJobEntry() {
        Job job = null;
        job = new Job();
        job.setName("job6");
        String newResourceKey = UUID.randomUUID().toString();
        job.setResourceKey(newResourceKey);
        job.setStatus(JobStatus.CREATED);
        job.setSubjectName("NEW_USER_2");
        mJobRepository.saveOrUpdate(job);

        List<Job> jobs = mJobRepository.findByUser("NEW_USER_2");
        Assert.assertEquals("did not get expected number of entities", 3, jobs.size());
        Assert.assertNotNull("entity did not get persisted", job.getJobId());
    }

    /**
     * Find and update.
     */
    @Test
    public void testFindAndUpdate() {
        Job job = mJobRepository.findByResourceKey(SEARCH_GUID);
        Assert.assertEquals("NEW_USER_1", job.getSubjectName());

        job.setStatus(JobStatus.DONE);
        mJobRepository.saveOrUpdate(job);

        List<Job> jobs = mJobRepository.findByUserAndStatus("NEW_USER_1", JobStatus.DONE);
        Assert.assertEquals("did not get expected number of entities", 2, jobs.size());
    }

}
