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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;

/**
 * A ThreadLocal FileSystemManager dispenser.
 *
 * @author Gerson Galang
 */
public class FileSystemManagerDispenser {

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(FileSystemManagerDispenser.class);

    /** A reference to the DtsVfsUtil. */
    private DtsVfsUtil mDtsVfsUtil;

    /** The ThreadLocal version of this FileSystemManager. */
    private final ThreadLocal<DefaultFileSystemManager> mThreadLocalFsManager =
        new ThreadLocal<DefaultFileSystemManager>() {
        @Override
        protected DefaultFileSystemManager initialValue() {
            try {
                return mDtsVfsUtil.createNewFsManager();
            }
            catch (final FileSystemException e) {
                LOGGER
                    .error(
                        "FileSystemException was thrown while creating a new FileSystemManager",
                        e);
            }
            return null;
        }

        @Override
        public void remove() {
            get().close();
        }
    };

    /**
     * Gets a FileSystemManager for the running thread.
     *
     * @return a FileSystemManager for the running thread
     */
    public DefaultFileSystemManager getFileSystemManager() {
        LOGGER.debug("Instantiating a new FileSystemManager");
        return mThreadLocalFsManager.get();
    }

    /**
     * Closes the FileSystemManager for the current running thread.
     */
    public void closeFileSystemManager() {
        LOGGER.debug("Closing FileSystemManager");
        mThreadLocalFsManager.remove();
    }

    /**
     * Sets the DtsVfsUtil.
     *
     * @param dtsVfsUtil the DtsVfsUtil
     */
    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

}
