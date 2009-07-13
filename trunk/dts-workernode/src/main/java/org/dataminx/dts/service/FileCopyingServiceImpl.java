/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

import static org.dataminx.dts.common.DtsConfigManager.DEFAULT_MYPROXY_CREDENTIAL_LIFETIME_KEY;

import org.apache.commons.vfs.Capability;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.ftp.FtpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.gridftp.cogjglobus.GridFtpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.dataminx.dts.common.DtsConfigManager;
import org.dataminx.dts.common.util.StopwatchTimer;
import org.dataminx.dts.vfs.DtsFileSystemManager;
import org.dataminx.schemas.dts._2009._05.dts.CredentialType;
import org.dataminx.schemas.dts._2009._05.dts.MyProxyTokenType;
import org.dataminx.schemas.dts._2009._05.dts.SourceTargetType;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.UsernameTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

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

    /** A reference to the DTS Configuration manager. */
    @Autowired
    private DtsConfigManager mDtsConfigManager;

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
     * {@inheritDoc}
     */
    public void copyFiles(SourceTargetType source, SourceTargetType target) {
        LOG.info(String.format("Copying source '%s' to target '%s'...", source.getURI(), target.getURI()));
        try {
            final StopwatchTimer timer = new StopwatchTimer();

            // create the FileSystemOptions based on Source and Target
            FileSystemOptions sourceOption = createFileSystemOptions(source);
            FileSystemOptions targetOption = createFileSystemOptions(target);

            copyFiles(mFileSystemManager.resolveFile(source.getURI(), sourceOption),
                mFileSystemManager.resolveFile(target.getURI(), targetOption));

            LOG.info(String.format("Finished copying source '%s' to target '%s' in %s.",
                source.getURI(), target.getURI(), timer.getFormattedElapsedTime()));
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

    /**
     * Create the FileSystemOptions based on the provided source or target.
     *
     * @param sourceOrTarget the source or target
     * @return the FileSystemOptions for the given source or target
     */
    private FileSystemOptions createFileSystemOptions(SourceTargetType sourceOrTarget)
            throws FileSystemException {
        FileSystemOptions options = new FileSystemOptions();

        //TODO decide if we need to put this one whole method in DtsFileSystemOptions.
        // use Passive FTP
        FtpFileSystemConfigBuilder.getInstance().setPassiveMode(options, true);

        try {
            SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(options, "no");
        }
        catch (final FileSystemException ex) {
            LOG.warn("Unable to disable SFTP strict host key checking", ex);
        }


        CredentialType credentialType = sourceOrTarget.getCredential();
        if (credentialType != null) {

            // at the moment we're only supporting MyProxy credentials
            if (credentialType.getMyProxyToken() != null) {
                MyProxyTokenType myProxyDetails = credentialType.getMyProxyToken();

                MyProxy myproxy = new MyProxy(myProxyDetails.getMyProxyServer(),
                    myProxyDetails.getMyProxyPort());

                GSSCredential credential = null;
                try {
                    credential = myproxy.get(myProxyDetails.getMyProxyUsername(),
                        myProxyDetails.getMyProxyPassword(),
                        mDtsConfigManager.getDtsConfig().getInt(DEFAULT_MYPROXY_CREDENTIAL_LIFETIME_KEY));
                    GridFtpFileSystemConfigBuilder.getInstance().setGSSCredential(options, credential);

                    //TODO set the credential for all the other Grid file systems we want
                    //     to support in the future
                    //SRBFileSystemConfigBuilder.getInstance().setGSSCredential(options, credential);

                }
                catch (MyProxyException e) {
                    LOG.error(String.format("Could not get delegated proxy from server '%s:%s'\n%s",
                        myProxyDetails.getMyProxyServer(),
                        myProxyDetails.getMyProxyPort(),
                        e.getMessage()));
                    throw new DtsFileSystemAuthenticationException(e.getMessage());
                }
            }
            else if (credentialType.getUsernameToken() != null) {
                UsernameTokenType usernameTokenDetails = credentialType.getUsernameToken();
                String username = usernameTokenDetails.getUsername().getValue();
                String password = "";
                for (Object element : usernameTokenDetails.getAny()) {
                    // just in case there are other elements within a UsernameToken, ignore them
                    // unless it's a PasswordString
                    if (((Element)element).getLocalName().equals("PasswordString")) {
                        password = ((Element)element).getTextContent();
                    }
                }
                LOG.debug(String.format("using username '%s' and password '%s'.", username, password));
                StaticUserAuthenticator auth = new StaticUserAuthenticator(null, username, password);
                DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, auth);
            }


            //TODO support other types of credentials
        }

        // TODO set the other URI related options here like the min/max port numbers
        //      for GridFTP if provided

        return options;
    }
}
