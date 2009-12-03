/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.vfs;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelector;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileTypeSelector;
import org.apache.commons.vfs.NameScope;
import org.dataminx.dts.wn.batch.DtsFileTransferDetails;
import org.dataminx.dts.wn.batch.DtsFileTransferDetailsPlan;
import org.dataminx.dts.wn.batch.DtsJobExecutionException;
import org.dataminx.dts.wn.batch.DtsJobSplitterStrategy;
import org.dataminx.dts.wn.common.util.SchemaUtils;
import org.dataminx.dts.wn.service.DtsFileCopyOperationException;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxSourceTargetType;
import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Default implementation of {@link DtsJobSplitterStrategy}.
 *
 * @author Alex Arana
 */
@Scope("singleton")
@Component("dtsSingleFileSplitter")
public class DtsSingleFileSplitterStrategy implements DtsJobSplitterStrategy {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsSingleFileSplitterStrategy.class);

    /** A VFS file selector that includes *all* files. */
    private static final FileSelector FILES_ONLY_SELECTOR = new FileTypeSelector(FileType.FILE);

    /** A reference to the VFS file system manager. */
    @Autowired
    private DtsFileSystemManager mFileSystem;

    @Override
    public DtsFileTransferDetailsPlan splitJobRequest(final SubmitJobRequest submitJobRequest) {
        Assert.notNull(submitJobRequest, "DTS Job request must not be null");
        final List<DataTransferType> dataTransfers = SchemaUtils.getDataTransfers(submitJobRequest);
        if (CollectionUtils.isEmpty(dataTransfers)) {
            final String message = "DTS job request is incomplete as it does not contain any data transfer elements.";
            final DtsJobExecutionException ex = new DtsJobExecutionException(message);
            LOG.error(message, ex);
            throw ex;
        }

        final DtsFileTransferDetailsPlan result = new DtsFileTransferDetailsPlan();
        for (final DataTransferType dataTransfer : dataTransfers) {
            try {
                final List<DtsFileTransferDetails> fileTransfers = processDataTransfer(dataTransfer);
                for (final DtsFileTransferDetails fileTransfer : fileTransfers) {
                    result.put(fileTransfer.getSourceUri(), fileTransfer);
                }
            }
            catch (final FileSystemException ex) {
                throw new DtsJobScopingException(ex);
            }
        }
        return result;
    }

    /**
     * Processes a single data transfer element pertaining to a DTS job request.
     *
     * @param dataTransfer An instance of {@link DataTransferType}
     * @return A collection of data transfer detail object corresponding to the given data transfer
     * @throws FileSystemException if an error occurs
     */
    private List<DtsFileTransferDetails> processDataTransfer(
        final DataTransferType dataTransfer) throws FileSystemException {

        final MinxSourceTargetType sourceParameters = dataTransfer.getSource();
        final MinxSourceTargetType targetParameters = dataTransfer.getTarget();
        final FileSystemOptions sourceFileSystemOptions = mFileSystem.getFileSystemOptions(sourceParameters);
        final FileSystemOptions targetFileSystemOptions = mFileSystem.getFileSystemOptions(targetParameters);
        final FileObject sourceFile = mFileSystem.resolveFile(sourceParameters.getURI(), sourceFileSystemOptions);
        final FileObject targetFile = mFileSystem.resolveFile(targetParameters.getURI(), targetFileSystemOptions);
        final FileName sourceFilename = sourceFile.getName();
        final FileName targetFilename = targetFile.getName();

        final List<DtsFileTransferDetails> list = new ArrayList<DtsFileTransferDetails>();
        if (sourceFile.getType().equals(FileType.FOLDER)) {
            if (targetFile.getType().equals(FileType.FILE)) {
                // FOLDER => FILE
                // NOTE: this is currently not a supported scenario. single-file VFS
                //       providers such as ZIP, JAR, TAR etc are all read-only
                throw new DtsFileCopyOperationException(String.format("An attempt has been made to"
                    + " copy a directory into an existing file: '%s'", targetFile.getName().getFriendlyURI()));
            }

            // FOLDER => FOLDER
            for (final FileObject child : sourceFile.findFiles(FILES_ONLY_SELECTOR)) {
                final String relativePath = sourceFilename.getRelativeName(child.getName());
                final FileObject targetChild = targetFile.resolveFile(relativePath, NameScope.DESCENDENT_OR_SELF);
                final FileName targetChildName = targetChild.getName();
                final DtsFileTransferDetails fileTransfer =
                    new DtsFileTransferDetails(child.getName().getURI(), targetChildName.getURI());
                fileTransfer.setSourceParameters(sourceParameters);
                fileTransfer.setTargetParameters(targetParameters);
                fileTransfer.setTotalBytes(child.getContent().getSize());
                list.add(fileTransfer);
            }
        }
        else {
            // FILE => FOLDER
            final String targetUri;
            final String sourceUri = sourceFilename.getURI();
            if (targetFile.getType().equals(FileType.FOLDER)) {
                final FileName targetChildName =
                    mFileSystem.resolveName(targetFile.getName(), sourceFilename.getBaseName());
                targetUri = targetChildName.getURI();
            }
            // FILE => FILE
            else {
                targetUri = targetFilename.getURI();
            }

            final DtsFileTransferDetails fileTransfer = new DtsFileTransferDetails(sourceUri, targetUri);
            fileTransfer.setSourceParameters(sourceParameters);
            fileTransfer.setTargetParameters(targetParameters);
            fileTransfer.setTotalBytes(sourceFile.getContent().getSize());
            list.add(fileTransfer);
        }
        return list;
    }
}
