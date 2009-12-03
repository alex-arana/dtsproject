/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.vfs;

import org.dataminx.dts.DtsException;

/**
 * Represents an error that occurs while splitting a single DTS job request into its most atomic parts
 * for the purposes of processing.
 *
 * @author Alex Arana
 */
public class DtsJobScopingException extends DtsException {
    /**
     * Constructs an instance of {@link DtsJobScopingException}.
     */
    public DtsJobScopingException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsJobScopingException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsJobScopingException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsJobScopingException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsJobScopingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of {@link DtsJobScopingException} with the specified cause.
     *
     * @param cause the cause.
     */
    public DtsJobScopingException(final Throwable cause) {
        super(cause);
    }
}
