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

@Test(groups = { "unit-test" })
public class FileSystemManagerCacheTest {

    private FileSystemManagerCache mFileSystemManagerCache;

    private static final String ROOT_FILE_OBJECT = "file:///";

    @Test(expectedExceptions = FileSystemManagerCacheAlreadyInitializedException.class)
    public void testInitFileSystemManagerCacheWithError() throws FileSystemManagerCacheAlreadyInitializedException {
        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject = new HashMap<String, List<FileSystemManager>>();
        fileSystemManagersPerRootFileObject.put(anyString(), anyList());
        mFileSystemManagerCache.initFileSystemManagerCache(fileSystemManagersPerRootFileObject);
        mFileSystemManagerCache.initFileSystemManagerCache(anyMap());
    }

    public void testBorrowOne() throws FileSystemManagerCacheAlreadyInitializedException,
            UnknownRootFileObjectException, FileSystemException {
        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject = new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        final FileSystemManager fsm = DtsVfsUtil.createNewFsManager(true, true, true, true, true, true, true, "/tmp");
        fsmList.add(fsm);
        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache.initFileSystemManagerCache(fileSystemManagersPerRootFileObject);
        final FileSystemManager returnedFSM = mFileSystemManagerCache.borrowOne(ROOT_FILE_OBJECT);
        Assert.assertEquals(returnedFSM, fsm);
    }

    @Test(expectedExceptions = UnknownRootFileObjectException.class)
    public void testBorrowOneWithUnknownRootFileObjectException()
            throws FileSystemManagerCacheAlreadyInitializedException, UnknownRootFileObjectException,
            FileSystemException {
        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject = new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        final FileSystemManager fsm = DtsVfsUtil.createNewFsManager(true, true, true, true, true, true, true, "/tmp");
        fsmList.add(fsm);
        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache.initFileSystemManagerCache(fileSystemManagersPerRootFileObject);
        mFileSystemManagerCache.borrowOne(anyString());
    }

    public void testBorrowOneFromList() throws FileSystemManagerCacheAlreadyInitializedException,
            UnknownRootFileObjectException, FileSystemException {
        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject = new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        for (int i = 0; i < 5; i++) {
            fsmList.add(DtsVfsUtil.createNewFsManager(true, true, true, true, true, true, true, "/tmp"));
        }
        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache.initFileSystemManagerCache(fileSystemManagersPerRootFileObject);
        Assert.assertEquals(mFileSystemManagerCache.getSizeOfAvailableFileSystemManagers(ROOT_FILE_OBJECT), 5);
        for (int i = 5; i > 0; i--) {
            final FileSystemManager returnedFSM = mFileSystemManagerCache.borrowOne(ROOT_FILE_OBJECT);
            Assert.assertTrue(fsmList.contains(returnedFSM));
            Assert.assertEquals(mFileSystemManagerCache.getSizeOfAvailableFileSystemManagers(ROOT_FILE_OBJECT), i - 1);
        }
    }

    public void testReturnOneFromList() throws FileSystemManagerCacheAlreadyInitializedException,
            UnknownRootFileObjectException, FileSystemException, UnknownFileSystemManagerException {
        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject = new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        for (int i = 0; i < 2; i++) {
            fsmList.add(DtsVfsUtil.createNewFsManager(true, true, true, true, true, true, true, "/tmp"));
        }

        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache.initFileSystemManagerCache(fileSystemManagersPerRootFileObject);

        final FileSystemManager returnedFSM = mFileSystemManagerCache.borrowOne(ROOT_FILE_OBJECT);

        Assert.assertEquals(mFileSystemManagerCache.getSizeOfAvailableFileSystemManagers(ROOT_FILE_OBJECT), 1);

        mFileSystemManagerCache.returnOne(ROOT_FILE_OBJECT, returnedFSM);

        Assert.assertEquals(mFileSystemManagerCache.getSizeOfAvailableFileSystemManagers(ROOT_FILE_OBJECT), 2);
    }

    @Test(expectedExceptions = UnknownRootFileObjectException.class)
    public void testReturnOneWithUnknownRootFileObjectError() throws FileSystemManagerCacheAlreadyInitializedException,
            UnknownRootFileObjectException, FileSystemException, UnknownFileSystemManagerException {
        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject = new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        fsmList.add(DtsVfsUtil.createNewFsManager(true, true, true, true, true, true, true, "/tmp"));

        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache.initFileSystemManagerCache(fileSystemManagersPerRootFileObject);

        final FileSystemManager returnedFSM = mFileSystemManagerCache.borrowOne(ROOT_FILE_OBJECT);

        mFileSystemManagerCache.returnOne(anyString(), returnedFSM);
    }

    @Test(expectedExceptions = UnknownFileSystemManagerException.class)
    public void testReturnOneWithUnknownFileSystemManagerError()
            throws FileSystemManagerCacheAlreadyInitializedException, UnknownRootFileObjectException,
            FileSystemException, UnknownFileSystemManagerException {
        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject = new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        fsmList.add(DtsVfsUtil.createNewFsManager(true, true, true, true, true, true, true, "/tmp"));

        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache.initFileSystemManagerCache(fileSystemManagersPerRootFileObject);

        mFileSystemManagerCache.borrowOne(ROOT_FILE_OBJECT);

        mFileSystemManagerCache.returnOne(ROOT_FILE_OBJECT, DtsVfsUtil.createNewFsManager(true, true, true, true, true,
                true, true, "/tmp"));
    }

    @Test(expectedExceptions = UnknownRootFileObjectException.class)
    public void testClear() throws FileSystemManagerCacheAlreadyInitializedException, UnknownRootFileObjectException,
            FileSystemException {
        mFileSystemManagerCache = new FileSystemManagerCache();
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject = new HashMap<String, List<FileSystemManager>>();
        final List<FileSystemManager> fsmList = new ArrayList<FileSystemManager>();
        for (int i = 0; i < 5; i++) {
            fsmList.add(DtsVfsUtil.createNewFsManager(true, true, true, true, true, true, true, "/tmp"));
        }
        fileSystemManagersPerRootFileObject.put(ROOT_FILE_OBJECT, fsmList);
        mFileSystemManagerCache.initFileSystemManagerCache(fileSystemManagersPerRootFileObject);

        mFileSystemManagerCache.clear();
        Assert.assertEquals(mFileSystemManagerCache.getSizeOfAvailableFileSystemManagers(ROOT_FILE_OBJECT), 0);
    }
}
