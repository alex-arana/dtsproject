package org.dataminx.dts.batch;


//import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Map;

import org.apache.commons.vfs.impl.DefaultFileSystemManager;
//import org.dataminx.dts.common.batch.util.FileObjectMap;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.dataminx.dts.common.vfs.FileSystemManagerCache;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


import static org.testng.Assert.*;


/**
 * A unit test for the MaxStreamCounterTask class.
 * 
 * @author Gerson Galang
 * @author David Meredith
 */
@Test(groups = {"unit-test"})
public class MaxStreamCounterTaskTest {

    //private DtsVfsUtil mockDtsVfsUtil;
    private SubmitJobRequest mockSubmitJobRequest;
    //private FileSystemManagerCache mockFileSystemManagerCache;
    private DtsJobDetails mockDtsJobDetails;

    public MaxStreamCounterTaskTest(){
           TestUtils.assertTestEnvironmentOk();
    }

    @BeforeClass
    public void init() {
        //mockDtsVfsUtil = mock(DtsVfsUtil.class);
        mockSubmitJobRequest = mock(SubmitJobRequest.class);
        //mockFileSystemManagerCache = mock(FileSystemManagerCache.class);
        mockDtsJobDetails = mock(DtsJobDetails.class);
    }    

    /**
     * This test asserts that the expected number of parallel connections
     * cahced by the MaxStreamCounterTask is limited by the
     * <code>maxStreamCounterTask.setMaxConnectionsToTry(int)</code> limit.
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(groups = {"local-file-transfer-test"})
    public void testMaxStreamCounterTaskCachesParallelConnectionsToDefinedLimit() throws Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()+ ".xml").getFile();
        //final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final JobDefinitionDocument dtsJob = TestUtils.getTestJobDefinitionDocument(f);

        DtsVfsUtil dtsVfsUtil = new DtsVfsUtil();
        /*dtsVfsUtil.setFtpSupported(false);
        dtsVfsUtil.setGsiftpSupported(false);
        dtsVfsUtil.setHttpSupported(false);
        dtsVfsUtil.setIrodsSupported(false);
        dtsVfsUtil.setSftpSupported(false);
        dtsVfsUtil.setFileSupported(true);
        dtsVfsUtil.setTmpDirPath(System.getProperty("java.io.tmpdir"));*/

        MinxJobDescriptionType minx = (MinxJobDescriptionType) dtsJob.getJobDefinition().getJobDescription();
        assertTrue(minx.getDataTransferArray().length == 1);
        DataTransferType transers[] = minx.getDataTransferArray();

        String sourceUri = transers[0].getSource().getURI();
        String targetUri = transers[0].getTarget().getURI();
        DefaultFileSystemManager fsMan = dtsVfsUtil.createNewFsManager();
        String sourceRootURL = fsMan.resolveFile(sourceUri).getFileSystem().getRoot().getURL().toString();
        String targetRootURL = fsMan.resolveFile(targetUri).getFileSystem().getRoot().getURL().toString();
        System.out.println("sourceRootURL: " + sourceRootURL);
        System.out.println("targetRootURL: " + targetRootURL);

        // map Holds the maximum number of files to be transferred from each Source or Target element.
        //final Map<String, Integer> sourceTargetMaxTotalFilesToTransfer = new FileObjectMap<String, Integer>();
        final Map<String, Integer> sourceTargetMaxTotalFilesToTransfer = new java.util.HashMap<String, Integer>();

        // We can't use DtsConstants.FILE_ROOT_PROTOCOL+"C:/" because the file root protocol
        // varies on different OS.
        //sourceTargetMaxTotalFilesToTransfer.put(DtsConstants.FILE_ROOT_PROTOCOL+"C:/", 1);
        //sourceTargetMaxTotalFilesToTransfer.put(DtsConstants.TMP_ROOT_PROTOCOL, 1);

        // max files to transfer must be greater than setMaxConnectionsToTry limit.
        sourceTargetMaxTotalFilesToTransfer.put(sourceRootURL, 40000);
        sourceTargetMaxTotalFilesToTransfer.put(targetRootURL, 40000);


        final MaxStreamCounterTask maxStreamCounterTask = new MaxStreamCounterTask();
        maxStreamCounterTask.setSubmitJobRequest(mockSubmitJobRequest);

        maxStreamCounterTask.setDtsVfsUtil(dtsVfsUtil);
        //maxStreamCounterTask.setDtsVfsUtil(mockDtsVfsUtil);

        FileSystemManagerCache fileSystemManagerCache = new FileSystemManagerCache();
        maxStreamCounterTask.setFileSystemManagerCache(fileSystemManagerCache);
        // set the max connections to try LESS than the number of files.
        maxStreamCounterTask.setMaxConnectionsToTry(4);
        maxStreamCounterTask.setDtsJobDetails(mockDtsJobDetails);

        // mokito objects here
        when(mockSubmitJobRequest.getJobDefinition()).thenReturn(
            dtsJob.getJobDefinition());

        when(mockDtsJobDetails.getSourceTargetMaxTotalFilesToTransfer())
            .thenReturn(sourceTargetMaxTotalFilesToTransfer);

        // Don't think we can use a mokito object here since mokito dont seem
        // to return a NEW FsManager instance when mockDtsVfsUtil.createNewFsManager()
        // is invoked !
        //when(mockDtsVfsUtil.createNewFsManager()).thenReturn(
        //   VFSUtil.createNewFsManager(false, false, false, false, false, true, false, System.getProperty("java.io.tmpdir")));

        final RepeatStatus taskStatus = maxStreamCounterTask.execute(null, null);

        // verify that initFileSystemManagerCache was called ONCE on the mockFileSystemManagerCache
        //verify(mockFileSystemManagerCache).initFileSystemManagerCache(anyMap());
        assertEquals(fileSystemManagerCache.getSizeOfAvailableFileSystemManagers(targetRootURL), 4);
        assertEquals(fileSystemManagerCache.getSizeOfAvailableFileSystemManagers(sourceRootURL), 4);

        assertEquals(taskStatus, RepeatStatus.FINISHED);

        // TODO: add other tests in here once we start putting the result of the MaxStreamCounterTask
        // to the ExecutionContext

    }




    /**
     * This test asserts that the expected number of parallel connections
     * cahced by the MaxStreamCounterTask is limited by the
     * <code>sourceTargetMaxTotalFilesToTransfer</code> limit. 
     *
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test(groups = {"local-file-transfer-test"})
    public void testMaxStreamCounterTaskCachesParallelConnections_ToFileLimit() throws Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()+ ".xml").getFile();
        //final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);

        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final JobDefinitionDocument dtsJob = TestUtils.getTestJobDefinitionDocument(f);

        DtsVfsUtil dtsVfsUtil = new DtsVfsUtil();

        MinxJobDescriptionType minx = (MinxJobDescriptionType) dtsJob.getJobDefinition().getJobDescription();
        assertTrue(minx.getDataTransferArray().length == 1);
        DataTransferType transers[] = minx.getDataTransferArray();

        String sourceUri = transers[0].getSource().getURI();
        String targetUri = transers[0].getTarget().getURI();
        DefaultFileSystemManager fsMan = dtsVfsUtil.createNewFsManager();
        String sourceRootURL = fsMan.resolveFile(sourceUri).getFileSystem().getRoot().getURL().toString();
        String targetRootURL = fsMan.resolveFile(targetUri).getFileSystem().getRoot().getURL().toString();
        System.out.println("sourceRootURL: " + sourceRootURL);
        System.out.println("targetRootURL: " + targetRootURL);

        // map Holds the maximum number of files to be transferred from each Source or Target element.
        final Map<String, Integer> sourceTargetMaxTotalFilesToTransfer = new java.util.HashMap<String, Integer>();

        sourceTargetMaxTotalFilesToTransfer.put(sourceRootURL, 5);
        sourceTargetMaxTotalFilesToTransfer.put(targetRootURL, 5);

        final MaxStreamCounterTask maxStreamCounterTask = new MaxStreamCounterTask();
        maxStreamCounterTask.setSubmitJobRequest(mockSubmitJobRequest);

        maxStreamCounterTask.setDtsVfsUtil(dtsVfsUtil);
        //maxStreamCounterTask.setDtsVfsUtil(mockDtsVfsUtil);

        FileSystemManagerCache fileSystemManagerCache = new FileSystemManagerCache();
        maxStreamCounterTask.setFileSystemManagerCache(fileSystemManagerCache);
        // set the max connections to try MORE than the number of files.
        maxStreamCounterTask.setMaxConnectionsToTry(10);
        maxStreamCounterTask.setDtsJobDetails(mockDtsJobDetails);

        // mokito objects here
        when(mockSubmitJobRequest.getJobDefinition()).thenReturn(
            dtsJob.getJobDefinition());

        when(mockDtsJobDetails.getSourceTargetMaxTotalFilesToTransfer())
            .thenReturn(sourceTargetMaxTotalFilesToTransfer);

        final RepeatStatus taskStatus = maxStreamCounterTask.execute(null, null);

        assertEquals(fileSystemManagerCache.getSizeOfAvailableFileSystemManagers(targetRootURL), 5);
        assertEquals(fileSystemManagerCache.getSizeOfAvailableFileSystemManagers(sourceRootURL), 5);

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
