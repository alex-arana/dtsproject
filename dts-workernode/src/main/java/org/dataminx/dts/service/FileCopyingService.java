/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

/**
 * Describes the File Copying service behaviour.
 *
 * @author Alex Arana
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
}
