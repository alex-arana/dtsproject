/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.vfs;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.dataminx.dts.common.DtsConstants.WS_SECURITY_NAMESPACE_URI;
import static org.dataminx.dts.wn.common.util.XmlBeansUtils.extractElementTextAsString;
import static org.dataminx.dts.wn.common.util.XmlBeansUtils.selectAnyElement;

import java.io.File;
import javax.xml.namespace.QName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.commons.vfs.provider.gridftp.cogjglobus.GridFtpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.temp.TemporaryFileProvider;
import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.DtsException;
import org.dataminx.dts.wn.service.DtsFileSystemAuthenticationException;
import org.dataminx.schemas.dts.x2009.x07.jsdl.CredentialType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxSourceTargetType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MyProxyTokenType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.oasisOpen.docs.wss.x2004.x01.oasis200401WssWssecuritySecext10.UsernameTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * DTS File System Manager implementation.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
@Component
@Scope("singleton")
public class DtsFileSystemManager extends StandardFileSystemManager implements InitializingBean {
    /** Qualified name of the XML element containing a password within a credentials element. */
    private static final QName PASSWORD_STRING_QNAME =
        new QName(WS_SECURITY_NAMESPACE_URI, "PasswordString");

    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsFileSystemManager.class);

    /** Configuration URL. */
    private Resource mConfigurationResource;

    /**
     * Specifies the lifetime of the MyProxy credentials managed by this class.  If this setting is not
     * specifically configured, it will hold a default value of <code>0</code> which means use the maximum
     * possible lifetime for the credential.
     */
    private int mMyProxyCredentialLifetime;

    /** Default set of options for most file systems. */
    private FileSystemOptions mDefaultFileSystemOptions = new DtsFileSystemOptions();

    /**
     * {@inheritDoc}
     */
    public void init() throws FileSystemException {
        super.init();

        // let's specify our own tmp directory to be used by our own tmp scheme if providers.xml doesn't
        // list tmp as a scheme to be supported
        if (!hasProvider("tmp")) {
            // let's add tmp as a protocol to be supported as well
            File xFile = new File(System.getProperty("java.io.tmpdir", "/tmp"));
            if (!xFile.exists()) {
                throw new IllegalStateException(
                        "cannot allocate temporary directory. Please set java.io.tmpdir system property");
            }
            addProvider("tmp", new TemporaryFileProvider(xFile));
            setTemporaryFileStore(new DefaultFileReplicator(xFile));
        }
    }

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

        if (sourceOrTarget instanceof MinxSourceTargetType) {
            final MinxSourceTargetType minxSourceOrTarget = (MinxSourceTargetType) sourceOrTarget;
            final CredentialType credentialType = minxSourceOrTarget.getCredential();
            if (credentialType != null) {
                // at the moment we're only supporting MyProxy credentials
                if (credentialType.getMyProxyToken() != null) {
                    final MyProxyTokenType myProxyDetails = credentialType.getMyProxyToken();
                    final MyProxy myproxy = new MyProxy(
                        myProxyDetails.getMyProxyServer(), myProxyDetails.getMyProxyPort());

                    GSSCredential credential = null;
                    try {
                        credential = myproxy.get(myProxyDetails.getMyProxyUsername(),
                            myProxyDetails.getMyProxyPassword(), mMyProxyCredentialLifetime);
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
                    final UsernameTokenType usernameTokenDetails = credentialType.getUsernameToken();
                    final String username = usernameTokenDetails.getUsername().getStringValue();
                    final XmlObject element = selectAnyElement(usernameTokenDetails, PASSWORD_STRING_QNAME);
                    final String password = element == null ? EMPTY : extractElementTextAsString(element);
                    final StaticUserAuthenticator auth = new StaticUserAuthenticator(null, username, password);
                    DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, auth);
                }

                //TODO support other types of credentials
            }
        }

        // TODO set the other URI related options here like the min/max port numbers
        //      for GridFTP if provided

        return options;
    }

    public void setConfigurationResource(final Resource resource) {
        mConfigurationResource = resource;
    }

    public int getMyProxyCredentialLifetime() {
        return mMyProxyCredentialLifetime;
    }

    public void setMyProxyCredentialLifetime(final int myProxyCredentialLifetime) {
        mMyProxyCredentialLifetime = myProxyCredentialLifetime;
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
