/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.service;

import org.dataminx.dts.DtsException;

/**
 * Represents an error that occurs while executing a file copy operation.
 *
 * @author Alex Arana
 */
public class DtsFileCopyOperationException extends DtsException {
    /**
     * Constructs an instance of {@link DtsFileCopyOperationException}.
     */
    public DtsFileCopyOperationException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsFileCopyOperationException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsFileCopyOperationException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsFileCopyOperationException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsFileCopyOperationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of {@link DtsFileCopyOperationException} with the specified cause.
     *
     * @param cause the cause.
     */
    public DtsFileCopyOperationException(final Throwable cause) {
        super(cause);
    }
}
