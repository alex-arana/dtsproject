/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.batch;

import org.dataminx.dts.DtsException;

/**
 * Represents an error that occurs while executing a DTS Job.
 *
 * @author Alex Arana
 */
public class DtsJobExecutionException extends DtsException {
    /**
     * Constructs an instance of {@link DtsJobExecutionException}.
     */
    public DtsJobExecutionException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsJobExecutionException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsJobExecutionException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsJobExecutionException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsJobExecutionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
