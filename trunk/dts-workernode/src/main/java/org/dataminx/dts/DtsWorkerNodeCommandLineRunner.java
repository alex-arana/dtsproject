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

import org.apache.commons.lang.ArrayUtils;
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

    /**
     * Returns the Spring application classpath for this application.
     *
     * @return Spring application classpath as an array of {@link String}s
     */
    public static String[] getSpringClasspath() {
        return DEFAULT_SPRING_CLASSPATH;
    }

    /**
     * DTS Worker Node application command-line launcher.
     *
     * @param args Command-line arguments
     */
    public static void main(final String[] args) {
        final String[] classpath = getSpringClasspath();
        final ClassPathXmlApplicationContext context =
            new ClassPathXmlApplicationContext(classpath, DtsWorkerNodeCommandLineRunner.class);
        LOG.debug("Spring context loaded from classpath: " + ArrayUtils.toString(classpath));

        // since our messaging container beans are lifecycle aware the application
        // will immediately start processing
        context.start();
        LOG.info("DTS Worker Node has started.");
    }
}
