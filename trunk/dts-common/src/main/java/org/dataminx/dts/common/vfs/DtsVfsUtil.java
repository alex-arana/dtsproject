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

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.dataminx.dts.common.DtsConstants.WS_SECURITY_NAMESPACE_URI;
import static org.dataminx.dts.common.util.XmlBeansUtils.extractElementTextAsString;
import static org.dataminx.dts.common.util.XmlBeansUtils.selectAnyElement;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.auth.StaticUserAuthenticator;
import org.apache.commons.vfs.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.gridftp.cogjglobus.GridFtpFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.irods.IRODSFileSystemConfigBuilder;
import org.apache.commons.vfs.provider.storageresourcebroker.SRBFileSystemConfigBuilder;
import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.security.crypto.Encrypter;
import org.dataminx.dts.security.crypto.UnknownEncryptionAlgorithmException;
import org.dataminx.schemas.dts.x2009.x07.jsdl.CredentialType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.GridFtpURIPropertiesType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.IrodsURIPropertiesType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxSourceTargetType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MyProxyTokenType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.SrbURIPropertiesType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;
import org.globus.ftp.MarkerListener;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.oasisOpen.docs.wss.x2004.x01.oasis200401WssWssecuritySecext10.UsernameTokenType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.dl.escience.vfs.util.GridFTPUtil;
import uk.ac.dl.escience.vfs.util.VFSUtil;

/**
 * 
 * @author Gerson Galang
 */
public class DtsVfsUtil extends VFSUtil {

    /**
     * Qualified name of the XML element containing a password within a credentials element.
     */
    private static final QName PASSWORD_STRING_QNAME = new QName(
        WS_SECURITY_NAMESPACE_URI, "PasswordString");

    /** Internal logger object. */
    private static final Logger LOGGER = LoggerFactory
        .getLogger(DtsVfsUtil.class);

    private static final int FIFTY_KILOBYTES = 51200;

    private static final int FIVE_MEGABYTES = 5242880;

    private static final int SRB_QUERY_TIMEOUT = 10;

    private static final int ONE_HUNDRED = 100;

    // TODO: think of a better way of having this implemented rather than
    // copying the code from the original VfsUtil
    protected static void copyFileToFile(final FileObject fromFo,
        final FileObject toFo, final boolean append) throws IOException {
        final Log log = LogFactory.getLog(VFSUtil.class);
        if (!fromFo.exists()) {
            throw new IOException("path " + fromFo + " is not found");
        }
        if (!fromFo.getType().equals(FileType.FILE)) {
            throw new IOException("path " + fromFo + " is not a file");
        }

        InputStream xIS = null;
        OutputStream xOS = null;
        try {
            xIS = fromFo.getContent().getInputStream();
            xOS = toFo.getContent().getOutputStream(append);

            final long fileSize = fromFo.getContent().getSize();
            int bufferSize = (int) fileSize / ONE_HUNDRED;
            //minimum buf size of 50KiloBytes
            if (bufferSize < FIFTY_KILOBYTES) {
                bufferSize = FIFTY_KILOBYTES;
            }
            else if (bufferSize > FIVE_MEGABYTES) {
                bufferSize = FIVE_MEGABYTES;
            }

            final byte[] xBytes = new byte[bufferSize];
            int xByteRead = -1;
            int xTotalByteWrite = 0;
            while ((xByteRead = xIS.read(xBytes)) != -1) {
                xOS.write(xBytes, 0, xByteRead);
                xTotalByteWrite = xTotalByteWrite + xByteRead;
            }
            log.info("total byte write " + xTotalByteWrite);
            xOS.flush();
            xOS.flush();
        }
        catch (final Exception xEx) {
            log.error(xEx.getMessage(), xEx);
            throw new IOException(xEx.getMessage());
        }
        finally {
            try {
                fromFo.getContent().close();
            }
            catch (final Exception xEx) {
            }
            try {
                toFo.getContent().close();
            }
            catch (final Exception xEx) {
            }
            try {
                if (xIS != null) {
                    xIS.close();
                }
            }
            catch (final Exception ex) {
            }
            try {
                if (xOS != null) {
                    xOS.close();
                }
            }
            catch (final Exception ex) {
            }
        }
    }

    public static void fastCopy(final FileObject srcFo,
        final FileObject destFo, final MarkerListener listener,
        final boolean doThirdPartyTransferForTwoGridFtpFileObjects)
        throws IOException, FileSystemException {
        final Log log = LogFactory.getLog(VFSUtil.class);

        // todo: support append
        // check srcFo file exsits and is readable
        if (!srcFo.exists()) {
            throw new FileSystemException(
                "vfs.provider/copy-missing-file.error", srcFo);
        }
        if (!srcFo.isReadable()) {
            throw new FileSystemException(
                "vfs.provider/read-not-readable.error", srcFo);
        }

        // create the destination file or folder if it does not already exist.
        if (destFo.getType() == FileType.IMAGINARY || !destFo.exists()) {
            if (srcFo.getType().equals(FileType.FILE)) {
                destFo.createFile();
            }
            else if (srcFo.getType().equals(FileType.FOLDER)) {
                destFo.createFolder();
            }
        }

        // check can write to the target
        if (!destFo.isWriteable()) {
            throw new FileSystemException("vfs.provider/copy-read-only.error",
                destFo);
        }

        // check src and target FileObjects are not the same file
        if (destFo.getName().getURI().equals(srcFo.getName().getURI())) {
            throw new FileSystemException("vfs.provider/copy-file.error",
                new Object[] {srcFo, destFo}, null);
        }

        // Do transfer
        // If two gsiftp uris and electing to do third party transfer
        if (doThirdPartyTransferForTwoGridFtpFileObjects
            && srcFo.getName().getScheme().equalsIgnoreCase("gsiftp")
            && destFo.getName().getScheme().equalsIgnoreCase("gsiftp")) {
            try {
                final GridFTPUtil util = setupGridFtpThridPartyTransfer(srcFo,
                    destFo, listener);
                doGridFtpThridPartyTransfer(util, srcFo, destFo, false);
            }
            catch (final Exception ex) {
                ex.printStackTrace();
                throw new IOException("Error on gridftp third party transfer");
            }
            return;
        }

        // Do transfer
        // If copying between two different file systems
        if (srcFo.getType().equals(FileType.FILE)) {
            if (destFo.getType().equals(FileType.FOLDER)) {
                log.debug("vfs FILE into FOLDER");
                // get a handle on the new file to create at the destination.
                final FileObject nestedDestFo = destFo.resolveFile(srcFo
                    .getName().getBaseName());
                // copyFileToFile(srcFo, nestedDestFo, false); //append false here
                nestedDestFo.copyFrom(srcFo, new AllFileSelector());

            }
            else {
                log.debug("vfs FILE to FILE");
                // copyFileToFile(srcFo, destFo, false); //append false here
                destFo.copyFrom(srcFo, new AllFileSelector());

            }
        }
        else if (srcFo.getType().equals(FileType.FOLDER)) {
            // copying the children of a folder into another folder
            if (destFo.getType().equals(FileType.FOLDER)) {
                log.debug("vfs FOLDER children into FOLDER");
                destFo.copyFrom(srcFo, new AllFileSelector());

            }
            else {
                throw new IOException(
                    "Cannot copy a folder to a destination that is not a folder");
            }
        }
        else {
            throw new IOException("Cannot copy from path of type "
                + srcFo.getType() + " to another path of type "
                + destFo.getType());
        }
    }

    private boolean mFtpSupported = true;

    private boolean mSftpSupported = true;

    private boolean mHttpSupported = true;

    private boolean mGsiftpSupported = true;

    private boolean mSrbSupported = true;

    private boolean mFileSupported = true;

    private boolean mIrodsSupported = true;

    private String mTmpDirPath = null;

    /**
     * Specifies the lifetime of the MyProxy credentials managed by this class. If this setting is not specifically
     * configured, it will hold a default value of <code>0</code> which means use the maximum possible lifetime for the
     * credential.
     */
    private int mMyProxyCredentialLifetime;

    /**
     * Create a new set of file system options, as an instance of {@link FileSystemOptions}, based on the provided
     * source or target entity.
     * 
     * @param sourceOrTarget
     *            the source or target entity
     * @param encrypter
     *            the encrypter that will decrypt the password
     * @return the set of file system options for the given source or target entity
     * @throws FileSystemException
     *             when an error occurs during a VFS file copy operation.
     */
    public FileSystemOptions createFileSystemOptions(
        final SourceTargetType sourceOrTarget, final Encrypter encrypter)
        throws FileSystemException {
        final FileSystemOptions options = new DtsFileSystemOptions();

        if (sourceOrTarget instanceof MinxSourceTargetType) {
            final MinxSourceTargetType minxSourceOrTarget = (MinxSourceTargetType) sourceOrTarget;
            final CredentialType credentialType = minxSourceOrTarget
                .getCredential();
            final XmlObject uriPropertiesXML = minxSourceOrTarget
                .getURIProperties();
            if (credentialType != null) {
                // at the moment we're only supporting MyProxy credentials
                if (credentialType.getMyProxyToken() != null) {
                    final MyProxyTokenType myProxyDetails = credentialType
                        .getMyProxyToken();
                    final MyProxy myproxy = new MyProxy(myProxyDetails
                        .getMyProxyServer(), myProxyDetails.getMyProxyPort());

                    GSSCredential credential = null;
                    try {
                        credential = myproxy.get(myProxyDetails
                            .getMyProxyUsername(), encrypter
                            .decrypt(myProxyDetails.getMyProxyPassword()),
                            mMyProxyCredentialLifetime);
                        GridFtpFileSystemConfigBuilder.getInstance()
                            .setGSSCredential(options, credential);
                        SRBFileSystemConfigBuilder.getInstance()
                            .setGSSCredential(options, credential);
                        IRODSFileSystemConfigBuilder.getInstance()
                            .setGSSCredential(options, credential);
                    }
                    catch (final MyProxyException ex) {
                        LOGGER
                            .error(String
                                .format(
                                    "Could not get delegated proxy from server '%s:%s'\n%s",
                                    myProxyDetails.getMyProxyServer(),
                                    myProxyDetails.getMyProxyPort(), ex
                                        .getMessage()));
                        throw new FileSystemAuthenticationException(ex
                            .getMessage());
                    }
                    catch (final UnknownEncryptionAlgorithmException ex) {
                        LOGGER.error("Password could not be decrypted", ex);
                        throw new FileSystemAuthenticationException(ex
                            .getMessage());
                    }
                }
                else if (credentialType.getUsernameToken() != null) {
                    final UsernameTokenType usernameTokenDetails = credentialType
                        .getUsernameToken();
                    final String username = usernameTokenDetails.getUsername()
                        .getStringValue();
                    final XmlObject element = selectAnyElement(
                        usernameTokenDetails, PASSWORD_STRING_QNAME);
                    final String password = element == null ? EMPTY
                        : extractElementTextAsString(element);
                    final StaticUserAuthenticator auth = new StaticUserAuthenticator(
                        null, username, password);
                    DefaultFileSystemConfigBuilder.getInstance()
                        .setUserAuthenticator(options, auth);
                    SRBFileSystemConfigBuilder.getInstance()
                        .setUserAuthenticator(options, auth);
                    IRODSFileSystemConfigBuilder.getInstance()
                        .setUserAuthenticator(options, auth);
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
                    final String defaultStorageResource = srbUriProperties
                        .getDefaultResource();
                    //int firewallPortMax = srbUriProperties.
                    //int firewallPortMin = srbUriProperties.
                    final String homeDirectory = srbUriProperties
                        .getMdasCollectionHome();
                    final String mcatZone = srbUriProperties.getMcatZone();
                    final String mdasDomainName = srbUriProperties
                        .getMdasDomainHome();

                    if (defaultStorageResource != null
                        && !defaultStorageResource.trim().equals("")) {
                        LOGGER.debug("Setting SRB.defaultStorageResource to "
                            + defaultStorageResource);
                        SRBFileSystemConfigBuilder.getInstance()
                            .setDefaultStorageResource(options,
                                defaultStorageResource);
                    }

                    //SRBFileSystemConfigBuilder.getInstance().setFileWallPortMax(options, max);
                    //SRBFileSystemConfigBuilder.getInstance().setFileWallPortMin(options, min);

                    if (homeDirectory != null
                        && !homeDirectory.trim().equals("")) {
                        LOGGER.debug("Setting SRB.homeDirectory to "
                            + homeDirectory);
                        SRBFileSystemConfigBuilder.getInstance()
                            .setHomeDirectory(options, homeDirectory);
                    }

                    if (mcatZone != null && !mcatZone.trim().equals("")) {
                        LOGGER.debug("Setting SRB.mcatZone to " + mcatZone);
                        SRBFileSystemConfigBuilder.getInstance().setMcatZone(
                            options, mcatZone);
                    }

                    if (mdasDomainName != null
                        && !mdasDomainName.trim().equals("")) {
                        LOGGER.debug("Setting SRB.mdasDomainName to "
                            + mdasDomainName);
                        SRBFileSystemConfigBuilder.getInstance()
                            .setMdasDomainName(options, mdasDomainName);
                    }

                    SRBFileSystemConfigBuilder.getInstance().setQueryTimeout(
                        options, SRB_QUERY_TIMEOUT);

                }
                else if (uriPropertiesXML instanceof IrodsURIPropertiesType) {
                    final IrodsURIPropertiesType irodsUriProperties = (IrodsURIPropertiesType) uriPropertiesXML;
                    final String defaultStorageResource = irodsUriProperties
                        .getIrodsDefaultResource();
                    final String homeDirectory = irodsUriProperties
                        .getIrodsHome();
                    final String zone = irodsUriProperties.getIrodsZone();

                    if (defaultStorageResource != null
                        && !defaultStorageResource.trim().equals("")) {
                        LOGGER.debug("Setting IRODS.defaultStorageResource to "
                            + defaultStorageResource);
                        IRODSFileSystemConfigBuilder.getInstance()
                            .setDefaultStorageResource(options,
                                defaultStorageResource);
                    }
                    if (homeDirectory != null
                        && !homeDirectory.trim().equals("")) {
                        LOGGER.debug("Setting IRODS.homeDirectory to "
                            + homeDirectory);
                        IRODSFileSystemConfigBuilder.getInstance()
                            .setHomeDirectory(options, homeDirectory);
                    }
                    if (zone != null && !zone.trim().equals("")) {
                        LOGGER.debug("Setting IRODS.zone to " + zone);
                        IRODSFileSystemConfigBuilder.getInstance().setZone(
                            options, zone);
                    }

                }

            }
        }

        // TODO set the other URI related options here like the min/max port numbers
        //      for GridFTP if provided

        return options;
    }

    public DefaultFileSystemManager createNewFsManager()
        throws FileSystemException {
        return VFSUtil.createNewFsManager(mFtpSupported, mSftpSupported,
            mHttpSupported, mGsiftpSupported, mSrbSupported, mFileSupported,
            mIrodsSupported, mTmpDirPath);
    }

    public int getMyProxyCredentialLifetime() {
        return mMyProxyCredentialLifetime;
    }

    public String getTmpDirPath() {
        return mTmpDirPath;
    }

    public boolean isFileSupported() {
        return mFileSupported;
    }

    public boolean isFtpSupported() {
        return mFtpSupported;
    }

    public boolean isGsiftpSupported() {
        return mGsiftpSupported;
    }

    public boolean isHttpSupported() {
        return mHttpSupported;
    }

    public boolean isIrodsSupported() {
        return mIrodsSupported;
    }

    public boolean isSftpSupported() {
        return mSftpSupported;
    }

    public boolean isSrbSupported() {
        return mSrbSupported;
    }

    public void setFileSupported(final boolean fileSupported) {
        mFileSupported = fileSupported;
    }

    public void setFtpSupported(final boolean ftpSupported) {
        mFtpSupported = ftpSupported;
    }

    public void setGsiftpSupported(final boolean gsiftpSupported) {
        mGsiftpSupported = gsiftpSupported;
    }

    public void setHttpSupported(final boolean httpSupported) {
        mHttpSupported = httpSupported;
    }

    public void setIrodsSupported(final boolean irodsSupported) {
        mIrodsSupported = irodsSupported;
    }

    public void setMyProxyCredentialLifetime(final int myProxyCredentialLifetime) {
        mMyProxyCredentialLifetime = myProxyCredentialLifetime;
    }

    public void setSftpSupported(final boolean sftpSupported) {
        mSftpSupported = sftpSupported;
    }

    public void setSrbSupported(final boolean srbSupported) {
        mSrbSupported = srbSupported;
    }

    public void setTmpDirPath(final String tmpDirPath) {
        mTmpDirPath = tmpDirPath;
    }

}
