package org.dataminx.dts.common.vfs;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import uk.ac.dl.escience.vfs.util.VFSUtil;

@Test(groups = {"unit-test"})
public class FileSystemManagerCacheTest {

    private static final String ROOT_FILE_OBJECT = "file:///";

    private FileSystemManagerCache mFileSystemManagerCache;

    @Test(expectedExceptions = FileSystemManagerCacheAlreadyInitializedException.class)
    public void testInitFileSystemManagerCacheWithError()
        throws FileSystemManagerCacheAlreadyInitializedException {

        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject =
            new HashMap<String, List<FileSystemManager>>();
        fileSystemManagersPerRootFileObject.put(anyString(), anyList());
        mFileSystemManagerCache.initFileSystemManagerCache(fileSystemManagersPerRootFileObject);
        // next line will force the expected exception
        mFileSystemManagerCache.initFileSystemManagerCache(anyMap());
    }
    
    @Test
    public void testBorrowOne()
        throws FileSystemManagerCacheAlreadyInitializedException,
        UnknownRootFileObjectException, FileSystemException {

        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject =
            new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        final FileSystemManager fsm = VFSUtil.createNewFsManager(true, true,
            true, true, true, true, true, System.getProperty("java.io.tmpdir"));
        fsmList.add(fsm);
        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);

        // init the cache
        mFileSystemManagerCache.initFileSystemManagerCache(fileSystemManagersPerRootFileObject);

        final FileSystemManager returnedFSM = mFileSystemManagerCache
            .borrowOne(ROOT_FILE_OBJECT);
        // assert that the cached fsm is the returnedFSM
        Assert.assertEquals(returnedFSM, fsm);
    }

    @Test(expectedExceptions = UnknownRootFileObjectException.class)
    public void testBorrowOneWithUnknownRootFileObjectException()
        throws FileSystemManagerCacheAlreadyInitializedException,
        UnknownRootFileObjectException, FileSystemException {

        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject =
            new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        final FileSystemManager fsm = VFSUtil.createNewFsManager(true, true,
            true, true, true, true, true, System.getProperty("java.io.tmpdir"));
        fsmList.add(fsm);
        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache
            .initFileSystemManagerCache(fileSystemManagersPerRootFileObject);
        // this line forces the expected exception because its an unknown key
        mFileSystemManagerCache.borrowOne(anyString());
    }

    @Test
    public void testBorrowOneFromList()
        throws FileSystemManagerCacheAlreadyInitializedException,
        UnknownRootFileObjectException, FileSystemException {

        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject =
            new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        for (int i = 0; i < 5; i++) {
            fsmList.add(VFSUtil.createNewFsManager(true, true, true, true,
                true, true, true, System.getProperty("java.io.tmpdir")));
        }
        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        // init the cache
        mFileSystemManagerCache.initFileSystemManagerCache(fileSystemManagersPerRootFileObject);
        // assert that the cache contains expected 5 FSMs
        Assert.assertEquals(mFileSystemManagerCache.getSizeOfAvailableFileSystemManagers(ROOT_FILE_OBJECT), 5);

        // borrow 5 FSMs and assert that the size of the available FSMs in the
        // cache is decremented after each loan.
        for (int i = 5; i > 0; i--) {
            final FileSystemManager returnedFSM = mFileSystemManagerCache
                .borrowOne(ROOT_FILE_OBJECT);
            Assert.assertTrue(fsmList.contains(returnedFSM));
            Assert.assertEquals(mFileSystemManagerCache
                .getSizeOfAvailableFileSystemManagers(ROOT_FILE_OBJECT), i - 1);
        }
    }
    
    @Test
    public void testReturnOneFromList()
        throws FileSystemManagerCacheAlreadyInitializedException,
        UnknownRootFileObjectException, FileSystemException,
        UnknownFileSystemManagerException {

        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject =
            new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        for (int i = 0; i < 2; i++) {
            fsmList.add(VFSUtil.createNewFsManager(true, true, true, true,
                true, true, true, System.getProperty("java.io.tmpdir")));
        }

        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache
            .initFileSystemManagerCache(fileSystemManagersPerRootFileObject);

        // borrow one
        final FileSystemManager returnedFSM = mFileSystemManagerCache
            .borrowOne(ROOT_FILE_OBJECT);

        // so there should be only one left that is available
        Assert.assertEquals(mFileSystemManagerCache
            .getSizeOfAvailableFileSystemManagers(ROOT_FILE_OBJECT), 1);

        // return the fsm
        mFileSystemManagerCache.returnOne(ROOT_FILE_OBJECT, returnedFSM);

        // so there should be two available after the return.
        Assert.assertEquals(mFileSystemManagerCache
            .getSizeOfAvailableFileSystemManagers(ROOT_FILE_OBJECT), 2);
    }

    @Test(expectedExceptions = UnknownRootFileObjectException.class)
    public void testReturnOneWithUnknownRootFileObjectError()
        throws FileSystemManagerCacheAlreadyInitializedException,
        UnknownRootFileObjectException, FileSystemException,
        UnknownFileSystemManagerException {

        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject =
            new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        fsmList.add(VFSUtil.createNewFsManager(true, true, true, true, true,
            true, true, System.getProperty("java.io.tmpdir")));

        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache
            .initFileSystemManagerCache(fileSystemManagersPerRootFileObject);

        final FileSystemManager returnedFSM = mFileSystemManagerCache
            .borrowOne(ROOT_FILE_OBJECT);

        // throw expected exception when trying to return an FSM with an unknown key
        mFileSystemManagerCache.returnOne(anyString(), returnedFSM);
    }

    @Test(expectedExceptions = UnknownFileSystemManagerException.class)
    public void testReturnOneWithUnknownFileSystemManagerError()
        throws FileSystemManagerCacheAlreadyInitializedException,
        UnknownRootFileObjectException, FileSystemException,
        UnknownFileSystemManagerException {

        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject =
            new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        fsmList.add(VFSUtil.createNewFsManager(true, true, true, true, true,
            true, true, System.getProperty("java.io.tmpdir")));

        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache
            .initFileSystemManagerCache(fileSystemManagersPerRootFileObject);

        mFileSystemManagerCache.borrowOne(ROOT_FILE_OBJECT);

        // assert that returning a new FSM (not the borrowed one) throws an expected exception
        mFileSystemManagerCache.returnOne(ROOT_FILE_OBJECT, VFSUtil
            .createNewFsManager(true, true, true, true, true, true, true,
                System.getProperty("java.io.tmpdir")));
    }

    @Test(expectedExceptions = UnknownRootFileObjectException.class)
    public void testClear()
        throws FileSystemManagerCacheAlreadyInitializedException,
        UnknownRootFileObjectException, FileSystemException {

        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject =
            new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        for (int i = 0; i < 5; i++) {
            fsmList.add(VFSUtil.createNewFsManager(true, true, true, true,
                true, true, true, System.getProperty("java.io.tmpdir")));
        }
        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache
            .initFileSystemManagerCache(fileSystemManagersPerRootFileObject);

        // clear the cache so it does not contain any cahced FSMs
        mFileSystemManagerCache.clear();
        // this line throws the expected exception because the cache is cleared
        // and so the ROOT_FILE_OBJECT key is unknown. 
        Assert.assertEquals(
                mFileSystemManagerCache.getSizeOfAvailableFileSystemManagers(ROOT_FILE_OBJECT), 0);
    }
}
