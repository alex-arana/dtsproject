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
package org.dataminx.dts.common;

import static org.dataminx.dts.common.DtsConstants.DEFAULT_LOG4J_CONFIGURATION_FILE;
import static org.dataminx.dts.common.DtsConstants.DEFAULT_LOG4J_CONFIGURATION_KEY;

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
import org.springframework.util.Assert;
import org.springframework.util.Log4jConfigurer;
import org.springframework.util.ResourceUtils;

/**
 * A simple bean that initialises the Log4j logging subsystem. This class can then be configured as a Spring
 * {@link InitializingBean}.
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
     * Delay between refresh checks, in milliseconds. Default is 0, indicating no refresh checks at all.
     */
    @Qualifier
    private long mRefreshInterval;

    /**
     * {@inheritDoc}
     */
    public void afterPropertiesSet() throws Exception {
        if (mConfiguration == null || !mConfiguration.exists()) {
            mConfiguration = new ClassPathResource(
                DEFAULT_LOG4J_CONFIGURATION_FILE);
        }

        // allow the Log4J configuration resource to be overridden using the value of an
        // application-specific Java system property. eg.:
        //   java -Dlog4j.configuration=/etc/log4j.properties
        final String log4jOverride = System
            .getProperty(DEFAULT_LOG4J_CONFIGURATION_KEY);
        if (StringUtils.isNotBlank(log4jOverride)) {
            mConfiguration = new UrlResource(ResourceUtils
                .getURL(log4jOverride));
        }

        LogManager.resetConfiguration();
        final String uri = mConfiguration.getURL().toExternalForm();
        if (isFileResource(mConfiguration)) {
            if (mRefreshInterval == 0) {
                Log4jConfigurer.initLogging(uri);
            }
            else {
                Log4jConfigurer.initLogging(uri, mRefreshInterval);
            }
        }
        else {
            // the resource cannot be resolved as an absolute file path,
            // thus refresh is not supported
            Log4jConfigurer.initLogging(uri);
        }
        Logger.getLogger(getClass()).info(
            String.format("Configured logging from %s", mConfiguration));
    }

    /**
     * Returns logical <code>true</code> if the specified {@link Resource} points to a file in a locally available file
     * system, <code>false</code> otherwise.
     * <p>
     * This is purely a convenience method to handle a potential exception being raised when detecting this condition.
     *
     * @param resource
     *            Object that points to an underlying resource such as a file or classpath resource
     * @return true if the given Resource points to a File, false otherwise
     */
    private boolean isFileResource(final Resource resource) {
        Assert.notNull(resource);
        try {
            resource.getFile();
            return true;
        }
        catch (final IOException ex) {
            return false;
        }
    }

    public void setConfiguration(final Resource configuration) {
        mConfiguration = configuration;
    }

    public void setRefreshInterval(final long refreshInterval) {
        mRefreshInterval = refreshInterval;
    }
}
