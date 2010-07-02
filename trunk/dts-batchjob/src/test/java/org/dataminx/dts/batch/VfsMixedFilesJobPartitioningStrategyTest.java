package org.dataminx.dts.batch;

import static org.dataminx.dts.common.util.TestFileChooser.getTestFilePostfix;
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
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
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
 */
@ContextConfiguration(locations = {"/org/dataminx/dts/batch/client-context.xml"})
@Test(groups = {"unit-test"})
public class VfsMixedFilesJobPartitioningStrategyTest extends
    AbstractTestNGSpringContextTests {

    @Autowired
    private AbstractJobPartitioningStrategy mPartitioningStrategy;
    private DtsVfsUtil mDtsVfsUtil;

    private static final long FILE_SIZE_10MB = 10485760;

    @BeforeClass
    public void init() {
        mDtsVfsUtil = mock(DtsVfsUtil.class);
        System.setProperty(DtsBatchJobConstants.DTS_JOB_STEP_DIRECTORY_KEY,
            System.getProperty("java.io.tmpdir"));
    }

    @Test(groups = {"local-file-transfer-test"})
    public void testPartitionWith1File() throws IOException, XmlException,
        JobScopingException {
        final File f = new ClassPathResource(
            "/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()
                + ".xml").getFile();
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory
            .parse(f);
        //mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(FILE_SIZE_10MB);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(3);

        final FileSystemManager fileSystemManager = VFSUtil.createNewFsManager(
            false, false, false, false, true, true, false, System.getProperty("java.io.tmpdir"));

        when(mDtsVfsUtil.createNewFsManager()).thenReturn(
            (DefaultFileSystemManager) fileSystemManager);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        final DtsJobDetails jobDetails = mPartitioningStrategy.partitionTheJob(
            dtsJob.getJobDefinition(), jobId, jobTag);

        assertNotNull(jobDetails);
        assertEquals(jobDetails.getJobSteps().size(), 1);
        assertEquals(jobDetails.getTotalFiles(), 1);
        assertEquals(jobDetails.getSourceTargetMaxTotalFilesToTransfer().get(
            DtsConstants.FILE_ROOT_PROTOCOL).intValue(), 1);
    }

    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = DtsException.class)
    public void testNegativeMaxTotalFileNumPerStepLimit() throws IOException,
        XmlException, JobScopingException {
        final File f = new ClassPathResource(
            "/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()
                + ".xml").getFile();
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory
            .parse(f);
        //mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        mPartitioningStrategy.setMaxTotalFileNumPerStepLimit(-1);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        mPartitioningStrategy.partitionTheJob(dtsJob.getJobDefinition(), jobId,
            jobTag);
    }

    @Test(groups = {"local-file-transfer-test"}, expectedExceptions = DtsException.class)
    public void testNegativeMaxTotalByteSizePerStepLimit() throws IOException,
        XmlException, JobScopingException {
        final File f = new ClassPathResource(
            "/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()
                + ".xml").getFile();
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory
            .parse(f);
        //mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.setDtsVfsUtil(mDtsVfsUtil);
        mPartitioningStrategy.setMaxTotalByteSizePerStepLimit(-1);
        final String jobId = UUID.randomUUID().toString();
        final String jobTag = jobId;
        mPartitioningStrategy.partitionTheJob(dtsJob.getJobDefinition(), jobId,
            jobTag);
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
        XmlException, JobScopingException {
        final File f = new ClassPathResource(
            "/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()
                + ".xml").getFile();
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory
            .parse(f);
        //mPartitioningStrategy = new VfsMixedFilesJobPartitioningStrategy();
        mPartitioningStrategy.partitionTheJob(dtsJob.getJobDefinition(), null,
            null);
    }

}
