package org.dataminx.dts.ws;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/**
 * An exception that gets thrown if the requested protocol to use in a job is not supported by the Data Transfer
 * Service.
 *
 * @author Gerson Galang
 */
@SoapFault(faultCode = FaultCode.CLIENT)
public class TransferProtocolNotSupportedException  extends RuntimeException {

    /**
     * Constructs an instance of {@link TransferProtocolNotSupportedException}.
     */
    public TransferProtocolNotSupportedException() {
        super();
    }

    /**
     * Constructs an instance of {@link TransferProtocolNotSupportedException} given the specified message.
     *
     * @param msg the exception message
     */
    public TransferProtocolNotSupportedException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@link TransferProtocolNotSupportedException} given the specified message and cause.
     *
     * @param msg the exception message
     * @param cause the error or exception that cause this exception to be thrown
     */
    public TransferProtocolNotSupportedException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of {@link TransferProtocolNotSupportedException} given the cause.
     *
     * @param cause the error or exception that cause this exception to be thrown
     */
    public TransferProtocolNotSupportedException(Throwable cause) {
        super(cause);
    }
}
