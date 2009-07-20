package org.dataminx.dts.ws;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * An exception that relates to the an error on how the DTS Job Definition document
 * has been composed.
 *
 * @author Gerson Galang
 */
@SoapFault(faultCode = FaultCode.CLIENT)
public class DtsJobDefinitionException extends RuntimeException {

    /**
     * Constructs an instance of {@link DtsJobDefinitionException}.
     */
    public DtsJobDefinitionException() {
        super();
    }

    /**
     * Constructs an instance of {@link DtsJobDefinitionException} given the specified message.
     *
     * @param msg the exception message
     */
    public DtsJobDefinitionException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@link DtsJobDefinitionException} given the specified message and cause.
     *
     * @param msg the exception message
     * @param cause the error or exception that cause this exception to be thrown
     */
    public DtsJobDefinitionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of {@link DtsJobDefinitionException} given the cause.
     *
     * @param cause the error or exception that cause this exception to be thrown
     */
    public DtsJobDefinitionException(Throwable cause) {
        super(cause);
    }

}
