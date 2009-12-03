/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.batch;

import org.dataminx.schemas.dts.x2009.x07.messages.SubmitJobRequestDocument.SubmitJobRequest;

/**
 * Strategy for breaking down a DTS Job request into atomic units of work.
 *
 * @author Alex Arana
 */
public interface DtsJobSplitterStrategy {

    /**
     * Parses a given DTS schema entity corresponding to a file transfer job request into an instance of
     * {@link DtsFileTransferDetailsPlan} that describes the entire operation including all its data
     * transfer elements.
     *
     * @param submitJobRequest DTS schema entity containing a file transfer job request
     * @return An instance of <code>DtsFileTransferDetailsPlan</code> detailing the entire operation
     */
    DtsFileTransferDetailsPlan splitJobRequest(SubmitJobRequest submitJobRequest);
}
