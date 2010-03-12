package org.dataminx.dts.common.vfs;

public class UnknownFileSystemManagerException extends Exception {
    /**
     * Constructs an instance of {@link DtsJobCreationException}.
     */
    public UnknownFileSystemManagerException() {
        super();
    }

    /**
     * Constructs an instance of {@link UnknownFileSystemManagerException} with
     * a specified message.
     * 
     * @param message the detail message.
     */
    public UnknownFileSystemManagerException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link UnknownFileSystemManagerException} with
     * the specified detail message and cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public UnknownFileSystemManagerException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
