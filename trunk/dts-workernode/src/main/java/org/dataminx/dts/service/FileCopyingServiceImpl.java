/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.Selectors;
import org.dataminx.dts.common.util.StopwatchTimer;
import org.dataminx.dts.vfs.DtsFileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link FileCopyingService}.
 *
 * @author Alex Arana
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
    public void copyFiles(String sourceURI, String targetURI) {
        LOG.info(String.format("Copying source '%s' to target '%s'...", sourceURI, targetURI));
        try {
            final StopwatchTimer timer = new StopwatchTimer();
            copyFiles(mFileSystemManager.resolveFile(sourceURI), mFileSystemManager.resolveFile(targetURI));
            LOG.info(String.format("Finished copying source '%s' to target '%s' in %s.",
                sourceURI, targetURI, timer.getFormattedElapsedTime()));
        }
        catch (FileSystemException ex) {
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
        destinationFile.copyFrom(sourceFile, DEFAULT_FILE_SELECTOR);
        if (mPreserveLastModified
            && sourceFile.getFileSystem().hasCapability(Capability.GET_LAST_MODIFIED)
            && destinationFile.getFileSystem().hasCapability(Capability.SET_LAST_MODIFIED_FILE))
        {
            final long lastModTime = sourceFile.getContent().getLastModifiedTime();
            destinationFile.getContent().setLastModifiedTime(lastModTime);
        }
    }
}