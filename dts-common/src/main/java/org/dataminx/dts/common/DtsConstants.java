/**
 *
 */
package org.dataminx.dts.common;

/**
 * Defines all the constants for the DTS application
 *
 * @author Alex Arana
 */
public class DtsConstants {

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


}
