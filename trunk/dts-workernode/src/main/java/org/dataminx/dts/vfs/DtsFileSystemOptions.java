/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.vfs;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for default DTS file options.
 *
 * @author Alex Arana
 */
public class DtsFileSystemOptions extends FileSystemOptions {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsFileSystemOptions.class);

    /**
     * Default constructor for instances of {@link DtsFileSystemOptions}.
     */
    DtsFileSystemOptions() {
        initialiseDefaults();
    }

    /**
     * Initialises the set of default VFS file system options to be used.
     */
    private void initialiseDefaults() {
        // use Passive FTP
        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(this, true);

        try {
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(this, "no");
        }
        catch (final FileSystemException ex) {
            LOG.warn("Unable to disable SFTP strict host key checking", ex);
        }
    }
}
