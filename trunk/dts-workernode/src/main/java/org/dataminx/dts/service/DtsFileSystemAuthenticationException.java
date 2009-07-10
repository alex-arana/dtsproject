package org.dataminx.dts.service;

import org.dataminx.dts.DtsException;

/**
 * An error that gets thrown when the DTS service fails to authenticate to the
 * remote file system.
 *
 * @author Gerson Galang
 */
public class DtsFileSystemAuthenticationException extends DtsException {
    /**
     * Constructs an instance of {@link DtsFileSystemAuthenticationException}.
     */
    public DtsFileSystemAuthenticationException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsFileSystemAuthenticationException} with a specified message.
     *
     * @param message the detail message.
     */
    public DtsFileSystemAuthenticationException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link DtsFileSystemAuthenticationException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public DtsFileSystemAuthenticationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an instance of {@link DtsFileSystemAuthenticationException} with the specified cause.
     *
     * @param cause the cause.
     */
    public DtsFileSystemAuthenticationException(final Throwable cause) {
        super(cause);
    }

}
