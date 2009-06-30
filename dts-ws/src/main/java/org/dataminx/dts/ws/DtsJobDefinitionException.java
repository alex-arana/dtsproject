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
     */
    public DtsJobDefinitionException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@link DtsJobDefinitionException} given the specified message and cause.
     */
    public DtsJobDefinitionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of {@link DtsJobDefinitionException} given the cause.
     */
    public DtsJobDefinitionException(Throwable cause) {
        super(cause);
    }

}
