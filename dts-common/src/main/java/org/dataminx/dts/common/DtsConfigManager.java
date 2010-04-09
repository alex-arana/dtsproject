/**
 * Copyright (c) 2009, VeRSI Consortium
 *   (Victorian eResearch Strategic Initiative, Australia)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the VeRSI, the VeRSI Consortium members, nor the
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

import java.net.URL;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.context.annotation.Scope;

/**
 * The Dts Configuration Manager.
 *
 * @author Gerson Galang
 */
@Scope("singleton")
public class DtsConfigManager {

    /** The DTS config. */
    private Configuration mDtsConfig;

    /**
     * Sets the dts config location.
     *
     * @param dtsConfigLocation the new dts config location in String format
     * @throws ConfigurationException if an error occurs while initialising the configuration from the
     *         specified location
     */
    public void setDtsConfigLocation(final String dtsConfigLocation) throws ConfigurationException {
        mDtsConfig = new PropertiesConfiguration(dtsConfigLocation);
    }

    /**
     * Sets the dts config location.
     *
     * @param dtsConfigLocation the new dts config location in URL format
     * @throws ConfigurationException if an error occurs while initialising the configuration from the
     *         specified location
     */
    public void setDtsConfigLocation(final URL dtsConfigLocation) throws ConfigurationException {
        mDtsConfig = new PropertiesConfiguration(dtsConfigLocation);
    }

    /**
     * Gets the dts config.
     *
     * @return the dts config
     */
    public Configuration getDtsConfig() {
        return mDtsConfig;
    }
}
