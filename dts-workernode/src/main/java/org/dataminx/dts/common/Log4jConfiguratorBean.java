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

import static org.dataminx.dts.common.DtsWorkerNodeConstants.DEFAULT_LOG4J_CONFIGURATION_FILE;
import static org.dataminx.dts.common.DtsWorkerNodeConstants.DEFAULT_LOG4J_CONFIGURATION_KEY;

import java.io.File;
import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Repository;
import org.springframework.util.Log4jConfigurer;
import org.springframework.util.ResourceUtils;

/**
 * A simple bean that initialises the Log4j logging subsystem. This class can then be configured
 * as a Spring {@link InitializingBean}.
 *
 * @author Alex Arana
 */
@Scope("singleton")
@Repository("log4jConfigurator")
public class Log4jConfiguratorBean implements InitializingBean {
    /** Holds the log4j configuration file resource. */
    @Qualifier
    private Resource mConfiguration;

    /**
     * Delay between refresh checks, in milliseconds.
     * Default is 0, indicating no refresh checks at all.
     */
    @Qualifier
    private long mRefreshInterval;

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        if (mConfiguration == null || !mConfiguration.exists()) {
            mConfiguration = new ClassPathResource(DEFAULT_LOG4J_CONFIGURATION_FILE);
        }

        // allow the Log4J configuration resource to be overridden using the value of an
        // application-specific Java system property. eg.:
        //   java -Dlog4j.configuration=/etc/log4j.properties
        final String log4jOverride = System.getProperty(DEFAULT_LOG4J_CONFIGURATION_KEY);
        if (StringUtils.isNotBlank(log4jOverride)) {
            mConfiguration = new UrlResource(ResourceUtils.getURL(log4jOverride));
        }

        LogManager.resetConfiguration();
        try {
            final File file = mConfiguration.getFile();
            final String path = file.getAbsolutePath();
            if (mRefreshInterval == 0) {
                Log4jConfigurer.initLogging(path);
            }
            else {
                Log4jConfigurer.initLogging(path, mRefreshInterval);
            }
        }
        catch (final IOException ioe) {
            // the resource cannot be resolved as an absolute file path,
            // thus refresh is not supported
            final String url = mConfiguration.getURL().toExternalForm();
            Log4jConfigurer.initLogging(url);
        }

        Logger.getLogger(getClass()).info(String.format("Configured logging from %s", mConfiguration));
    }

    public void setConfiguration(final Resource configuration) {
        mConfiguration = configuration;
    }

    public void setRefreshInterval(final long refreshInterval) {
        mRefreshInterval = refreshInterval;
    }
}
