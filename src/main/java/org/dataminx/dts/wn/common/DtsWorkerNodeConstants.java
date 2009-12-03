/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.wn.common;

/**
 * Defines a common set of constants global to the DTS Worker Node application.
 *
 * @author Alex Arana
 */
public final class DtsWorkerNodeConstants {

    /**
     * A job execution context key used to hold a DTS Job ID.
     */
    public static final String DTS_JOB_ID_KEY = "DTS_JOB_ID_KEY";

    /**
     * A job execution context key used to hold a DTS job request.
     */
    public static final String DTS_SUBMIT_JOB_REQUEST_KEY = "SUBMIT_JOB_REQUEST";

    /**
     * A step execution context key used to hold a data transfer details element.
     */
    public static final String DTS_DATA_TRANSFER_STEP_KEY = "DATA_TRANSFER_STEP";

    /** Holds the namespace URI of the WS-Security namespace. */
    public static final String WS_SECURITY_NAMESPACE_URI =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
}
