/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.vfs;

import static org.dataminx.dts.common.DtsConstants.DEFAULT_MYPROXY_CREDENTIAL_LIFETIME_KEY;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.commons.vfs.provider.gridftp.cogjglobus.GridFtpFileSystemConfigBuilder;
import org.dataminx.dts.DtsException;
import org.dataminx.dts.common.DtsConfigManager;
import org.dataminx.dts.service.DtsFileSystemAuthenticationException;
import org.dataminx.schemas.dts._2009._05.dts.CredentialType;
import org.dataminx.schemas.dts._2009._05.dts.MyProxyTokenType;
import org.dataminx.schemas.dts._2009._05.dts.SourceTargetType;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.UsernameTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.w3c.dom.Element;

/**
 * DTS File System Manager implementation.
 *
 * @author Alex Arana
 */
@Component
@Scope("singleton")
public class DtsFileSystemManager extends StandardFileSystemManager implements InitializingBean {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsFileSystemManager.class);

    /** Configuration URL. */
    private Resource mConfigurationResource;

    /** A reference to the DTS Configuration manager. */
    @Autowired
    private DtsConfigManager mDtsConfigManager;

    /** Default set of options for most file systems. */
    private FileSystemOptions mDefaultFileSystemOptions = new DtsFileSystemOptions();

    /**
     * Locates a file by URI using the default FileSystemOptions for file-system creation.
     *
     * @param uri File unique resource identifier string
     * @return A new file object instance
     */
    public FileObject resolveFile(final String uri) {
        try {
            return super.resolveFile(uri, mDefaultFileSystemOptions);
        }
        catch (final FileSystemException ex) {
            throw new DtsException(ex);
        }
    }

    /**
     * Locates a file by URI using the default FileSystemOptions for file-system creation.
     *
     * @param sourceOrTarget JAXB entity containing details about a file transfer source/destination
     * @return A new file object instance
     */
    public FileObject resolveFile(final SourceTargetType sourceOrTarget) {
        Assert.notNull(sourceOrTarget);
        try {
            return resolveFile(sourceOrTarget.getURI(), createFileSystemOptions(sourceOrTarget));
        }
        catch (final FileSystemException ex) {
            throw new DtsException(ex);
        }
    }

    /**
     * Create a new set of file system options, as an instance of {@link FileSystemOptions}, based on the provided
     * source or target entity.
     *
     * @param sourceOrTarget the source or target entity
     * @return the set of file system options for the given source or target entity
     * @throws FileSystemException when an error occurs during a VFS file copy operation.
     */
    public FileSystemOptions createFileSystemOptions(final SourceTargetType sourceOrTarget) throws FileSystemException {
        final FileSystemOptions options = new DtsFileSystemOptions();
        final CredentialType credentialType = sourceOrTarget.getCredential();
        if (credentialType != null) {
            // at the moment we're only supporting MyProxy credentials
            if (credentialType.getMyProxyToken() != null) {
                final MyProxyTokenType myProxyDetails = credentialType.getMyProxyToken();
                final MyProxy myproxy = new MyProxy(
                    myProxyDetails.getMyProxyServer(), myProxyDetails.getMyProxyPort());

                GSSCredential credential = null;
                try {
                    credential = myproxy.get(myProxyDetails.getMyProxyUsername(),
                        myProxyDetails.getMyProxyPassword(),
                        //TODO replace with property injection?
                        mDtsConfigManager.getDtsConfig().getInt(DEFAULT_MYPROXY_CREDENTIAL_LIFETIME_KEY, 0));
                    GridFtpFileSystemConfigBuilder.getInstance().setGSSCredential(options, credential);

                    //TODO set the credential for all the other Grid file systems we want
                    //     to support in the future
                    //SRBFileSystemConfigBuilder.getInstance().setGSSCredential(options, credential);
                }
                catch (final MyProxyException ex) {
                    LOG.error(String.format("Could not get delegated proxy from server '%s:%s'\n%s",
                        myProxyDetails.getMyProxyServer(),
                        myProxyDetails.getMyProxyPort(),
                        ex.getMessage()));
                    throw new DtsFileSystemAuthenticationException(ex.getMessage());
                }
            }

            else if (credentialType.getUsernameToken() != null) {
                UsernameTokenType usernameTokenDetails = credentialType.getUsernameToken();
                String username = usernameTokenDetails.getUsername().getValue();
                String password = "";
                for (Object element : usernameTokenDetails.getAny()) {
                    // just in case there are other elements within a UsernameToken, ignore them
                    // unless it's a PasswordString
                    if (((Element) element).getLocalName().equals("PasswordString")) {
                        password = ((Element) element).getTextContent();
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

    public void setConfigurationResource(final Resource resource) {
        mConfigurationResource = resource;
    }

    public FileSystemOptions getDefaultFileSystemOptions() {
        return mDefaultFileSystemOptions;
    }

    public void setDefaultFileSystemOptions(final FileSystemOptions fileSystemOptions) {
        mDefaultFileSystemOptions = fileSystemOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(mConfigurationResource, "VFS file system providers have not yet been configured.");
        setConfiguration(mConfigurationResource.getURL());
    }
}
