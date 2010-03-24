package org.dataminx.dts.batch;

public class UnsatisfiedRequirementsException extends Exception {
    /**
     * Constructs an instance of {@link UnsatisfiedRequirementsException}.
     */
    public UnsatisfiedRequirementsException() {
        super();
    }

    /**
     * Constructs an instance of {@link UnsatisfiedRequirementsException} with a
     * specified message.
     * 
     * @param message the detail message.
     */
    public UnsatisfiedRequirementsException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link UnsatisfiedRequirementsException} with
     * the specified detail message and cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public UnsatisfiedRequirementsException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
