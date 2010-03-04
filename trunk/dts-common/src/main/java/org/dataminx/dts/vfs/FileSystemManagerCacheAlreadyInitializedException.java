package org.dataminx.dts.vfs;

public class FileSystemManagerCacheAlreadyInitializedException extends Exception {

    /**
     * Constructs an instance of
     * {@link FileSystemManagerCacheAlreadyInitializedException}.
     */
    public FileSystemManagerCacheAlreadyInitializedException() {
        super();
    }

    /**
     * Constructs an instance of
     * {@link FileSystemManagerCacheAlreadyInitializedException} with a
     * specified message.
     * 
     * @param message the detail message.
     */
    public FileSystemManagerCacheAlreadyInitializedException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of
     * {@link FileSystemManagerCacheAlreadyInitializedException} with the
     * specified detail message and cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public FileSystemManagerCacheAlreadyInitializedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
