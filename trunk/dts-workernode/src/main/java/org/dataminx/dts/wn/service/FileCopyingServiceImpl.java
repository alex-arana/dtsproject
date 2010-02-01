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
package org.dataminx.dts.wn.service;

import java.io.IOException;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.Selectors;
import org.dataminx.dts.vfs.DtsVfsUtil;
import org.dataminx.dts.wn.common.util.StopwatchTimer;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;
import org.globus.ftp.MarkerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.ac.dl.escience.vfs.util.MarkerListenerImpl;
import uk.ac.dl.escience.vfs.util.VFSUtil;

/**
 * Default implementation of {@link FileCopyingService}.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
@Service("fileCopyingService")
@Scope("singleton")
public class FileCopyingServiceImpl implements FileCopyingService {
    /** A reference to the internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(FileCopyingServiceImpl.class);

    /** Default file selector to be used during file copy operations. */
    private static final FileSelector DEFAULT_FILE_SELECTOR = Selectors.SELECT_ALL;

    /**
     * Flag that determines whether to preserve the last modified timestamp when copying files.
     * TODO read this option from the job details instead... is this a potential addition to the schema?
     */
    private final boolean mPreserveLastModified = true;
    
    @Autowired
    private DtsVfsUtil mDtsVfsUtil;

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyFiles(final String sourceURI, final String targetURI, FileSystemManager fileSystemManager) {
        LOG.info(String.format("Copying source '%s' to target '%s'...", sourceURI, targetURI));
        try {
            final StopwatchTimer timer = new StopwatchTimer();
            copyFiles(fileSystemManager.resolveFile(sourceURI), fileSystemManager.resolveFile(targetURI));
            LOG.info(String.format("Finished copying source '%s' to target '%s' in %s.",
                sourceURI, targetURI, timer.getFormattedElapsedTime()));
        }
        catch (final FileSystemException ex) {
            LOG.error("An error has occurred during a file copy operation: " + ex, ex);
            throw new DtsFileCopyOperationException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void copyFiles(final SourceTargetType source, final SourceTargetType target, 
    		FileSystemManager fileSystemManager) {
        LOG.info(String.format("Copying source '%s' to target '%s'...", source.getURI(), target.getURI()));
        try {
            final StopwatchTimer timer = new StopwatchTimer();
            copyFiles(fileSystemManager.resolveFile(source.getURI(), 
            		mDtsVfsUtil.createFileSystemOptions(source)), 
            		fileSystemManager.resolveFile(target.getURI(),
            		mDtsVfsUtil.createFileSystemOptions(target)));
            LOG.info(String.format("Finished copying source '%s' to target '%s' in %s.",
                source.getURI(), target.getURI(), timer.getFormattedElapsedTime()));
        }
        catch (final FileSystemException ex) {
            LOG.error("An error has occurred during a file copy operation: " + ex, ex);
            throw new DtsFileCopyOperationException(ex);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void copyFiles(String sourceURI, String targetURI, DataTransferType dataTransferType, 
    		FileSystemManager fileSystemManager)  {
    	SourceTargetType source = dataTransferType.getSource();
    	SourceTargetType target = dataTransferType.getTarget();
    	FileObject sourceFO = null;
    	FileObject targetFO = null;
    	
    	try {
	        sourceFO = fileSystemManager.resolveFile(sourceURI, mDtsVfsUtil.createFileSystemOptions(source));
	        targetFO = fileSystemManager.resolveFile(targetURI, mDtsVfsUtil.createFileSystemOptions(target));
	        copyFiles(sourceFO, targetFO);
        } catch (FileSystemException e) {
        	LOG.error("An error has occurred during a file copy operation: " + e, e);
            throw new DtsFileCopyOperationException(e);
        }
    }

    /**
     * Copies the content from a source file to a destination file.
     *
     * @param sourceFile Source file to copy from
     * @param destinationFile Destination file to copy
     * @throws FileSystemException when an error occurs during a VFS file copy operation.
     */
    private void copyFiles(final FileObject sourceFile, final FileObject destinationFile) throws FileSystemException {
        Assert.notNull(sourceFile);
        Assert.notNull(destinationFile);

        //TODO handle overwrites
        // only works on file to file type of transfer
        //destinationFile.copyFrom(sourceFile, DEFAULT_FILE_SELECTOR);

        //TODO we might later decide to not use VFSUtil.copy if we start tracking a file copy progress.
        //     dealing directly with ObjectFiles will give more control on what we do in every step of the
        //     data transfer process.
        try {
            VFSUtil.copy(sourceFile, destinationFile, (MarkerListener) new MarkerListenerImpl(), true);
        }
        catch (IOException e) {
            LOG.error(String.format("IOException was thrown while trying to copy source '%s' to target '%s\n%s",
                    sourceFile.getURL().toString(), destinationFile.getURL().toString(), e.getMessage()));
            throw new FileSystemException(e.getMessage(), e.getCause());
        }

        if (mPreserveLastModified
            && sourceFile.getFileSystem().hasCapability(Capability.GET_LAST_MODIFIED)
            && destinationFile.getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE))
        {
            final long lastModTime = sourceFile.getContent().getLastModifiedTime();
            destinationFile.getContent().setLastModifiedTime(lastModTime);
        }
    }
    
    public void setDtsVfsUtil(DtsVfsUtil dtsVfsUtil) {
    	mDtsVfsUtil = dtsVfsUtil;
    }
}
