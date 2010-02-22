package org.dataminx.dts.batch;

import org.dataminx.dts.DtsException;

public class JobScopingException extends DtsException {

    /**
     * Constructs an instance of {@link DtsJobCreationException}.
     */
    public JobScopingException() {
        super();
    }

    /**
     * Constructs an instance of {@link JobScopingException} with a specified
     * message.
     * 
     * @param message the detail message.
     */
    public JobScopingException(final String message) {
        super(message);
    }

    /**
     * Constructs an instance of {@link JobScopingException} with the specified
     * detail message and cause.
     * 
     * @param message the detail message.
     * @param cause the cause.
     */
    public JobScopingException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
