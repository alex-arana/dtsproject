package org.dataminx.dts.batch;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.xmlbeans.XmlException;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * An integration test for the VfsMixedFilesJobPartitioningStrategy class.
 * 
 * @author Gerson Galang
 */
@Test(groups = { "integration-test" })
public class VfsMixedFilesJobPartitioningStrategyIntegrationTest {

    private VfsMixedFilesJobPartitioningStrategy mPartitioningStrategy;

    private static final long FILE_SIZE_10MB = 10485760;
    private static final long FILE_SIZE_1MB = 1048576;

    @BeforeClass
    public void init() {
        mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        final DtsVfsUtil dtsVfsUtil = new DtsVfsUtil();
        mPartitioningStrategy.setDtsVfsUtil(dtsVfsUtil);
    }

    @DataProvider(name = "jobwith9files-provider")
    public Object[][] jobWith9FilesInput() {
        return new Object[][] { { 2, 5 }, { 3, 3 } };
    }

    @Test(groups = { "local-file-transfer-test" }, dataProvider = "jobwith9files-provider")
    public void testPartitionJobWith9FilesBasedOnMaxNumOfFiles(final int maxTotalFileNumPerStepLimit,
            final int expectedNumOfSteps) throws IOException, XmlException, JobScopingException {

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-9files.xml").getFile();
        final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory.parse(f);

        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(maxTotalFileNumPerStepLimit);
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(mDtsJob.getJobDefinition(), UUID
                .randomUUID().toString());

        assertNotNull(jobDetails);
        assertEquals(jobDetails.getJobSteps().size(), expectedNumOfSteps);
        assertEquals(jobDetails.getTotalBytes(), 9437184);
        assertEquals(jobDetails.getTotalFiles(), 9);
    }

    @Test(groups = { "local-file-transfer-test" })
    public void testPartitionJobWith20MixedFilesBasedOnMaxNumOfFiles() throws IOException, XmlException,
            JobScopingException {

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-20files.xml").getFile();
        final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory.parse(f);

        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(2);
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(mDtsJob.getJobDefinition(), UUID
                .randomUUID().toString());

        assertNotNull(jobDetails);
        assertEquals(jobDetails.getJobSteps().size(), 11);
        assertEquals(jobDetails.getTotalBytes(), 30408704);
        assertEquals(jobDetails.getTotalFiles(), 20);
    }

    @Test(groups = { "local-file-transfer-test" })
    public void testPartitionJobWithMixedFilesBasedOnMaxByteSize() throws IOException, XmlException,
            JobScopingException {
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-mixedfiles.xml").getFile();
        final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory.parse(f);

        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(3);
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(mDtsJob.getJobDefinition(), UUID
                .randomUUID().toString());

        assertNotNull(jobDetails);
        assertEquals(jobDetails.getJobSteps().size(), 5);
        assertEquals(jobDetails.getTotalBytes(), 20971520);
        assertEquals(jobDetails.getTotalFiles(), 11);
    }

    @Test(groups = { "local-file-transfer-test" }, expectedExceptions = JobScopingException.class)
    public void testPartitionJobWithFileExceedingMaxByteSizeLimit() throws IOException, XmlException,
            JobScopingException {
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-mixedfiles.xml").getFile();
        final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory.parse(f);

        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_1MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(3);
        mPartitioningStrategy.partitionTheJob(mDtsJob.getJobDefinition(), UUID.randomUUID().toString());
    }

}
