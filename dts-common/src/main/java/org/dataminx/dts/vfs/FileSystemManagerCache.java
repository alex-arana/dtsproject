package org.dataminx.dts.vfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;

public class FileSystemManagerCache {

    private static final Log LOGGER = LogFactory.getLog(FileSystemManagerCache.class);
    private Stack<FileSystemManager> fsmAvailableStack;
    private List<FileSystemManager> fsmOnLoanList;
    private int mSize;

    public FileSystemManagerCache() {
        fsmAvailableStack = new Stack<FileSystemManager>();
        fsmOnLoanList = new ArrayList<FileSystemManager>();
        mSize = 0;
    }

    public int getSize() {
        return mSize;
    }

    public void returnOne(final FileSystemManager fileSystemManager) throws UnknownFileSystemManagerException {
        // we'll only allow the return of a file system manager if it's in the cache
        if (fsmOnLoanList.contains(fileSystemManager)) {
            if (fsmOnLoanList.remove(fileSystemManager)) {
                fsmAvailableStack.push(fileSystemManager);
            }
        }
        else {
            throw new UnknownFileSystemManagerException();
        }
    }

    public FileSystemManager borrowOne() {
        final FileSystemManager borrowedFileSystemManager = fsmAvailableStack.pop();
        fsmOnLoanList.add(borrowedFileSystemManager);
        return borrowedFileSystemManager;
    }

    public void initFileSystemManagerCache(final List<FileSystemManager> fileSystemManagers)
            throws FileSystemManagerAlreadyInitializedException {
        LOGGER.debug("FileSystemManagerCache initFileSystemManagerCache()");

        mSize = fileSystemManagers.size();
        LOGGER.debug("FileSystemManagerCache contains " + mSize + " FileSystemManager instances");

        // we'll only allow for the cache to be initialised once
        if (!fsmAvailableStack.isEmpty() || !fsmOnLoanList.isEmpty()) {
            throw new FileSystemManagerAlreadyInitializedException();
        }
        for (final FileSystemManager fsm : fileSystemManagers) {
            fsmAvailableStack.push(fsm);
        }
    }

    public void clear() {
        LOGGER.debug("FileSystemManagerCache clear()");
        FileSystemManager tmpFileSystemManager = null;
        while (!fsmAvailableStack.empty()) {
            tmpFileSystemManager = fsmAvailableStack.pop();
            LOGGER.debug("closing FileSystemManager...");
            ((DefaultFileSystemManager) tmpFileSystemManager).close();
        }
        for (final FileSystemManager fsm : fsmOnLoanList) {
            LOGGER.debug("closing FileSystemManager...");
            ((DefaultFileSystemManager) fsm).close();
        }
        fsmOnLoanList = new ArrayList<FileSystemManager>();
        fsmAvailableStack = new Stack<FileSystemManager>();
        mSize = 0;
    }

}
