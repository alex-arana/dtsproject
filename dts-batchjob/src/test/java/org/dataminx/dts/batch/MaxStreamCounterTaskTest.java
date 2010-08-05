package org.dataminx.dts.batch;


import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.Map;
import org.apache.commons.vfs.FileObject;

import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dataminx.dts.common.DtsConstants;
import org.dataminx.dts.common.batch.util.FileObjectMap;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.dataminx.dts.common.vfs.FileSystemManagerCache;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.DataStagingType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import uk.ac.dl.escience.vfs.util.VFSUtil;

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
    private FileSystemManagerCache mockFileSystemManagerCache;
    private DtsJobDetails mockDtsJobDetails;

    public MaxStreamCounterTaskTest(){
           TestUtils.assertTestEnvironmentOk();
    }

    @BeforeClass
    public void init() {
        //mockDtsVfsUtil = mock(DtsVfsUtil.class);
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
        final Map<String, Integer> sourceTargetMaxTotalFilesToTransfer = new FileObjectMap<String, Integer>();
        // We can't use DtsConstants.FILE_ROOT_PROTOCOL+"C:/" because the file root protocol
        // varies on different OS. 
        //sourceTargetMaxTotalFilesToTransfer.put(DtsConstants.FILE_ROOT_PROTOCOL+"C:/", 1);
        //sourceTargetMaxTotalFilesToTransfer.put(DtsConstants.TMP_ROOT_PROTOCOL, 1);
        sourceTargetMaxTotalFilesToTransfer.put(sourceRootURL, 1);
        sourceTargetMaxTotalFilesToTransfer.put(targetRootURL, 1);


        final MaxStreamCounterTask maxStreamCounterTask = new MaxStreamCounterTask();
        maxStreamCounterTask.setSubmitJobRequest(mockSubmitJobRequest);

        maxStreamCounterTask.setDtsVfsUtil(dtsVfsUtil);
        //maxStreamCounterTask.setDtsVfsUtil(mockDtsVfsUtil);

        maxStreamCounterTask.setFileSystemManagerCache(mockFileSystemManagerCache);
        maxStreamCounterTask.setMaxConnectionsToTry(10);
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
