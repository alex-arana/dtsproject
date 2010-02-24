package org.dataminx.dts.vfs;

public class FileSystemManagerAlreadyInitializedException extends Exception {

    /**
     * Constructs an instance of {@link DtsJobCreationException}.
     */
    public FileSystemManagerAlreadyInitializedException() {
        super();
    }

    /**
     * Constructs an instance of {@link FileSystemManagerAlreadyInitializedException}
     * with a specified message.
     * 
     * @param message the detail message.
     */
    public FileSystemManagerAlreadyInitializedException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link FileSystemManagerAlreadyInitializedException}
     * with the specified detail message and cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public FileSystemManagerAlreadyInitializedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
