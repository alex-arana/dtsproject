package org.dataminx.dts.batch;

//import static org.dataminx.dts.common.util.TestFileChooser.getTestFilePostfix;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.xmlbeans.XmlException;
import org.dataminx.dts.DtsException;
import org.dataminx.dts.batch.common.DtsBatchJobConstants;
import org.dataminx.dts.common.DtsConstants;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
//import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.proposal.dmi.schemas.dts.x2010.dmiCommon.DataCopyActivityDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.ac.dl.escience.vfs.util.VFSUtil;

/**
 * A unit test for the AbstractJobPartitioningStrategy class that uses 
 * MixedFilesJobStepAllocator.
 * 
 * @author Gerson Galang
 * @author David Meredith 
 */
@ContextConfiguration(locations = {"/org/dataminx/dts/batch/client-context.xml"})
@Test(groups = {"unit-test"})
public class VfsMixedFilesJobPartitioningStrategyTest extends
    AbstractTestNGSpringContextTests {

    @Autowired
    private AbstractJobPartitioningStrategy mPartitioningStrategy;
    private DtsVfsUtil mDtsVfsUtil;
    private static final long FILE_SIZE_10MB = 10485760;

    public VfsMixedFilesJobPartitioningStrategyTest(){
         TestUtils.assertTestEnvironmentOk();
    }

    @BeforeClass
    public void init() {
        //TestUtils.assertTestEnvironmentOk();
        mDtsVfsUtil = mock(DtsVfsUtil.class);
        System.setProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY,
            System.getProperty("java.io.tmpdir"));
    }

    @Test(groups = {"local-file-transfer-test"})
    public void testPartitionWith1File() throws IOException, XmlException,
        JobScopingException, Exception {
       
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final DataCopyActivityDocument dtsJob = TestUtils.getTestDataCopyActivityDocument(f);

        //mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(3);
        mPartitioningStrategy.setTotalSizeLimit(-1);
        mPartitioningStrategy.setTotalFilesLimit(-1);
        

        final FileSystemManager fileSystemManager = VFSUtil.createNewFsManager(
            false, false, false, false, true, true, false, System.getProperty("java.io.tmpdir"));

        when(mDtsVfsUtil.createNewFsManager()).thenReturn(
            (DefaultFileSystemManager) fileSystemManager);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(
            dtsJob.getDataCopyActivity(), jobId, jobTag);

        assertNotNull(jobDetails);
        assertEquals(jobDetails.getJobSteps().size(), 1);
        assertEquals(jobDetails.getTotalFiles(), 1);

        //System.out.println("keyset: "+jobDetails.getSourceTargetMaxTotalFilesToTransfer().keySet());
        assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().size(), 2);
        assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(DtsConstants.TMP_ROOT_PROTOCOL).intValue(), 1);

        // this fails (original)
        // the value to which the specified key is mapped
        //assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(DtsConstants.FILE_ROOT_PROTOCOL).intValue(), 1);

        // this works ok on win
        //assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(DtsConstants.FILE_ROOT_PROTOCOL+"C:/").intValue(), 1);
    }



    @Test(groups = {"local-file-transfer-test"})
    public void testPartitionWith20Files() throws IOException, XmlException,
        JobScopingException, Exception {

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-20files.xml").getFile();
        final DataCopyActivityDocument dtsJob = TestUtils.getTestDataCopyActivityDocument(f);

        //mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(3);
        mPartitioningStrategy.setTotalSizeLimit(-1);
        mPartitioningStrategy.setTotalFilesLimit(-1);


        final FileSystemManager fileSystemManager = VFSUtil.createNewFsManager(
            false, false, false, false, true, true, false, System.getProperty("java.io.tmpdir"));

        when(mDtsVfsUtil.createNewFsManager()).thenReturn(
            (DefaultFileSystemManager) fileSystemManager);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(
            dtsJob.getDataCopyActivity(), jobId, jobTag);

        assertNotNull(jobDetails);
        /*
         * 19 files are 1MB each, 1 file is 10MB
         * therefore, if maxTotalFileNumPerStepLimit = 3 and
         * maxTotalByteSizePerStepLimit = 10MB,
         *
         * 1step for 1x10MB file
         * 6steps each with 3x01MB files
         * 1step with 1x01MB file
         * = (8 steps, 20 files)
         */
        assertEquals(jobDetails.getJobSteps().size(), 8);
        assertEquals(jobDetails.getTotalFiles(), 20);

        assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().size(), 2);
        // the maximum number of files to be transfered by one DataTransfer element to tmp is 11.
        assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(DtsConstants.TMP_ROOT_PROTOCOL).intValue(), 11);

    }


    @Test(groups = {"local-file-transfer-test"})
    public void test2ExpectedPartitionsWith20Files() throws IOException, XmlException,
        JobScopingException, Exception {

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-20files.xml").getFile();
        final DataCopyActivityDocument dtsJob = TestUtils.getTestDataCopyActivityDocument(f);

        //mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB*100);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(500);
        mPartitioningStrategy.setTotalSizeLimit(-1);
        mPartitioningStrategy.setTotalFilesLimit(-1); 

        final FileSystemManager fileSystemManager = VFSUtil.createNewFsManager(
            false, false, false, false, true, true, false, System.getProperty("java.io.tmpdir"));

        when(mDtsVfsUtil.createNewFsManager()).thenReturn(
            (DefaultFileSystemManager) fileSystemManager);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(
            dtsJob.getDataCopyActivity(), jobId, jobTag);

        assertNotNull(jobDetails);

        // two steps will be created and not one because at least one step gets
        // created per DataTransfer element, and the transfer-20files.xml file
        // has two DataTransfer elements ! 
        assertEquals(jobDetails.getJobSteps().size(), 2);
        assertEquals(jobDetails.getTotalFiles(), 20);

        assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().size(), 2);
        // the maximum number of files to be transfered by one DataTransfer element to tmp is 11.
        assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(DtsConstants.TMP_ROOT_PROTOCOL).intValue(), 11);

    }





    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = DtsException.class)
    public void testNegativeMaxTotalFileNumPerStepLimit() throws IOException,
        XmlException, JobScopingException, Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix() + ".xml").getFile();
        //final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final DataCopyActivityDocument dtsJob = TestUtils.getTestDataCopyActivityDocument(f);

        //mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        // this line will cause the DtsException
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(-1);
        mPartitioningStrategy.setTotalSizeLimit(-1);
        mPartitioningStrategy.setTotalFilesLimit(-1);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        mPartitioningStrategy.partitionTheJob(dtsJob.getDataCopyActivity(), jobId,
            jobTag);
    }


    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = DtsException.class)
    public void testNegativeMaxTotalByteSizePerStepLimit() throws IOException,
        XmlException, JobScopingException, Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()+ ".xml").getFile();
        //final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final DataCopyActivityDocument dtsJob = TestUtils.getTestDataCopyActivityDocument(f);

        //mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        // this line will cause the DtsException
        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(-1);
        mPartitioningStrategy.setTotalSizeLimit(-1);
        mPartitioningStrategy.setTotalFilesLimit(-1);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        mPartitioningStrategy.partitionTheJob(dtsJob.getDataCopyActivity(), jobId,
            jobTag);
    }


    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = JobScopingException.class)
    public void testMaxTotalFilesLimit() throws IOException,
            XmlException, JobScopingException, Exception {

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-20files.xml").getFile();
        final DataCopyActivityDocument dtsJob = TestUtils.getTestDataCopyActivityDocument(f);
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(3);
        mPartitioningStrategy.setTotalSizeLimit(-1);
        // this is the tested var - it will cause the expected job scoping exception
        // because the max files limit is only 10 and we are trying to copy 20 files.
        mPartitioningStrategy.setTotalFilesLimit(10);

        final FileSystemManager fileSystemManager = VFSUtil.createNewFsManager(
                false, false, false, false, true, true, false, System.getProperty("java.io.tmpdir"));

        when(mDtsVfsUtil.createNewFsManager()).thenReturn(
                (DefaultFileSystemManager) fileSystemManager);

        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        mPartitioningStrategy.partitionTheJob(dtsJob.getDataCopyActivity(), jobId,
                jobTag);
    }

    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = JobScopingException.class)
    public void testMaxTotalSizeLimit() throws IOException,
            XmlException, JobScopingException, Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix() + ".xml").getFile();
        //final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final DataCopyActivityDocument dtsJob = TestUtils.getTestDataCopyActivityDocument(f);
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(3);
        mPartitioningStrategy.setTotalFilesLimit(-1);
        // this is the tested var - it will cause the expected job scoping exception
        // because 100 bytes < 1MB test file
        mPartitioningStrategy.setTotalSizeLimit(100);

        final FileSystemManager fileSystemManager = VFSUtil.createNewFsManager(
                false, false, false, false, true, true, false, System.getProperty("java.io.tmpdir"));

        when(mDtsVfsUtil.createNewFsManager()).thenReturn(
                (DefaultFileSystemManager) fileSystemManager);

        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        mPartitioningStrategy.partitionTheJob(dtsJob.getDataCopyActivity(), jobId,
                jobTag);
    }

    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = JobScopingException.class)
    public void testInvalidTotalSizeLimit() throws IOException,
            XmlException, JobScopingException, Exception {

        AbstractJobPartitioningStrategy localPs = new AbstractJobPartitioningStrategy() {

            public DtsJobStepAllocator createDtsJobStepAllocator() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        localPs.setDtsVfsUtil(mDtsVfsUtil);
        localPs.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        localPs.setMaxTotalFileNumPerStepLimit(3);
        localPs.setTotalFilesLimit(-1);
        // this is the tested var - it will cause the expected exception because
        // the value is not positive or -1.
        localPs.setTotalSizeLimit(-2);
        // need to call afterPropertiesSet to force throwing of the exception
        localPs.afterPropertiesSet();
    }

    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = JobScopingException.class)
    public void testInvalidTotalFilesLimit() throws IOException,
            XmlException, JobScopingException, Exception {

        AbstractJobPartitioningStrategy localPs = new AbstractJobPartitioningStrategy() {

            public DtsJobStepAllocator createDtsJobStepAllocator() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        localPs.setDtsVfsUtil(mDtsVfsUtil);
        localPs.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        localPs.setMaxTotalFileNumPerStepLimit(3);
        // this is the tested var - it will cause the expected exception because
        // the value is not positive or -1.
        localPs.setTotalFilesLimit(-2);
        localPs.setTotalSizeLimit(-1);
        // need to call afterPropertiesSet to force throwing of the exception
        localPs.afterPropertiesSet();
    }
    


    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = IllegalArgumentException.class)
    public void testNullJobDefinitionParameter() throws JobScopingException {
        //mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        mPartitioningStrategy.partitionTheJob(null, jobId, jobTag);
    }

    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = IllegalArgumentException.class)
    public void testNullJobResourceKeyParameter() throws IOException,
        XmlException, JobScopingException, Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()+ ".xml").getFile();
        //final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final DataCopyActivityDocument dtsJob = TestUtils.getTestDataCopyActivityDocument(f);

        //mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.partitionTheJob(dtsJob.getDataCopyActivity(), null,
            null);
    }

}
