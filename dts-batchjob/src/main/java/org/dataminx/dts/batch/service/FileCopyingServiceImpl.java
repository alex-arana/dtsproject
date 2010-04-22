/**
 * Copyright (c) 2009, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
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
package org.dataminx.dts.batch.service;

import java.io.IOException;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.Selectors;
import org.dataminx.dts.common.vfs.DtsVfsUtil;
import org.dataminx.dts.security.crypto.DummyEncrypter;
import org.dataminx.dts.security.crypto.Encrypter;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import uk.ac.dl.escience.vfs.util.MarkerListenerImpl;
import uk.ac.dl.escience.vfs.util.VFSUtil;

/**
 * Default implementation of {@link FileCopyingService}.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
public class FileCopyingServiceImpl implements FileCopyingService,
    InitializingBean {
    /** A reference to the internal logger object. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(FileCopyingServiceImpl.class);

    /** Default file selector to be used during file copy operations. */
    private static final FileSelector DEFAULT_FILE_SELECTOR = Selectors.SELECT_ALL;

    /**
     * Flag that determines whether to preserve the last modified timestamp when
     * copying files. TODO read this option from the job details instead... is
     * this a potential addition to the schema?
     */
    private final boolean mPreserveLastModified = true;

    /** A reference to the DtsVfsUtil. */
    private DtsVfsUtil mDtsVfsUtil;

    /** A reference to the Encrypter. */
    private Encrypter mEncrypter;

    /**
     * {@inheritDoc}
     */
    public void copyFiles(final String sourceURI, final String targetURI,
        final FileSystemManager sourceFileSystemManager,
        final FileSystemManager targetFileSystemManager) {
        LOGGER.info(String.format("Copying source '%s' to target '%s'...",
            sourceURI, targetURI));
        try {
            copyFiles(sourceFileSystemManager.resolveFile(sourceURI),
                targetFileSystemManager.resolveFile(targetURI));
        }
        catch (final FileSystemException ex) {
            LOGGER.error("An error has occurred during a file copy operation: "
                + ex, ex);
            throw new DtsFileCopyOperationException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void copyFiles(final SourceTargetType source,
        final SourceTargetType target,
        final FileSystemManager sourceFileSystemManager,
        final FileSystemManager targetFileSystemManager) {
        LOGGER.info(String.format("Copying source '%s' to target '%s'...",
            source.getURI(), target.getURI()));
        try {
            copyFiles(sourceFileSystemManager.resolveFile(source.getURI(),
                mDtsVfsUtil.createFileSystemOptions(source, mEncrypter)),
                targetFileSystemManager.resolveFile(target.getURI(),
                    mDtsVfsUtil.createFileSystemOptions(target, mEncrypter)));
        }
        catch (final FileSystemException ex) {
            LOGGER.error("An error has occurred during a file copy operation: "
                + ex, ex);
            throw new DtsFileCopyOperationException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void copyFiles(final String sourceURI, final String targetURI,
        final DataTransferType dataTransferType,
        final FileSystemManager sourceFileSystemManager,
        final FileSystemManager targetFileSystemManager) {
        final SourceTargetType source = dataTransferType.getSource();
        final SourceTargetType target = dataTransferType.getTarget();
        FileObject sourceFO = null;
        FileObject targetFO = null;

        try {
            sourceFO = sourceFileSystemManager.resolveFile(sourceURI,
                mDtsVfsUtil.createFileSystemOptions(source, mEncrypter));
            targetFO = targetFileSystemManager.resolveFile(targetURI,
                mDtsVfsUtil.createFileSystemOptions(target, mEncrypter));
            copyFiles(sourceFO, targetFO);
        }
        catch (final FileSystemException e) {
            LOGGER.error("An error has occurred during a file copy operation: "
                + e, e);
            throw new DtsFileCopyOperationException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void copyFiles(final String sourceURI, final String targetURI,
        final FileSystemManager fileSystemManager) {
        copyFiles(sourceURI, targetURI, fileSystemManager, fileSystemManager);
    }

    /**
     * {@inheritDoc}
     */
    public void copyFiles(final SourceTargetType source,
        final SourceTargetType target, final FileSystemManager fileSystemManager) {
        copyFiles(source, target, fileSystemManager, fileSystemManager);
    }

    /**
     * {@inheritDoc}
     */
    public void copyFiles(final String sourceURI, final String targetURI,
        final DataTransferType dataTransferType,
        final FileSystemManager fileSystemManager) {
        copyFiles(sourceURI, targetURI, dataTransferType, fileSystemManager,
            fileSystemManager);
    }

    /**
     * Copies the content from a source file to a destination file.
     *
     * @param sourceFile Source file to copy from
     * @param destinationFile Destination file to copy
     * @throws FileSystemException when an error occurs during a VFS file copy
     *         operation.
     */
    private void copyFiles(final FileObject sourceFile,
        final FileObject destinationFile) throws FileSystemException {
        Assert.notNull(sourceFile);
        Assert.notNull(destinationFile);

        // TODO handle overwrites
        // only works on file to file type of transfer
        // destinationFile.copyFrom(sourceFile, DEFAULT_FILE_SELECTOR);

        // TODO we might later decide to not use VFSUtil.copy if we start
        // tracking a file copy progress.
        // dealing directly with ObjectFiles will give more control on what we
        // do in every step of the
        // data transfer process.
        try {
            VFSUtil.copy(sourceFile, destinationFile, new MarkerListenerImpl(),
                true);

        }
        catch (final IOException e) {
            LOGGER
                .error(String
                    .format(
                        "IOException was thrown while trying to copy source '%s' to target '%s\n%s",
                        sourceFile.getURL().toString(), destinationFile
                            .getURL().toString(), e.getMessage()));
            throw new FileSystemException(e.getMessage(), e.getCause());
        }

        if (mPreserveLastModified
            && sourceFile.getFileSystem().hasCapability(
                Capability.GET_LAST_MODIFIED)
            && destinationFile.getFileSystem().hasCapability(
                Capability.SET_LAST_MODIFIED_FILE)) {
            final long lastModTime = sourceFile.getContent()
                .getLastModifiedTime();
            destinationFile.getContent().setLastModifiedTime(lastModTime);
        }
    }

    public void setDtsVfsUtil(final DtsVfsUtil dtsVfsUtil) {
        mDtsVfsUtil = dtsVfsUtil;
    }

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(mDtsVfsUtil != null, "DtsVfsUtil has not been set.");
        if (mEncrypter == null) {
            mEncrypter = new DummyEncrypter();
        }
    }

    /**
     * Sets the Encrypter.
     *
     * @param encrypter the Encrypter
     */
    public void setEncrypter(final Encrypter encrypter) {
        mEncrypter = encrypter;
    }

}
