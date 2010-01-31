package org.dataminx.dts.wn.batch;

import org.dataminx.dts.DtsException;

public class DtsJobCancelledException extends DtsException {
    /**
     * Constructs an instance of {@link DtsJobCreationException}.
     */
    public DtsJobCancelledException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsJobCancelledException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsJobCancelledException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsJobCancelledException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsJobCancelledException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
