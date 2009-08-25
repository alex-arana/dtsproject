package org.dataminx.dts.ws;


/**
 * An custom exception that gets thrown by the WS call.
 *
 * @author Gerson Galang
 */
public class CustomException  extends DtsFaultException {

    /**
     * Constructs an instance of {@link CustomException}.
     */
    public CustomException() {
        super();
    }

    /**
     * Constructs an instance of {@link CustomException} given the specified message.
     *
     * @param msg the exception message
     */
    public CustomException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@link CustomException} given the specified message and cause.
     *
     * @param msg the exception message
     * @param cause the error or exception that cause this exception to be thrown
     */
    public CustomException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of {@link CustomException} given the cause.
     *
     * @param cause the error or exception that cause this exception to be thrown
     */
    public CustomException(Throwable cause) {
        super(cause);
    }
}
