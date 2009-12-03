/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.vfs;

import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.dataminx.dts.wn.batch.DtsFileTransferDetails;
import org.dataminx.dts.wn.batch.DtsFileTransferDetailsPlan;
import org.dataminx.dts.wn.batch.DtsJobExecutionException;
import org.dataminx.dts.wn.batch.DtsJobSplitterStrategy;
import org.dataminx.dts.wn.common.util.SchemaUtils;
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
 * An implementation of {@link DtsJobSplitterStrategy} that breaks down a {@link SubmitJobRequest} into
 * a collection of execution steps, one for each {@link DataTransferType} schema entity present in the
 * input.
 * <p>
 * <em>NOTE</em>: This is provided purely to provide support for the current behaviour and should be
 *                removed later on.
 *
 * @author Alex Arana
 */
@Scope("singleton")
@Component("dtsDataTransferSplitter")
public class DtsDataTransferSplitterStrategy implements DtsJobSplitterStrategy {
    /** Internal logger object. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsDataTransferSplitterStrategy.class);

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

        final DtsFileTransferDetailsPlan plan = new DtsFileTransferDetailsPlan();
        try {
            for (final DataTransferType dataTransfer : dataTransfers) {
                final DtsFileTransferDetails fileTransferDetails = createFileTransferDetails(dataTransfer);
                plan.put(fileTransferDetails.getSourceUri(), fileTransferDetails);
            }
        }
        catch (final FileSystemException ex) {
            throw new DtsJobScopingException(ex);
        }
        return plan;
    }

    /**
     * Creates a new instance of {@link DtsFileTransferDetails} containing information corresponding to the
     * given data transfer schema entity.
     *
     * @param dataTransfer A DTS schema entity containing details pertaining to a discreet data transfer
     *        element
     * @return A new instance of <code>DtsFileTransferDetails</code>
     * @throws FileSystemException if an error occurs while accessing the source file system
     */
    private DtsFileTransferDetails createFileTransferDetails(
        final DataTransferType dataTransfer) throws FileSystemException {

        final MinxSourceTargetType sourceParameters = dataTransfer.getSource();
        final MinxSourceTargetType targetParameters = dataTransfer.getTarget();
        final String sourceUri = sourceParameters.getURI();
        final DtsFileTransferDetails fileTransferDetails =
            new DtsFileTransferDetails(sourceUri, targetParameters.getURI());
        fileTransferDetails.setSourceParameters(sourceParameters);
        fileTransferDetails.setTargetParameters(targetParameters);
        final FileObject sourceFile = mFileSystem.resolveFile(sourceUri, sourceParameters);
        if (sourceFile.getType().equals(FileType.FILE)) {
            fileTransferDetails.setTotalBytes(sourceFile.getContent().getSize());
        }
        return fileTransferDetails;
    }
}
