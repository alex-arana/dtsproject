package org.dataminx.dts.batch;

import static org.dataminx.dts.common.util.TestFileChooser.getTestFilePostfix;
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
import org.testng.annotations.Test;

import uk.ac.dl.escience.vfs.util.VFSUtil;

/**
 * A unit test for the MaxStreamCounterTask class.
 * 
 * @author Gerson Galang
 */
@Test(groups = {"unit-test"})
public class MaxStreamCounterTaskTest {

    private DtsVfsUtil mDtsVfsUtil;
    private SubmitJobRequest mSubmitJobRequest;
    private FileSystemManagerCache mFileSystemManagerCache;
    private DtsJobDetails mDtsJobDetails;

    @BeforeClass
    public void init() {
        mDtsVfsUtil = mock(DtsVfsUtil.class);
        mSubmitJobRequest = mock(SubmitJobRequest.class);
        mFileSystemManagerCache = mock(FileSystemManagerCache.class);
        mDtsJobDetails = mock(DtsJobDetails.class);
    }

    @SuppressWarnings("unchecked")
    @Test(groups = {"local-file-transfer-test"})
    public void testExecuteThatReturns10ParallelConnections() throws Exception {
        final File f = new ClassPathResource(
            "/org/dataminx/dts/batch/transfer-1file" + getTestFilePostfix()
                + ".xml").getFile();
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory
            .parse(f);
        final Map<String, Integer> sourceTargetMaxTotalFilesToTransfer = new FileObjectMap<String, Integer>();
        sourceTargetMaxTotalFilesToTransfer.put(
            DtsConstants.FILE_ROOT_PROTOCOL, 2);

        final MaxStreamCounterTask maxStreamCounterTask = new MaxStreamCounterTask();
        maxStreamCounterTask.setSubmitJobRequest(mSubmitJobRequest);
        maxStreamCounterTask.setDtsVfsUtil(mDtsVfsUtil);
        maxStreamCounterTask.setFileSystemManagerCache(mFileSystemManagerCache);
        maxStreamCounterTask.setMaxConnectionsToTry(10);
        maxStreamCounterTask.setDtsJobDetails(mDtsJobDetails);

        when(mSubmitJobRequest.getJobDefinition()).thenReturn(
            dtsJob.getJobDefinition());

        when(mDtsJobDetails.getSourceTargetMaxTotalFilesToTransfer())
            .thenReturn(sourceTargetMaxTotalFilesToTransfer);

        final FileSystemManager fileSystemManager = VFSUtil.createNewFsManager(
            false, false, false, false, true, true, false, "/tmp");
        when(mDtsVfsUtil.createNewFsManager()).thenReturn(
            (DefaultFileSystemManager) fileSystemManager);

        final RepeatStatus taskStatus = maxStreamCounterTask
            .execute(null, null);

        verify(mFileSystemManagerCache).initFileSystemManagerCache(anyMap());

        assertEquals(taskStatus, RepeatStatus.FINISHED);

        // TODO: add other tests in here once we start putting the result of the MaxStreamCounterTask
        // to the ExecutionContext

    }

    @Test
    public void testContentsOfTheFileSystemManagerCache() {
        // TODO: check and see if max connections really is provided by a file transfer
    }

    @Test
    public void testNonExistenceOfPropertiesFromExecutionContextAfterJobFinishedSuccessfully() {
        // TODO:
    }
}
