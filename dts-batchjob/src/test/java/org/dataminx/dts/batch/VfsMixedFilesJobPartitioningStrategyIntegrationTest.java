package org.dataminx.dts.batch;

import static org.dataminx.dts.common.util.TestFileChooser.getTestFilePostfix;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.xmlbeans.XmlException;
import org.dataminx.dts.batch.common.DtsBatchJobConstants;
import org.dataminx.dts.common.DtsConstants;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * An integration test for the AbstractJobPartitioningStrategy class that uses 
 * MixedFilesJobStepAllocator.
 * 
 * @author Gerson Galang
 */
@ContextConfiguration(locations = {"/org/dataminx/dts/batch/client-context.xml"})
@Test(groups = {"integration-test"})
public class VfsMixedFilesJobPartitioningStrategyIntegrationTest extends
    AbstractTestNGSpringContextTests {

    private static final long FILE_SIZE_10MB = 10485760;
    private static final long FILE_SIZE_1MB = 1048576;

    @Autowired
    private AbstractJobPartitioningStrategy mPartitioningStrategy;

    @BeforeClass
    public void init() {
        final DtsVfsUtil dtsVfsUtil = new DtsVfsUtil();
        mPartitioningStrategy.setDtsVfsUtil(dtsVfsUtil);
        System.setProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY,
            "/tmp");
    }

    @DataProvider(name = "jobwith9files-provider")
    public Object[][] jobWith9FilesInput() {
        return new Object[][] { {2, 5}, {3, 3}};
    }

    @Test(groups = {"local-file-transfer-test"})
    public void testPartitionJobWith20MixedFilesBasedOnMaxNumOfFiles()
        throws IOException, XmlException, JobScopingException {

        final File f = new ClassPathResource(
            "/org/dataminx/dts/batch/transfer-20files" + getTestFilePostfix()
                + ".xml").getFile();
        final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory
            .parse(f);

        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(2);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(
            mDtsJob.getJobDefinition(), jobId, jobTag);

        assertNotNull(jobDetails);
        assertEquals(jobDetails.getTotalBytes(), 30408704);
        assertEquals(jobDetails.getTotalFiles(), 20);
        assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(
            DtsConstants.FILE_ROOT_PROTOCOL).intValue(), 11);
    }

    @Test(groups = {"local-file-transfer-test"}, dataProvider = "jobwith9files-provider")
    public void testPartitionJobWith9FilesBasedOnMaxNumOfFiles(
        final int maxTotalFileNumPerStepLimit, final int expectedNumOfSteps)
        throws IOException, XmlException, JobScopingException {

        final File f = new ClassPathResource(
            "/org/dataminx/dts/batch/transfer-9files" + getTestFilePostfix()
                + ".xml").getFile();
        final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory
            .parse(f);

        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy
            .setMaxTotalFileNumPerStepLimit(maxTotalFileNumPerStepLimit);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(
            mDtsJob.getJobDefinition(), jobId, jobTag);

        assertNotNull(jobDetails);
        assertEquals(jobDetails.getJobSteps().size(), expectedNumOfSteps);
        assertEquals(jobDetails.getTotalBytes(), 9437184);
        assertEquals(jobDetails.getTotalFiles(), 9);
        assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(
            DtsConstants.FILE_ROOT_PROTOCOL).intValue(), 9);
    }

    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = JobScopingException.class)
    public void testPartitionJobWithFileExceedingMaxByteSizeLimit()
        throws IOException, XmlException, JobScopingException {
        final File f = new ClassPathResource(
            "/org/dataminx/dts/batch/transfer-mixedfiles"
                + getTestFilePostfix() + ".xml").getFile();
        final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory
            .parse(f);

        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_1MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(3);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        mPartitioningStrategy.partitionTheJob(mDtsJob.getJobDefinition(),
            jobId, jobTag);
    }

    @Test(groups = {"local-file-transfer-test"})
    public void testPartitionJobWithMixedFilesBasedOnMaxByteSize()
        throws IOException, XmlException, JobScopingException {
        final File f = new ClassPathResource(
            "/org/dataminx/dts/batch/transfer-mixedfiles"
                + getTestFilePostfix() + ".xml").getFile();
        final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory
            .parse(f);

        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(3);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(
            mDtsJob.getJobDefinition(), jobId, jobTag);

        assertNotNull(jobDetails);
        assertEquals(jobDetails.getJobSteps().size(), 5);
        assertEquals(jobDetails.getTotalBytes(), 20971520);
        assertEquals(jobDetails.getTotalFiles(), 11);
        assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(
            DtsConstants.FILE_ROOT_PROTOCOL).intValue(), 11);
    }

}
