/**
 * Copyright (c) 2009, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.domain.jparepo;

import java.io.FileInputStream;
import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.dataminx.dts.common.model.JobStatus;
import org.dataminx.dts.ws.model.Job;
import org.dataminx.dts.ws.repo.JobDao;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * The JobJpaDaoImpl Unit Test.
 *
 * @author Gerson Galang
 */
public class JobJpaDaoImplTest extends
    AbstractTransactionalDataSourceSpringContextTests {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(JobJpaDaoImplTest.class);

    /** The Global Unique Id to be searched for. */
    private static final String SEARCH_GUID = "505f48c9-d31f-4c68-ba7b-1c69db06107a";

    /** The job repository. */
    private final JobDao mJobRepository;

    public JobJpaDaoImplTest() {
        super();
        LOGGER.info("setting up test in JobJpaDaoImplTest constructor");
        final ApplicationContext ctx = super.getApplicationContext();
        mJobRepository = (JobDao) ctx.getBean("jobRepository");
        assertNotNull(mJobRepository);
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"org/dataminx/dts/ws/testdomain-context.xml"};
    }

    @Override
    protected void onSetUpInTransaction() throws Exception {
        final DataSource dataSource = jdbcTemplate.getDataSource();
        final Connection con = DataSourceUtils.getConnection(dataSource);
        final IDatabaseConnection dbUnitCon = new DatabaseConnection(con);
        final IDataSet dataSet = new FlatXmlDataSet(new FileInputStream(
            "./src/test/resources/dbunit-test-data/JobJpaDaoImpl.xml"));
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
        final Job job = mJobRepository.findByResourceKey(SEARCH_GUID);
        Assert.assertEquals("job1", job.getName());
    }

    /**
     * Find job by user.
     */
    @Test
    public void testFindJobByUser() {
        final List<Job> jobs = mJobRepository.findByUser("NEW_USER_1");
        Assert.assertEquals(3, jobs.size(),
            "did not get expected number of entities");
    }

    /**
     * Find job by user and status.
     */
    @Test
    public void testFindJobByUserAndStatus() {
        List<Job> jobs = mJobRepository.findByUserAndStatus("NEW_USER_1",
            JobStatus.DONE);
        Assert.assertEquals(1, jobs.size(),
            "did not get expected number of entities");

        jobs = mJobRepository.findByUserAndStatus("NEW_USER_2", JobStatus.DONE);
        Assert.assertEquals(0, jobs.size(),
            "did not get expected number of entities");
    }

    /**
     * Creates the job entry.
     */
    @Test
    public void testCreateJobEntry() {
        Job job = null;
        job = new Job();
        job.setName("job6");
        final String newResourceKey = UUID.randomUUID().toString();
        job.setResourceKey(newResourceKey);
        job.setStatus(JobStatus.CREATED);
        job.setSubjectName("NEW_USER_2");
        mJobRepository.saveOrUpdate(job);

        final List<Job> jobs = mJobRepository.findByUser("NEW_USER_2");
        Assert.assertEquals(3, jobs.size(),
            "did not get expected number of entities");
        Assert.assertNotNull(job.getJobId(), "entity did not get persisted");
    }

    /**
     * Find and update.
     */
    @Test
    public void testFindAndUpdate() {
        final Job job = mJobRepository.findByResourceKey(SEARCH_GUID);
        Assert.assertEquals("NEW_USER_1", job.getSubjectName());

        job.setStatus(JobStatus.DONE);
        mJobRepository.saveOrUpdate(job);

        final List<Job> jobs = mJobRepository.findByUserAndStatus("NEW_USER_1",
            JobStatus.DONE);
        Assert.assertEquals(2, jobs.size(),
            "did not get expected number of entities");
    }

}
