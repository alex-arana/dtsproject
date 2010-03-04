package org.dataminx.dts.vfs;

public class UnknownRootFileObjectException extends Exception {

    /**
     * Constructs an instance of {@link UnknownRootFileObjectException}.
     */
    public UnknownRootFileObjectException() {
        super();
    }

    /**
     * Constructs an instance of {@link UnknownRootFileObjectException} with a
     * specified message.
     * 
     * @param message the detail message.
     */
    public UnknownRootFileObjectException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link UnknownRootFileObjectException} with
     * the specified detail message and cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public UnknownRootFileObjectException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
