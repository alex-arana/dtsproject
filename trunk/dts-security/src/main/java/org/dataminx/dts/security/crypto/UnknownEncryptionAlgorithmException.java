package org.dataminx.dts.security.crypto;

public class UnknownEncryptionAlgorithmException extends Exception {
    /**
     * Constructs an instance of {@link DtsJobCreationException}.
     */
    public UnknownEncryptionAlgorithmException() {
        super();
    }

    /**
     * Constructs an instance of {@link UnknownEncryptionAlgorithmException} with a specified message.
     *
     * @param message the detail message.
     */
    public UnknownEncryptionAlgorithmException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link UnknownEncryptionAlgorithmException} with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause the cause.
     */
    public UnknownEncryptionAlgorithmException(final String message,
        final Throwable cause) {
        super(message, cause);
    }
}
