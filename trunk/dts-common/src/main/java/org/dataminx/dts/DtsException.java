/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts;

/**
 * <p>DtsException</p>
 * <p>Description: Base exception class for the DTS package.</p>
 *
 * @author Alex Arana
 */
public class DtsException extends RuntimeException {
    /**
     * Constructs an instance of {@link DtsException}.
     */
    public DtsException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of {@link DtsException} with the specified cause.
     *
     * @param cause the cause.
     */
    public DtsException(final Throwable cause) {
        super(cause);
    }
}
