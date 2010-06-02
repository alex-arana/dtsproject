/**
 * Copyright (c) 2010, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.common.vfs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.dataminx.dts.common.batch.util.FileObjectMap;
import org.springframework.util.Assert;

/**
 * A FileSystemManagerCache class is used as a cache to store FileSystemManager objects
 * that have been instantiated by the MaxSteamCounterTask.
 *
 * @author Gerson Galang
 */
public class FileSystemManagerCache {

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(FileSystemManagerCache.class);

    /** A map of the root FileObject and their corresponding stack of FileSystemManagers. */
    private final Map<String, Stack<FileSystemManager>> mFsmAvailableStackPerRootFileObject;

    /** A map of the root FileObject and their corrresponding list of FileSystemManagers that are on loan. */
    private final Map<String, List<FileSystemManager>> mFsmOnLoanListPerRootFileObject;

    /**
     * Default constructor of the FileSystemManagerCache.
     */
    public FileSystemManagerCache() {
        mFsmAvailableStackPerRootFileObject = new FileObjectMap<String, Stack<FileSystemManager>>();
        mFsmOnLoanListPerRootFileObject = new FileObjectMap<String, List<FileSystemManager>>();
    }

    /**
     * Borrows the FileSystemManager for the given root FileObject.
     *
     * @param rootFileObject the key to be used for the borrowed FileSystemManager
     * @return the cached FileSystemManager for the given root FileObject
     * @throws UnknownRootFileObjectException if the root FileObject is not found in the cache
     */
    public synchronized FileSystemManager borrowOne(final String rootFileObject)
        throws UnknownRootFileObjectException {
        if (mFsmAvailableStackPerRootFileObject.containsKey(rootFileObject)) {
            final Stack<FileSystemManager> fsmAvailableStack = mFsmAvailableStackPerRootFileObject
                .get(rootFileObject);
            if (!fsmAvailableStack.isEmpty()) {
                LOGGER.debug("Borrowing a FileSystemManager for \""
                    + rootFileObject + "\" from the cache.");
                final FileSystemManager borrowedFileSystemManager = fsmAvailableStack
                    .pop();
                Assert.notNull(borrowedFileSystemManager);

                mFsmOnLoanListPerRootFileObject.get(rootFileObject).add(
                    borrowedFileSystemManager);
                return borrowedFileSystemManager;
            }
            return null;
        }
        throw new UnknownRootFileObjectException(rootFileObject
            + " is not in the FileSystemManagerCache.");
    }

    /**
     * Clears the cache by removing all the FileSystemObjects that have been cached.
     */
    public synchronized void clear() {
        LOGGER.debug("FileSystemManagerCache clear()");
        FileSystemManager tmpFileSystemManager = null;

        // we'll use this variable to hold the keys of the FileObjectMaps as we can't
        // delete entries to the map while we are iterating through the items in the map
        List<String> rootFileObjectKeys = new ArrayList<String>();

        for (final String rootFileObjectString : mFsmAvailableStackPerRootFileObject
            .keySet()) {
            while (!mFsmAvailableStackPerRootFileObject.get(
                rootFileObjectString).empty()) {
                tmpFileSystemManager = mFsmAvailableStackPerRootFileObject.get(
                    rootFileObjectString).pop();
                LOGGER
                    .debug("Closing FileSystemManager on the AvailableStack...");
                ((DefaultFileSystemManager) tmpFileSystemManager).close();
            }

            for (final FileSystemManager fsm : mFsmOnLoanListPerRootFileObject
                .get(rootFileObjectString)) {
                LOGGER
                    .debug("Closing FileSystemManagers on the OnLoandList...");
                ((DefaultFileSystemManager) fsm).close();
            }

            rootFileObjectKeys.add(rootFileObjectString);
        }

        // now we can safely delete items from the FileObjectMap
        for (final String rootFileObjectString : rootFileObjectKeys) {
            mFsmAvailableStackPerRootFileObject.remove(rootFileObjectString);
            mFsmOnLoanListPerRootFileObject.remove(rootFileObjectString);
        }

        // let's have this list garbage collected
        rootFileObjectKeys = null;

    }

    /**
     * Gets the size of the cached FileSystemManagers for the given root FileObject.
     *
     * @param rootFileObject the root FileObject
     * @return the size of cached FileSystemManagers for the given root FileObject
     * @throws UnknownRootFileObjectException if the root FileObject is not in the cache
     */
    public synchronized int getSizeOfAvailableFileSystemManagers(
        final String rootFileObject) throws UnknownRootFileObjectException {
        if (mFsmAvailableStackPerRootFileObject.containsKey(rootFileObject)) {
            return mFsmAvailableStackPerRootFileObject.get(rootFileObject)
                .size();
        }
        else {
            throw new UnknownRootFileObjectException(rootFileObject
                + " is not in the FileSystemManagerCache.");
        }
    }

    /**
     * Initialises the FileSystemManagerCache object.
     *
     * @param fileSystemManagersPerRootFileObject A map of the root FileObjects and their corrresponding
     *        list of FileSystemManagers that are on loan.
     * @throws FileSystemManagerCacheAlreadyInitializedException if the cache has already been initialised
     *         (ie already contains FileSystemManagers)
     */
    public synchronized void initFileSystemManagerCache(
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject)
        throws FileSystemManagerCacheAlreadyInitializedException {
        LOGGER.debug("FileSystemManagerCache initFileSystemManagerCache()");

        if (!mFsmAvailableStackPerRootFileObject.isEmpty()
            || !mFsmOnLoanListPerRootFileObject.isEmpty()) {
            throw new FileSystemManagerCacheAlreadyInitializedException();
        }

        for (final String rootFileObjectString : fileSystemManagersPerRootFileObject
            .keySet()) {

            final Stack<FileSystemManager> fsmAvailableStack = new Stack<FileSystemManager>();
            final List<FileSystemManager> fileSystemManagers = fileSystemManagersPerRootFileObject
                .get(rootFileObjectString);

            LOGGER.debug("Caching " + fileSystemManagers.size()
                + " concurrent connections for \"" + rootFileObjectString
                + "\"");
            for (final FileSystemManager fsm : fileSystemManagers) {
                fsmAvailableStack.push(fsm);
            }

            mFsmAvailableStackPerRootFileObject.put(rootFileObjectString,
                fsmAvailableStack);
            mFsmOnLoanListPerRootFileObject.put(rootFileObjectString,
                new ArrayList<FileSystemManager>());
        }
    }

    /**
     * Returns the FileSystemManager that was previously borrowed.
     *
     * @param rootFileObject the root FileObject that owns the FileSystemManager that is being returned
     * @param fileSystemManager the FileSystemManager being returned
     * @throws UnknownFileSystemManagerException if the returned FileSystemManager does not belong to the
     *         root FileObject
     * @throws UnknownRootFileObjectException if the root FileObject is not in the cache
     */
    public synchronized void returnOne(final String rootFileObject,
        final FileSystemManager fileSystemManager)
        throws UnknownFileSystemManagerException,
        UnknownRootFileObjectException {
        if (mFsmOnLoanListPerRootFileObject.containsKey(rootFileObject)) {
            // we'll only allow the return of a file system manager if it's in the cache
            final List<FileSystemManager> fsmOnLoanList = mFsmOnLoanListPerRootFileObject
                .get(rootFileObject);
            if (fsmOnLoanList.contains(fileSystemManager)) {
                LOGGER.debug("Returning a FileSystemManager for \""
                    + rootFileObject + "\" to the cache.");
                if (fsmOnLoanList.remove(fileSystemManager)) {
                    mFsmAvailableStackPerRootFileObject.get(rootFileObject)
                        .push(fileSystemManager);
                }
            }
            else {
                throw new UnknownFileSystemManagerException(
                    "Unknown FileSystemManager cannot be added into the cache for \""
                        + rootFileObject + "\"");
            }
        }
        else {
            throw new UnknownRootFileObjectException(rootFileObject
                + " is not in the FileSystemManagerCache.");
        }
    }

}
