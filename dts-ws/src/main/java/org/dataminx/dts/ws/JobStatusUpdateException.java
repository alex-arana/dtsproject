package org.dataminx.dts.ws;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * An exception that gets thrown if the request to change the job's status is not valid (eg suspending job that is
 * already suspended, cancelling a job that has already finished running, etc).
 *
 * @author Gerson Galang
 */
@SoapFault(faultCode = FaultCode.CLIENT)
public class JobStatusUpdateException extends RuntimeException {

    /**
     * Constructs an instance of {@link JobStatusUpdateException}.
     */
    public JobStatusUpdateException() {
        super();
    }

    /**
     * Constructs an instance of {@link JobStatusUpdateException} given the specified message.
     *
     * @param msg the exception message
     */
    public JobStatusUpdateException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@link JobStatusUpdateException} given the specified message and cause.
     *
     * @param msg the exception message
     * @param cause the error or exception that cause this exception to be thrown
     */
    public JobStatusUpdateException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of {@link JobStatusUpdateException} given the cause.
     *
     * @param cause the error or exception that cause this exception to be thrown
     */
    public JobStatusUpdateException(Throwable cause) {
        super(cause);
    }

}
