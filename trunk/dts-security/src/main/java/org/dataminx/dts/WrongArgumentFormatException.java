package org.dataminx.dts;

public class WrongArgumentFormatException extends RuntimeException {
    /**
     * Constructs an instance of {@link WrongArgumentFormatException}.
     */
    public WrongArgumentFormatException() {
        super();
    }

    /**
     * Constructs an instance of {@link WrongArgumentFormatException} with a specified message.
     *
     * @param message the detail message.
     */
    public WrongArgumentFormatException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link WrongArgumentFormatException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public WrongArgumentFormatException(final String message,
        final Throwable cause) {
        super(message, cause);
    }
}
