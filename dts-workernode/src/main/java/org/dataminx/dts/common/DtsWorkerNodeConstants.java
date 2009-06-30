/**
 * Intersect Pty Ltd (c) 2009
 *
 * License: To Be Announced
 */
package org.dataminx.dts.common;

/**
 * Defines a common set of constants global to the DTS Worker Node application.
 *
 * @author Alex Arana
 */
public final class DtsWorkerNodeConstants {
    /**
     * Name of the configuration property that points to the directory holding configuration artifacts for the
     * application.
     * <p>
     * Normally, this parameter would be passed to the application at startup using the following:
     * <pre>
     *   java <b>-Ddataminx.dir=</b><i>/home/username/.dataminx</i> ...  &lt;runner_class&gt;
     * </pre>
     */
    public static final String DATAMINX_CONFIGURATION_KEY = "dataminx.dir";

    /** Default name of the dataminx configuration directory. */
    public static final String DEFAULT_DATAMINX_CONFIGURATION_DIR = ".dataminx";

    /** Default log4j configuration filename. */
    public static final String DEFAULT_LOG4J_CONFIGURATION_FILE = "log4j.xml";

    /** Default Spring classpath. */
    public static final String DEFAULT_SPRING_CLASSPATH =
        "au/org/dataminx/dts/applicationContext.xml";

    /** Default JVM property used to override the log4j configuration source. */
    public static final String DEFAULT_LOG4J_CONFIGURATION_KEY = "log4j.configuration";

    /**
     * A job execution context key used to hold a DTS job request.
     */
    public static final String DTS_SUBMIT_JOB_REQUEST_KEY = "SUBMIT_JOB_REQUEST";

    /**
     * A step execution context key used to hold a data staging element.
     */
    public static final String DTS_DATA_TRANSFER_STEP_KEY = "DATA_TRANSFER_STEP";
}
