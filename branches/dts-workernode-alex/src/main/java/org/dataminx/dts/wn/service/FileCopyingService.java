/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.service;

import org.dataminx.dts.wn.batch.DtsFileTransferDetails;

/**
 * Describes the File Copying service behaviour.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
public interface FileCopyingService {

    /**
     * Performs a file transfer operation given the input details object which contains all the information
     * required to carry out the operation including the source and target URIs.
     *
     * @param fileTransferDetails an instance of <code>DtsFileTransferDetails</code> containing all required
     *        inputs to the file copy operation
     */
    void copyFiles(DtsFileTransferDetails fileTransferDetails);
}
