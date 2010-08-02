package org.dataminx.dts.batch;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
 * @author David Meredith (modifications)
 */
@ContextConfiguration(locations = {"/org/dataminx/dts/batch/client-context.xml"})
@Test(groups = {"integration-test"})
public class VfsMixedFilesJobPartitioningStrategyIntegrationTest extends
    AbstractTestNGSpringContextTests {

    private static final long FILE_SIZE_10MB = 10485760; //bytes
    private static final long FILE_SIZE_1MB = 1048576;   // bytes

    @Autowired
    private AbstractJobPartitioningStrategy mPartitioningStrategy;

    @BeforeClass
    public void init() {
        TestUtils.assertTestEnvironmentOk();
        final DtsVfsUtil dtsVfsUtil = new DtsVfsUtil();
        mPartitioningStrategy.setDtsVfsUtil(dtsVfsUtil);
        System.setProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY,
            System.getProperty("java.io.tmpdir"));
    }

    @DataProvider(name = "jobwith9files-provider")
    public Object[][] jobWith9FilesInput() {
        return new Object[][] { {2, 5}, {3, 3}};
    }


    @Test(groups = {"local-file-transfer-test"})
    public void testPartitionJobWith20MixedFilesBasedOnMaxNumOfFiles()
        throws IOException, XmlException, JobScopingException, Exception {

        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-20files" + getTestFilePostfix()+ ".xml").getFile();
        //final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-20files.xml").getFile();

        final JobDefinitionDocument mDtsJob = TestUtils.getTestJobDefinitionDocument(f);

        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(2);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(
            mDtsJob.getJobDefinition(), jobId, jobTag);

        assertNotNull(jobDetails);
        assertEquals(jobDetails.getTotalBytes(), 30408704);
        // (two DataTransfer elements defined, first has 11 files, second has 9) 11 + 9 = 20
        assertEquals(jobDetails.getTotalFiles(), 20);

        // expected value here was 11 ! (should this be 20 ?).
        this.checkSourceFileCountEqualsSinkFileCount(11, jobDetails.getSourceTargetMaxTotalFilesToTransfer());
    }

    @Test(groups = {"local-file-transfer-test"}, dataProvider = "jobwith9files-provider")
    public void testPartitionJobWith9FilesBasedOnMaxNumOfFiles(
        final int maxTotalFileNumPerStepLimit, final int expectedNumOfSteps)
        throws IOException, XmlException, JobScopingException, Exception {

        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-9files" + getTestFilePostfix()+ ".xml").getFile();
        //final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-9files.xml").getFile();
        final JobDefinitionDocument mDtsJob = TestUtils.getTestJobDefinitionDocument(f);

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

        this.checkSourceFileCountEqualsSinkFileCount(9, jobDetails.getSourceTargetMaxTotalFilesToTransfer());
    }

    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = JobScopingException.class)
    public void testPartitionJobWithFileExceedingMaxByteSizeLimit()
        throws IOException, XmlException, JobScopingException, Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-mixedfiles"+ getTestFilePostfix() + ".xml").getFile();
        //final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-mixedfiles.xml").getFile();
        final JobDefinitionDocument mDtsJob = TestUtils.getTestJobDefinitionDocument(f);

        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_1MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(3);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        // expect to throw a JobScopingException
        mPartitioningStrategy.partitionTheJob(
                mDtsJob.getJobDefinition(), jobId, jobTag);
      
    }

     
    
    @Test(groups = {"local-file-transfer-test"})
    public void testPartitionJobWithMixedFilesBasedOnMaxByteSize()
        throws IOException, XmlException, JobScopingException, Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-mixedfiles"+ getTestFilePostfix() + ".xml").getFile();
        //final JobDefinitionDocument mDtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-mixedfiles.xml").getFile();
        final JobDefinitionDocument mDtsJob = TestUtils.getTestJobDefinitionDocument(f);
 
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

        this.checkSourceFileCountEqualsSinkFileCount(11, jobDetails.getSourceTargetMaxTotalFilesToTransfer());
    }

    /**
     * Check that the number of files to be copied from the source equals
     * the number of files to be delivered to the sink, and that there are two
     * records in the given map (one for the source, one for the sink).
     *
     * @param expectedFileCount
     * @param sourceTargetMaxTotalFilesToTransfer
     */
    private void checkSourceFileCountEqualsSinkFileCount(int expectedFileCount,
            Map<String, Integer> sourceTargetMaxTotalFilesToTransfer) {

        // assert that there are two records (source and sink)
        assertEquals(sourceTargetMaxTotalFilesToTransfer.size(), 2);

        // Different OS may have different keys (root URIs) in the
        // sourceTargetMaxTotalFilesToTransfer map as shown below -
        // - this would work on *nix:
        //assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(
        //      DtsConstants.FILE_ROOT_PROTOCOL).intValue(), 11);
        // - while this is required on Win (note need to add file system drive):
        //assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(
        //      DtsConstants.FILE_ROOT_PROTOCOL+"C:/").intValue(), 11);
        // 
        // Therefore, iterate the keys and assert that the number of files in all
        // the map entries have the expected value.
        Set<String> keys = sourceTargetMaxTotalFilesToTransfer.keySet();
        Iterator<String> it = keys.iterator();
        while(it.hasNext()){
            assertEquals(sourceTargetMaxTotalFilesToTransfer.get(it.next()).intValue(), expectedFileCount);
        }
        // We can test for TMP_ROOT_PROTOCOL as this works on both *nix and Win.
        assertEquals(sourceTargetMaxTotalFilesToTransfer.get(
            DtsConstants.TMP_ROOT_PROTOCOL).intValue(), expectedFileCount);

    }


}
