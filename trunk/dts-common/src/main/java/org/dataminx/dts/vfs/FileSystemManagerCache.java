package org.dataminx.dts.vfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dataminx.dts.batch.common.util.FileObjectMap;
import org.springframework.util.Assert;

public class FileSystemManagerCache {

    private static final Log LOGGER = LogFactory.getLog(FileSystemManagerCache.class);

    private final Map<String, Stack<FileSystemManager>> fsmAvailableStackPerRootFileObject;
    private final Map<String, List<FileSystemManager>> fsmOnLoanListPerRootFileObject;

    public FileSystemManagerCache() {
        fsmAvailableStackPerRootFileObject = new FileObjectMap<String, Stack<FileSystemManager>>();
        fsmOnLoanListPerRootFileObject = new FileObjectMap<String, List<FileSystemManager>>();
    }

    public synchronized int getSizeOfAvailableFileSystemManagers(final String rootFileObject) {
        return fsmAvailableStackPerRootFileObject.get(rootFileObject).size();
    }

    public synchronized void returnOne(final String rootFileObject, final FileSystemManager fileSystemManager)
            throws UnknownFileSystemManagerException, UnknownRootFileObjectException {
        if (fsmOnLoanListPerRootFileObject.containsKey(rootFileObject)) {
            // we'll only allow the return of a file system manager if it's in the cache
            final List<FileSystemManager> fsmOnLoanList = fsmOnLoanListPerRootFileObject.get(rootFileObject);
            if (fsmOnLoanList.contains(fileSystemManager)) {
                LOGGER.debug("Returning a FileSystemManager for \"" + rootFileObject + "\" to the cache.");
                if (fsmOnLoanList.remove(fileSystemManager)) {
                    fsmAvailableStackPerRootFileObject.get(rootFileObject).push(fileSystemManager);
                }
            }
            else {
                throw new UnknownFileSystemManagerException(
                        "Unknown FileSystemManager cannot be added into the cache for \"" + rootFileObject + "\"");
            }
        }
        else {
            throw new UnknownRootFileObjectException(rootFileObject + " is not in the FileSystemManagerCache.");
        }
    }

    public synchronized FileSystemManager borrowOne(final String rootFileObject) throws UnknownRootFileObjectException {
        if (fsmAvailableStackPerRootFileObject.containsKey(rootFileObject)) {
            final Stack<FileSystemManager> fsmAvailableStack = fsmAvailableStackPerRootFileObject.get(rootFileObject);
            if (!fsmAvailableStack.isEmpty()) {
                LOGGER.debug("Borrowing a FileSystemManager for \"" + rootFileObject + "\" from the cache.");
                final FileSystemManager borrowedFileSystemManager = fsmAvailableStack.pop();
                Assert.notNull(borrowedFileSystemManager);

                fsmOnLoanListPerRootFileObject.get(rootFileObject).add(borrowedFileSystemManager);
                return borrowedFileSystemManager;
            }
            return null;
        }
        throw new UnknownRootFileObjectException(rootFileObject + " is not in the FileSystemManagerCache.");
    }

    public synchronized void initFileSystemManagerCache(
            final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject)
            throws FileSystemManagerCacheAlreadyInitializedException {
        LOGGER.debug("FileSystemManagerCache initFileSystemManagerCache()");

        if (!fsmAvailableStackPerRootFileObject.isEmpty() || !fsmOnLoanListPerRootFileObject.isEmpty()) {
            throw new FileSystemManagerCacheAlreadyInitializedException();
        }

        for (final String rootFileObjectString : fileSystemManagersPerRootFileObject.keySet()) {

            final Stack<FileSystemManager> fsmAvailableStack = new Stack<FileSystemManager>();
            final List<FileSystemManager> fileSystemManagers = fileSystemManagersPerRootFileObject
                    .get(rootFileObjectString);

            LOGGER.debug("Caching " + fileSystemManagers.size() + " concurrent connections for \""
                    + rootFileObjectString + "\"");
            for (final FileSystemManager fsm : fileSystemManagers) {
                fsmAvailableStack.push(fsm);
            }

            fsmAvailableStackPerRootFileObject.put(rootFileObjectString, fsmAvailableStack);
            fsmOnLoanListPerRootFileObject.put(rootFileObjectString, new ArrayList<FileSystemManager>());
        }
    }

    public synchronized void clear() {
        LOGGER.debug("FileSystemManagerCache clear()");
        FileSystemManager tmpFileSystemManager = null;

        // we'll use this variable to hold the keys of the FileObjectMaps as we can't
        // delete entries to the map while we are iterating through the items in the map
        List<String> rootFileObjectKeys = new ArrayList<String>();

        for (final String rootFileObjectString : fsmAvailableStackPerRootFileObject.keySet()) {
            while (!fsmAvailableStackPerRootFileObject.get(rootFileObjectString).empty()) {
                tmpFileSystemManager = fsmAvailableStackPerRootFileObject.get(rootFileObjectString).pop();
                LOGGER.debug("Closing FileSystemManager on the AvailableStack...");
                ((DefaultFileSystemManager) tmpFileSystemManager).close();
            }

            for (final FileSystemManager fsm : fsmOnLoanListPerRootFileObject.get(rootFileObjectString)) {
                LOGGER.debug("Closing FileSystemManagers on the OnLoandList...");
                ((DefaultFileSystemManager) fsm).close();
            }

            rootFileObjectKeys.add(rootFileObjectString);
        }

        // now we can safely delete items from the FileObjectMap
        for (final String rootFileObjectString : rootFileObjectKeys) {
            fsmAvailableStackPerRootFileObject.remove(rootFileObjectString);
            fsmOnLoanListPerRootFileObject.remove(rootFileObjectString);
        }

        // let's have this list garbage collected
        rootFileObjectKeys = null;

    }

}
