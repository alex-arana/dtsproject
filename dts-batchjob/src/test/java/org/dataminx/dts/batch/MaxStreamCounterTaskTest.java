package org.dataminx.dts.batch;


import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.Map;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dataminx.dts.common.DtsConstants;
import org.dataminx.dts.common.batch.util.FileObjectMap;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.dataminx.dts.common.vfs.FileSystemManagerCache;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import uk.ac.dl.escience.vfs.util.VFSUtil;

/**
 * A unit test for the MaxStreamCounterTask class.
 * 
 * @author Gerson Galang
 * @author David Meredith (modifications)
 */
@Test(groups = {"unit-test"})
public class MaxStreamCounterTaskTest {

    private DtsVfsUtil mockDtsVfsUtil;
    private SubmitJobRequest mockSubmitJobRequest;
    private FileSystemManagerCache mockFileSystemManagerCache;
    private DtsJobDetails mockDtsJobDetails;

    @BeforeClass
    public void init() {
        TestUtils.assertTestEnvironmentOk();
        mockDtsVfsUtil = mock(DtsVfsUtil.class);
        mockSubmitJobRequest = mock(SubmitJobRequest.class);
        mockFileSystemManagerCache = mock(FileSystemManagerCache.class);
        mockDtsJobDetails = mock(DtsJobDetails.class);
    }    

    @SuppressWarnings("unchecked")
    @Test(groups = {"local-file-transfer-test"})
    public void testExecuteThatReturns10ParallelConnections() throws Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()+ ".xml").getFile();
        //final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final JobDefinitionDocument dtsJob = TestUtils.getTestJobDefinitionDocument(f);
        

        // map Holds the maximum number of files to be transferred from each Source or Target element.
        final Map<String, Integer> sourceTargetMaxTotalFilesToTransfer = new FileObjectMap<String, Integer>();
        sourceTargetMaxTotalFilesToTransfer.put(DtsConstants.FILE_ROOT_PROTOCOL, 1);
        sourceTargetMaxTotalFilesToTransfer.put(DtsConstants.TMP_ROOT_PROTOCOL, 1);

        final MaxStreamCounterTask maxStreamCounterTask = new MaxStreamCounterTask();
        maxStreamCounterTask.setSubmitJobRequest(mockSubmitJobRequest);
        maxStreamCounterTask.setDtsVfsUtil(mockDtsVfsUtil);
        maxStreamCounterTask.setFileSystemManagerCache(mockFileSystemManagerCache);
        maxStreamCounterTask.setMaxConnectionsToTry(10);
        maxStreamCounterTask.setDtsJobDetails(mockDtsJobDetails);

        // mokito objects here 
        when(mockSubmitJobRequest.getJobDefinition()).thenReturn(
            dtsJob.getJobDefinition());

        when(mockDtsJobDetails.getSourceTargetMaxTotalFilesToTransfer())
            .thenReturn(sourceTargetMaxTotalFilesToTransfer);

        final FileSystemManager fileSystemManager = VFSUtil.createNewFsManager(
            false, false, false, false, true, true, false, System.getProperty("java.io.tmpdir"));
        when(mockDtsVfsUtil.createNewFsManager()).thenReturn(
            (DefaultFileSystemManager) fileSystemManager);

        final RepeatStatus taskStatus = maxStreamCounterTask
            .execute(null, null);

        verify(mockFileSystemManagerCache).initFileSystemManagerCache(anyMap());

        assertEquals(taskStatus, RepeatStatus.FINISHED);

        // TODO: add other tests in here once we start putting the result of the MaxStreamCounterTask
        // to the ExecutionContext

    }



    /*@Test
    public void testContentsOfTheFileSystemManagerCache() {
        // TODO: check and see if max connections really is provided by a file transfer
    }*/

    /*@Test
    public void testNonExistenceOfPropertiesFromExecutionContextAfterJobFinishedSuccessfully() {
        // TODO:
    }*/
}
