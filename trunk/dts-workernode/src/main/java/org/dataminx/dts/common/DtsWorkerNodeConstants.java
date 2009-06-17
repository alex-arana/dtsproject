/**
 * Copyright 2009 - DataMINX Project Team
 * http://www.dataminx.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    public static final String DTS_JOB_DEFINITION_KEY = "DTS_JOB_DEFINITION";
}
