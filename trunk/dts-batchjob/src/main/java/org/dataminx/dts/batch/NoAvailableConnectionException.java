package org.dataminx.dts.batch;

import org.dataminx.dts.DtsException;

public class NoAvailableConnectionException extends DtsException {
    /**
     * Constructs an instance of {@link NoAvailableConnectionException}.
     */
    public NoAvailableConnectionException() {
        super();
    }

    /**
     * Constructs an instance of {@link NoAvailableConnectionException} with a
     * specified message.
     * 
     * @param message the detail message.
     */
    public NoAvailableConnectionException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link NoAvailableConnectionException} with the
     * specified detail message and cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public NoAvailableConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
