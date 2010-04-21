/**
 * Copyright (c) 2009, Intersect, Australia
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Intersect, Intersect's partners, nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.dataminx.dts.wn;

import static org.dataminx.dts.common.DtsConstants.DATAMINX_CONFIGURATION_KEY;
import static org.dataminx.dts.common.DtsConstants.DEFAULT_DATAMINX_CONFIGURATION_DIR;

import java.io.File;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.dataminx.dts.DtsException;
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
    public static final String[] DEFAULT_SPRING_CLASSPATH = { "/org/dataminx/dts/wn/application-context.xml",
            "/org/dataminx/dts/batch/batch-context.xml", "/org/dataminx/dts/wn/integration-context.xml",
            "/org/dataminx/dts/wn/activemq/jms-context.xml" };

    /** Internal application logger. */
    private static final Logger LOG = LoggerFactory.getLogger(DtsWorkerNodeCommandLineRunner.class);

    /**
     * Holds the location of the configuration directory used to initialise the
     * application.
     */
    private final File mConfigDir;

    /**
     * Constructs a new instance of <code>DtsWorkerNodeCommandLineRunner</code>
     * using the specified DataMINX configuration folder.
     * <p>
     * If the input string is either <code>null</code> or points to a
     * non-existent directory, the application will attempt to access the
     * default configuration folder, represented by the symbolic constant:
     * {@link org.dataminx.dts.common.DtsConstants#DEFAULT_DATAMINX_CONFIGURATION_DIR}.
     *
     * @param configDir String holding the fully qualified path to the DataMINX
     *        configuration folder (or null).
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
            throw new DtsException(String.format("An error occurred launching the DTS Worker Node."
                    + " Invalid DataMINX configuration folder: '%s'.  Check your configuration", mConfigDir
                    .getAbsolutePath()));
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
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(classpath,
                DtsWorkerNodeCommandLineRunner.class);
        LOG.debug("Spring context loaded from classpath: " + ArrayUtils.toString(classpath));

        context.registerShutdownHook();
        // since our messaging container beans are lifecycle aware the application
        // will immediately start processing
        context.start();

        final DtsWorkerNodeInformationService service = (DtsWorkerNodeInformationService) context
                .getBean("dtsWorkerNodeInformationService");
        /*
         Call the getInstanceId()method that is able to read or generate and persistent a worker node ID.
         Read the work node ID from the dts-workernode.properties configuration file, if the ID exists in that file;
         otherwise, to generate a unique ID for the worker node firstly and persistent 
         the ID into the configuration file for later restarts of worker node and accesses.
        */
        String workerNodeID = service.getInstanceId();
        LOG.info(String.format("DTS Worker Node has started: %s", workerNodeID));
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
