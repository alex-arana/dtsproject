package org.dataminx.dts.batch;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dataminx.dts.vfs.DtsVfsUtil;
import org.dataminx.dts.vfs.FileSystemManagerCache;
import org.dataminx.dts.vfs.FileSystemManagerDispenser;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * A unit test for the MaxStreamCounterTask class.
 * 
 * @author Gerson Galang
 */
@Test(groups = { "unit-test" })
public class MaxStreamCounterTaskTest {

    private FileSystemManagerDispenser mFileSystemManagerDispenser;
    private DtsVfsUtil mDtsVfsUtil;
    private SubmitJobRequest mSubmitJobRequest;
    private FileSystemManagerCache mFileSystemManagerCache;

    @BeforeClass
    public void init() {
        mFileSystemManagerDispenser = mock(FileSystemManagerDispenser.class);
        mDtsVfsUtil = mock(DtsVfsUtil.class);
        mFileSystemManagerDispenser.setDtsVfsUtil(mDtsVfsUtil);
        mSubmitJobRequest = mock(SubmitJobRequest.class);
        mFileSystemManagerCache = mock(FileSystemManagerCache.class);
    }

    @SuppressWarnings("unchecked")
    @Test(groups = { "local-file-transfer-test" })
    public void testExecuteThatReturns10ParallelConnections() throws Exception {
        final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);

        final MaxStreamCounterTask maxStreamCounterTask = new MaxStreamCounterTask();
        maxStreamCounterTask.setSubmitJobRequest(mSubmitJobRequest);
        maxStreamCounterTask.setDtsVfsUtil(mDtsVfsUtil);
        maxStreamCounterTask.setFileSystemManagerDispenser(mFileSystemManagerDispenser);
        maxStreamCounterTask.setFileSystemManagerCache(mFileSystemManagerCache);
        maxStreamCounterTask.setMaxConnectionsToTry(10);

        when(mSubmitJobRequest.getJobDefinition()).thenReturn(dtsJob.getJobDefinition());

        final FileSystemManager fileSystemManager = DtsVfsUtil.createNewFsManager(false, false, false, false, true,
                true, false, "/tmp");
        when(mFileSystemManagerDispenser.getFileSystemManager()).thenReturn(
                (DefaultFileSystemManager) fileSystemManager);

        final RepeatStatus taskStatus = maxStreamCounterTask.execute(null, null);

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
