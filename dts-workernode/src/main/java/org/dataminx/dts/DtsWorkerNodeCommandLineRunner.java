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
package org.dataminx.dts;

import static org.dataminx.dts.common.DtsWorkerNodeConstants.DATAMINX_CONFIGURATION_KEY;
import static org.dataminx.dts.common.DtsWorkerNodeConstants.DEFAULT_DATAMINX_CONFIGURATION_DIR;

import java.io.File;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Command-line launcher for the DTS Worker Node application.
 *
 * @author Alex Arana
 */
public class DtsWorkerNodeCommandLineRunner {
    /** Default Spring classpath definition. */
    public static final String[] DEFAULT_SPRING_CLASSPATH = {
        "/application-context.xml",
        "/integration-context.xml",
        "/activemq/jms-context.xml"
    };

    /** Internal application logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsWorkerNodeCommandLineRunner.class);

    /** Holds the location of the configuration directory used to initialise the application. */
    private final File mConfigDir;

    /**
     * Constructs a new instance of <code>DtsWorkerNodeCommandLineRunner</code> using the specified DataMINX
     * configuration folder.
     * <p>
     * If the input string is either <code>null</code> or points to a non-existent directory, the application
     * will attempt to access the default configuration folder, represented by the symbolic constant:
     * {@link org.dataminx.dts.common.DtsWorkerNodeConstants#DEFAULT_DATAMINX_CONFIGURATION_DIR}.
     *
     * @param configDir String holding the fully qualified path to the DataMINX configuration folder (or null).
     * @throws DtsException When an error occurs ...
     */
    public DtsWorkerNodeCommandLineRunner(final String configDir) throws DtsException {
        if (StringUtils.isNotBlank(configDir)) {
            mConfigDir = new File(configDir);
        }
        else {
            mConfigDir = new File(SystemUtils.USER_HOME, DEFAULT_DATAMINX_CONFIGURATION_DIR);
        }

        if (!mConfigDir.exists()) {
            throw new DtsException(String.format("An error occurred lanching the DTS Worker Node."
                + " Invalid DataMINX configuration folder: '%s'.  Check your configuration",
                mConfigDir.getAbsolutePath()));
        }

        if (!mConfigDir.canRead()) {
            throw new DtsException(String.format(
                "An error occurred accessing the configuration folder for the DTS Worker Node: '%s'."
                + " Check your access permissions.", mConfigDir.getAbsolutePath()));
        }

        // set the system property globally
        if (!System.getProperties().containsKey(DATAMINX_CONFIGURATION_KEY)) {
            System.setProperty(DATAMINX_CONFIGURATION_KEY, mConfigDir.getAbsolutePath());
        }
    }

    /**
     * Returns the Spring application classpath for this application.
     *
     * @return Spring application classpath as an array of {@link String}s
     */
    public String[] getSpringClasspath() {
        return DEFAULT_SPRING_CLASSPATH;
    }

    /**
     * Runs the DTS Worker Node container.
     */
    public void run() {
        final String[] classpath = getSpringClasspath();
        final ClassPathXmlApplicationContext context =
            new ClassPathXmlApplicationContext(classpath, DtsWorkerNodeCommandLineRunner.class);
        LOG.debug("Spring context loaded from classpath: " + ArrayUtils.toString(classpath));

        // since our messaging container beans are lifecycle aware the application
        // will immediately start processing
        context.start();
        LOG.info("DTS Worker Node has started.");
    }

    /**
     * DTS Worker Node application command-line launcher.
     *
     * @param args Command-line arguments
     * @throws DtsException if an error occurs initialising the application
     */
    public static void main(final String[] args) throws DtsException {
        //TODO consider accepting config location as a command-line argument instead...
        final String configDir = System.getProperty(DATAMINX_CONFIGURATION_KEY);
        new DtsWorkerNodeCommandLineRunner(configDir).run();
    }
}
