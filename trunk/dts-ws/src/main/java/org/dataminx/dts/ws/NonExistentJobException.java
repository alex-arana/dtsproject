package org.dataminx.dts.ws;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * An exception that is thrown if the job being queried for, suspended, resumed, or cancelled doesn't exist in the
 * Data Transfer Service's job database.
 *
 * @author Gerson Galang
 */
@SoapFault(faultCode = FaultCode.CLIENT)
public class NonExistentJobException extends RuntimeException {

    /**
     * Constructs an instance of {@link NonExistentJobException}.
     */
    public NonExistentJobException() {
        super();
    }

    /**
     * Constructs an instance of {@link NonExistentJobException} given the specified message.
     *
     * @param msg the exception message
     */
    public NonExistentJobException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@link NonExistentJobException} given the specified message and cause.
     *
     * @param msg the exception message
     * @param cause the error or exception that cause this exception to be thrown
     */
    public NonExistentJobException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of {@link NonExistentJobException} given the cause.
     *
     * @param cause the error or exception that cause this exception to be thrown
     */
    public NonExistentJobException(Throwable cause) {
        super(cause);
    }

}
