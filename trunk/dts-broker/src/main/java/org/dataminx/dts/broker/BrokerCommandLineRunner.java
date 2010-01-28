package org.dataminx.dts.broker;

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
 * Command-line runner to launch DTS MessageBroker
 *
 * @author hnguyen
 */
public class BrokerCommandLineRunner {
    /** Default Spring config file location */
    public static final String[] DEFAULT_SPRING_CLASSPATH = {
        "/org/dataminx/dts/broker/broker-context.xml",
        "/org/dataminx/dts/broker/integration-context.xml",
        "/org/dataminx/dts/broker/activemq/jms-context.xml"
    };

    /** Internal application logger. */
    private static final Logger LOG = LoggerFactory.getLogger(BrokerCommandLineRunner.class);

    private final File mConfigDir;

    public BrokerCommandLineRunner(String configDir) {
        if (StringUtils.isNotBlank(configDir)) {
            mConfigDir = new File(configDir);
        }
        else {
            mConfigDir = new File(SystemUtils.USER_HOME, DEFAULT_DATAMINX_CONFIGURATION_DIR);
        }

        if (!mConfigDir.exists()) {
            throw new DtsException(String.format("An error occurred launching the DTS Broker."
                + " Invalid DataMINX configuration folder: '%s'.  Check your configuration",
                mConfigDir.getAbsolutePath()));
        }

        if (!mConfigDir.canRead()) {
            throw new DtsException(String.format(
                "An error occurred accessing the configuration folder for the DTS Broker: '%s'."
                + " Check your access permissions.", mConfigDir.getAbsolutePath()));
        }

        // set the system property globally
        if (!System.getProperties().containsKey(DATAMINX_CONFIGURATION_KEY)) {
            System.setProperty(DATAMINX_CONFIGURATION_KEY, mConfigDir.getAbsolutePath());
        }

    }

    public void run() {
        final String[] classpath = getSpringClasspath();
        final ClassPathXmlApplicationContext context =
            new ClassPathXmlApplicationContext(classpath, BrokerCommandLineRunner.class);
        LOG.debug("Spring context loaded from classpath: " + ArrayUtils.toString(classpath));

        // since our messaging container beans are lifecycle aware the application
        // will immediately start processing
        context.start();
    }

    private String[] getSpringClasspath() {
        return DEFAULT_SPRING_CLASSPATH;
    }

    public static void main(String args[]) {
        final String configDir = System.getProperty(DATAMINX_CONFIGURATION_KEY);
        new BrokerCommandLineRunner(configDir).run();
    }

}
