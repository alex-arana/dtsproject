/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

import org.dataminx.schemas.dts._2009._05.dts.SourceTargetType;

/**
 * Describes the File Copying service behaviour.
 *
 * @author Alex Arana
 * @author Gerson Galang
 */
public interface FileCopyingService {

    /**
     * Copies the content from a source file to a destination file.
     * TODO: Implement this method using Apache commons-vfs
     *
     * @param sourceURI Source URI string
     * @param targetURI Target URI string
     */
    void copyFiles(String sourceURI, String targetURI);

    /**
     * Copies the source to the destination. This method provides more flexibility
     * in cases where Source and Target URIs come with their own URI Properties.
     *
     * @param source the source
     * @param target the target
     */
    void copyFiles(SourceTargetType source, SourceTargetType target);

}
