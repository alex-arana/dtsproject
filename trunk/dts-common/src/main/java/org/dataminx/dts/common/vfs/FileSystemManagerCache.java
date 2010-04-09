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

public class FileSystemManagerCache {

    private static final Log LOGGER = LogFactory
        .getLog(FileSystemManagerCache.class);

    private final Map<String, Stack<FileSystemManager>> fsmAvailableStackPerRootFileObject;
    private final Map<String, List<FileSystemManager>> fsmOnLoanListPerRootFileObject;

    public FileSystemManagerCache() {
        fsmAvailableStackPerRootFileObject = new FileObjectMap<String, Stack<FileSystemManager>>();
        fsmOnLoanListPerRootFileObject = new FileObjectMap<String, List<FileSystemManager>>();
    }

    public synchronized FileSystemManager borrowOne(final String rootFileObject)
        throws UnknownRootFileObjectException {
        if (fsmAvailableStackPerRootFileObject.containsKey(rootFileObject)) {
            final Stack<FileSystemManager> fsmAvailableStack = fsmAvailableStackPerRootFileObject
                .get(rootFileObject);
            if (!fsmAvailableStack.isEmpty()) {
                LOGGER.debug("Borrowing a FileSystemManager for \""
                    + rootFileObject + "\" from the cache.");
                final FileSystemManager borrowedFileSystemManager = fsmAvailableStack
                    .pop();
                Assert.notNull(borrowedFileSystemManager);

                fsmOnLoanListPerRootFileObject.get(rootFileObject).add(
                    borrowedFileSystemManager);
                return borrowedFileSystemManager;
            }
            return null;
        }
        throw new UnknownRootFileObjectException(rootFileObject
            + " is not in the FileSystemManagerCache.");
    }

    public synchronized void clear() {
        LOGGER.debug("FileSystemManagerCache clear()");
        FileSystemManager tmpFileSystemManager = null;

        // we'll use this variable to hold the keys of the FileObjectMaps as we can't
        // delete entries to the map while we are iterating through the items in the map
        List<String> rootFileObjectKeys = new ArrayList<String>();

        for (final String rootFileObjectString : fsmAvailableStackPerRootFileObject
            .keySet()) {
            while (!fsmAvailableStackPerRootFileObject
                .get(rootFileObjectString).empty()) {
                tmpFileSystemManager = fsmAvailableStackPerRootFileObject.get(
                    rootFileObjectString).pop();
                LOGGER
                    .debug("Closing FileSystemManager on the AvailableStack...");
                ((DefaultFileSystemManager) tmpFileSystemManager).close();
            }

            for (final FileSystemManager fsm : fsmOnLoanListPerRootFileObject
                .get(rootFileObjectString)) {
                LOGGER
                    .debug("Closing FileSystemManagers on the OnLoandList...");
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

    public synchronized int getSizeOfAvailableFileSystemManagers(
        final String rootFileObject) throws UnknownRootFileObjectException {
        if (fsmAvailableStackPerRootFileObject.containsKey(rootFileObject)) {
            return fsmAvailableStackPerRootFileObject.get(rootFileObject)
                .size();
        }
        else {
            throw new UnknownRootFileObjectException(rootFileObject
                + " is not in the FileSystemManagerCache.");
        }
    }

    public synchronized void initFileSystemManagerCache(
        final Map<String, List<FileSystemManager>> fileSystemManagersPerRootFileObject)
        throws FileSystemManagerCacheAlreadyInitializedException {
        LOGGER.debug("FileSystemManagerCache initFileSystemManagerCache()");

        if (!fsmAvailableStackPerRootFileObject.isEmpty()
            || !fsmOnLoanListPerRootFileObject.isEmpty()) {
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

            fsmAvailableStackPerRootFileObject.put(rootFileObjectString,
                fsmAvailableStack);
            fsmOnLoanListPerRootFileObject.put(rootFileObjectString,
                new ArrayList<FileSystemManager>());
        }
    }

    public synchronized void returnOne(final String rootFileObject,
        final FileSystemManager fileSystemManager)
        throws UnknownFileSystemManagerException,
        UnknownRootFileObjectException {
        if (fsmOnLoanListPerRootFileObject.containsKey(rootFileObject)) {
            // we'll only allow the return of a file system manager if it's in the cache
            final List<FileSystemManager> fsmOnLoanList = fsmOnLoanListPerRootFileObject
                .get(rootFileObject);
            if (fsmOnLoanList.contains(fileSystemManager)) {
                LOGGER.debug("Returning a FileSystemManager for \""
                    + rootFileObject + "\" to the cache.");
                if (fsmOnLoanList.remove(fileSystemManager)) {
                    fsmAvailableStackPerRootFileObject.get(rootFileObject)
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
