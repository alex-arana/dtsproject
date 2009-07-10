package org.dataminx.dts.common;

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

    /** The Constant DEFAULT_MYPROXY_CREDENTIAL_LIFETIME_KEY. */
    public static final String DEFAULT_MYPROXY_CREDENTIAL_LIFETIME_KEY = "default.myproxy.lifetime";

    /** The DTS config. */
    private Configuration mDtsConfig;

    /**
     * Sets the dts config location.
     *
     * @param dtsConfigLocation the new dts config location
     */
    public void setDtsConfigLocation(String dtsConfigLocation) {
        try {
            mDtsConfig = new PropertiesConfiguration(dtsConfigLocation);
        }
        catch (ConfigurationException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
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
