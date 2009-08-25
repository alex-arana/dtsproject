package org.dataminx.dts.ws;

import java.util.Date;

/**
 * The DtsFaultException is the base class of all the fault related exceptions that can be thrown by the DTS WS.
 *
 * @author Gerson Galang
 */
public class DtsFaultException extends RuntimeException {

    private Date mTimestamp;

    /**
     * Constructs an instance of {@link DtsFaultException}.
     */
    public DtsFaultException() {
        super();
        setTimestamp(new Date());
    }

    /**
     * Constructs an instance of {@link DtsFaultException} given the specified message.
     *
     * @param msg the exception message
     */
    public DtsFaultException(String msg) {
        super(msg);
        setTimestamp(new Date());
    }

    /**
     * Constructs an instance of {@link DtsFaultException} given the specified message and cause.
     *
     * @param msg the exception message
     * @param cause the error or exception that cause this exception to be thrown
     */
    public DtsFaultException(String msg, Throwable cause) {
        super(msg, cause);
        setTimestamp(new Date());
    }

    /**
     * Constructs an instance of {@link CustomException} given the cause.
     *
     * @param cause the error or exception that cause this exception to be thrown
     */
    public DtsFaultException(Throwable cause) {
        super(cause);
        setTimestamp(new Date());
    }

    /**
     * Sets the timestamp on when the fault occurred.
     */
    public void setTimestamp(Date timestamp) {
        mTimestamp = timestamp;
    }

    /**
     * Returns the date on when the fault/exception was thrown on the DTS Web Service.
     *
     * @return the date on when the fault/exception was thrown on the DTS Web Service
     */
    public Date getTimestamp() {
        return mTimestamp;
    }
}
