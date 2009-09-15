/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.service;

import java.io.IOException;
import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.Selectors;
import org.dataminx.dts.wn.common.util.StopwatchTimer;
import org.dataminx.dts.wn.vfs.DtsFileSystemManager;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;
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

    /** A reference to the VFS file manager. */
    @Autowired
    private DtsFileSystemManager mFileSystemManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void copyFiles(final String sourceURI, final String targetURI) {
        LOG.info(String.format("Copying source '%s' to target '%s'...", sourceURI, targetURI));
        try {
            final StopwatchTimer timer = new StopwatchTimer();
            copyFiles(mFileSystemManager.resolveFile(sourceURI), mFileSystemManager.resolveFile(targetURI));
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
    public void copyFiles(final SourceTargetType source, final SourceTargetType target) {
        LOG.info(String.format("Copying source '%s' to target '%s'...", source.getURI(), target.getURI()));
        try {
            final StopwatchTimer timer = new StopwatchTimer();
            copyFiles(mFileSystemManager.resolveFile(source), mFileSystemManager.resolveFile(target));
            LOG.info(String.format("Finished copying source '%s' to target '%s' in %s.",
                source.getURI(), target.getURI(), timer.getFormattedElapsedTime()));
        }
        catch (final FileSystemException ex) {
            LOG.error("An error has occurred during a file copy operation: " + ex, ex);
            throw new DtsFileCopyOperationException(ex);
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
            VFSUtil.copy(sourceFile, destinationFile, new MarkerListenerImpl(), true);
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
}
