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

import static org.dataminx.dts.common.DtsConstants.DEFAULT_LOG4J_CONFIGURATION_FILE;
import static org.dataminx.dts.common.DtsConstants.DEFAULT_LOG4J_CONFIGURATION_KEY;

import java.io.File;
import java.net.URL;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.Loader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.util.Log4jConfigurer;

/**
 * A simple bean that initialises the Log4j logging subsystem. This class can then be configured
 * as a Spring {@link InitializingBean}.
 *
 * @author Alex Arana
 */
@Scope("singleton")
@Repository("log4jConfigurator")
public class Log4jConfiguratorBean implements InitializingBean {
    /** Holds the URI of the log4j configuration file. */
    @Qualifier
    private String mLocation;

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
        if (StringUtils.isBlank(mLocation)) {
            final URL url;
            final File file = new File(DEFAULT_LOG4J_CONFIGURATION_FILE);
            if (file.exists()) {
                url = file.toURI().toURL();
            }
            else {
                url = Loader.getResource(DEFAULT_LOG4J_CONFIGURATION_FILE);
            }

            // check for log4j configuration override in relevant JVM property
            final String log4jOverride = System.getProperty(DEFAULT_LOG4J_CONFIGURATION_KEY);
            mLocation = log4jOverride == null ? url.toExternalForm() : log4jOverride;
        }

        LogManager.resetConfiguration();
        if (mRefreshInterval == 0) {
            Log4jConfigurer.initLogging(mLocation);
        }
        else {
            Log4jConfigurer.initLogging(mLocation, mRefreshInterval);
        }
        Logger.getLogger(getClass()).info(String.format("Configured logging from '%s'", mLocation));
    }

    public void setLocation(final String location) {
        mLocation = location;
    }

    public void setRefreshInterval(final long refreshInterval) {
        mRefreshInterval = refreshInterval;
    }
}
