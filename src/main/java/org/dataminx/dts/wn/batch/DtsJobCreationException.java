/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.batch;

import org.dataminx.dts.DtsException;

/**
 * Represents an error that occurs when failing to create a new instance of DTSJob.
 *
 * @author Alex Arana
 */
public class DtsJobCreationException extends DtsException {
    /**
     * Constructs an instance of {@link DtsJobCreationException}.
     */
    public DtsJobCreationException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsJobCreationException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsJobCreationException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsJobCreationException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsJobCreationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
