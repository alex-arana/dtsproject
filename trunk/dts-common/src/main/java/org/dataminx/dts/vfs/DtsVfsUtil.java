package org.dataminx.dts.vfs;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.dataminx.dts.common.DtsConstants.WS_SECURITY_NAMESPACE_URI;
import static org.dataminx.dts.wn.common.util.XmlBeansUtils.extractElementTextAsString;
import static org.dataminx.dts.wn.common.util.XmlBeansUtils.selectAnyElement;

import javax.xml.namespace.QName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.gridftp.cogjglobus.GridFtpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.irods.IRODSFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.storageresourcebroker.SRBFileSystemConfigBuilder;
import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.wn.service.DtsFileSystemAuthenticationException;
import org.dataminx.schemas.dts.x2009.x07.jsdl.CredentialType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.GridFtpURIPropertiesType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.IrodsURIPropertiesType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxSourceTargetType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MyProxyTokenType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.SrbURIPropertiesType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.oasisOpen.docs.wss.x2004.x01.oasis200401WssWssecuritySecext10.UsernameTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.dl.escience.vfs.util.VFSUtil;

/**
 * 
 * @author Gerson Galang
 */
public class DtsVfsUtil extends VFSUtil {

    /**
     * Qualified name of the XML element containing a password within a
     * credentials element.
     */
    private static final QName PASSWORD_STRING_QNAME = new QName(WS_SECURITY_NAMESPACE_URI, "PasswordString");

    /** Internal logger object. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DtsVfsUtil.class);

    private boolean mFtpSupported = true;

    private boolean mSftpSupported = true;

    private boolean mHttpSupported = true;

    private boolean mGsiftpSupported = true;

    private boolean mSrbSupported = true;

    private boolean mFileSupported = true;

    private boolean mIrodsSupported = true;

    private String mTmpDirPath = null;

    /**
     * Specifies the lifetime of the MyProxy credentials managed by this class.
     * If this setting is not specifically configured, it will hold a default
     * value of <code>0</code> which means use the maximum possible lifetime for
     * the credential.
     */
    private int mMyProxyCredentialLifetime;

    public boolean isFtpSupported() {
        return mFtpSupported;
    }

    public void setFtpSupported(final boolean ftpSupported) {
        mFtpSupported = ftpSupported;
    }

    public boolean isSftpSupported() {
        return mSftpSupported;
    }

    public void setSftpSupported(final boolean sftpSupported) {
        mSftpSupported = sftpSupported;
    }

    public boolean isHttpSupported() {
        return mHttpSupported;
    }

    public void setHttpSupported(final boolean httpSupported) {
        mHttpSupported = httpSupported;
    }

    public boolean isGsiftpSupported() {
        return mGsiftpSupported;
    }

    public void setGsiftpSupported(final boolean gsiftpSupported) {
        mGsiftpSupported = gsiftpSupported;
    }

    public boolean isSrbSupported() {
        return mSrbSupported;
    }

    public void setSrbSupported(final boolean srbSupported) {
        mSrbSupported = srbSupported;
    }

    public boolean isFileSupported() {
        return mFileSupported;
    }

    public void setFileSupported(final boolean fileSupported) {
        mFileSupported = fileSupported;
    }

    public boolean isIrodsSupported() {
        return mIrodsSupported;
    }

    public void setIrodsSupported(final boolean irodsSupported) {
        mIrodsSupported = irodsSupported;
    }

    public String getTmpDirPath() {
        return mTmpDirPath;
    }

    public void setTmpDirPath(final String tmpDirPath) {
        mTmpDirPath = tmpDirPath;
    }

    public DefaultFileSystemManager createNewFsManager() throws FileSystemException {
        return VFSUtil.createNewFsManager(mFtpSupported, mSftpSupported, mHttpSupported, mGsiftpSupported,
                mSrbSupported, mFileSupported, mIrodsSupported, mTmpDirPath);
    }

    /**
     * Create a new set of file system options, as an instance of
     * {@link FileSystemOptions}, based on the provided source or target entity.
     * 
     * @param sourceOrTarget the source or target entity
     * @return the set of file system options for the given source or target
     *         entity
     * @throws FileSystemException when an error occurs during a VFS file copy
     *         operation.
     */
    public FileSystemOptions createFileSystemOptions(final SourceTargetType sourceOrTarget) throws FileSystemException {
        final FileSystemOptions options = new DtsFileSystemOptions();

        if (sourceOrTarget instanceof MinxSourceTargetType) {
            final MinxSourceTargetType minxSourceOrTarget = (MinxSourceTargetType) sourceOrTarget;
            final CredentialType credentialType = minxSourceOrTarget.getCredential();
            final XmlObject uriPropertiesXML = minxSourceOrTarget.getURIProperties();
            if (credentialType != null) {
                // at the moment we're only supporting MyProxy credentials
                if (credentialType.getMyProxyToken() != null) {
                    final MyProxyTokenType myProxyDetails = credentialType.getMyProxyToken();
                    final MyProxy myproxy = new MyProxy(myProxyDetails.getMyProxyServer(), myProxyDetails
                            .getMyProxyPort());

                    GSSCredential credential = null;
                    try {
                        credential = myproxy.get(myProxyDetails.getMyProxyUsername(), myProxyDetails
                                .getMyProxyPassword(), mMyProxyCredentialLifetime);
                        GridFtpFileSystemConfigBuilder.getInstance().setGSSCredential(options, credential);
                        SRBFileSystemConfigBuilder.getInstance().setGSSCredential(options, credential);
                        IRODSFileSystemConfigBuilder.getInstance().setGSSCredential(options, credential);
                    } catch (final MyProxyException ex) {
                        LOGGER.error(String.format("Could not get delegated proxy from server '%s:%s'\n%s",
                                myProxyDetails.getMyProxyServer(), myProxyDetails.getMyProxyPort(), ex.getMessage()));
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
                    SRBFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, auth);
                    IRODSFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, auth);
                }

                //TODO support other types of credentials
            }
            if (uriPropertiesXML != null) {
                if (uriPropertiesXML instanceof GridFtpURIPropertiesType) {
                    final GridFtpURIPropertiesType gridFtpUriProperties = (GridFtpURIPropertiesType) uriPropertiesXML;

                    // TODO: need to check with the Commons VFS Grid guys if there's a way of setting
                    // other GridFTP URI related properties through GridFtpFileSystemConfigBuilder
                }
                else if (uriPropertiesXML instanceof SrbURIPropertiesType) {
                    final SrbURIPropertiesType srbUriProperties = (SrbURIPropertiesType) uriPropertiesXML;
                    final String defaultStorageResource = srbUriProperties.getDefaultResource();
                    //int firewallPortMax = srbUriProperties.
                    //int firewallPortMin = srbUriProperties.
                    final String homeDirectory = srbUriProperties.getMdasCollectionHome();
                    final String mcatZone = srbUriProperties.getMcatZone();
                    final String mdasDomainName = srbUriProperties.getMdasDomainHome();

                    if (defaultStorageResource != null && !defaultStorageResource.trim().equals("")) {
                        LOGGER.debug("Setting SRB.defaultStorageResource to " + defaultStorageResource);
                        SRBFileSystemConfigBuilder.getInstance().setDefaultStorageResource(options,
                                defaultStorageResource);
                    }

                    //SRBFileSystemConfigBuilder.getInstance().setFileWallPortMax(options, max);
                    //SRBFileSystemConfigBuilder.getInstance().setFileWallPortMin(options, min);

                    if (homeDirectory != null && !homeDirectory.trim().equals("")) {
                        LOGGER.debug("Setting SRB.homeDirectory to " + homeDirectory);
                        SRBFileSystemConfigBuilder.getInstance().setHomeDirectory(options, homeDirectory);
                    }

                    if (mcatZone != null && !mcatZone.trim().equals("")) {
                        LOGGER.debug("Setting SRB.mcatZone to " + mcatZone);
                        SRBFileSystemConfigBuilder.getInstance().setMcatZone(options, mcatZone);
                    }

                    if (mdasDomainName != null && !mdasDomainName.trim().equals("")) {
                        LOGGER.debug("Setting SRB.mdasDomainName to " + mdasDomainName);
                        SRBFileSystemConfigBuilder.getInstance().setMdasDomainName(options, mdasDomainName);
                    }

                    SRBFileSystemConfigBuilder.getInstance().setQueryTimeout(options, 10);

                }
                else if (uriPropertiesXML instanceof IrodsURIPropertiesType) {
                    final IrodsURIPropertiesType irodsUriProperties = (IrodsURIPropertiesType) uriPropertiesXML;
                    final String defaultStorageResource = irodsUriProperties.getIrodsDefaultResource();
                    final String homeDirectory = irodsUriProperties.getIrodsHome();
                    final String zone = irodsUriProperties.getIrodsZone();

                    if (defaultStorageResource != null && !defaultStorageResource.trim().equals("")) {
                        LOGGER.debug("Setting IRODS.defaultStorageResource to " + defaultStorageResource);
                        IRODSFileSystemConfigBuilder.getInstance().setDefaultStorageResource(options,
                                defaultStorageResource);
                    }
                    if (homeDirectory != null && !homeDirectory.trim().equals("")) {
                        LOGGER.debug("Setting IRODS.homeDirectory to " + homeDirectory);
                        IRODSFileSystemConfigBuilder.getInstance().setHomeDirectory(options, homeDirectory);
                    }
                    if (zone != null && !zone.trim().equals("")) {
                        LOGGER.debug("Setting IRODS.zone to " + zone);
                        IRODSFileSystemConfigBuilder.getInstance().setZone(options, zone);
                    }

                }

            }
        }

        // TODO set the other URI related options here like the min/max port numbers
        //      for GridFTP if provided

        return options;
    }

    public int getMyProxyCredentialLifetime() {
        return mMyProxyCredentialLifetime;
    }

    public void setMyProxyCredentialLifetime(final int myProxyCredentialLifetime) {
        mMyProxyCredentialLifetime = myProxyCredentialLifetime;
    }

}
