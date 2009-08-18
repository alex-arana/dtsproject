package org.dataminx.dts.ws;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * An exception that is thrown if the job failed to meet the minimum validity requirements.
 *
 * @author Gerson Galang
 */
@SoapFault(faultCode = FaultCode.CLIENT)
public class InvalidJobDefinitionException extends RuntimeException {

    /**
     * Constructs an instance of {@link InvalidJobDefinitionException}.
     */
    public InvalidJobDefinitionException() {
        super();
    }

    /**
     * Constructs an instance of {@link InvalidJobDefinitionException} given the specified message.
     *
     * @param msg the exception message
     */
    public InvalidJobDefinitionException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@link InvalidJobDefinitionException} given the specified message and cause.
     *
     * @param msg the exception message
     * @param cause the error or exception that cause this exception to be thrown
     */
    public InvalidJobDefinitionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of {@link InvalidJobDefinitionException} given the cause.
     *
     * @param cause the error or exception that cause this exception to be thrown
     */
    public InvalidJobDefinitionException(Throwable cause) {
        super(cause);
    }

}
