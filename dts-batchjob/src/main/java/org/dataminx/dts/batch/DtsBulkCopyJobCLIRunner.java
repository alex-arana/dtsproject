package org.dataminx.dts.batch;

import static org.dataminx.dts.common.DtsConstants.DATAMINX_CONFIGURATION_KEY;
import static org.dataminx.dts.common.DtsConstants.DEFAULT_DATAMINX_CONFIGURATION_DIR;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlbeans.XmlException;
import org.dataminx.dts.DtsException;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

/**
 * The command line runner for submitting a DTS job directly to Spring Batch.
 * 
 * @author Gerson Galang
 */
public class DtsBulkCopyJobCLIRunner {

    /** Default Spring classpath definition. */
    public static final String[] DEFAULT_SPRING_CLASSPATH = { "/org/dataminx/dts/batch/application-context.xml",
            "/org/dataminx/dts/batch/batch-context.xml" };

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DtsBulkCopyJobCLIRunner.class);

    /** The spring batch job launcher. */
    private DtsJobLauncher mJobLauncher;

    /** The dataminx config directory. */
    private final File mConfigDir;

    /**
     * Constructs a new instance of the DtsBulkCopyJobCLIRunner using the
     * default configuration folder.
     */
    public DtsBulkCopyJobCLIRunner() {
        this("");
    }

    /**
     * Constructs a new instance of the DtsBulkCopyJobCLIRunner.
     * <p>
     * If the input string is either <code>null</code> or points to a
     * non-existent directory, the application will attempt to access the
     * default configuration folder, represented by the symbolic constant:
     * {@link org.dataminx.dts.common.DtsConstants#DEFAULT_DATAMINX_CONFIGURATION_DIR}.
     * 
     * @param configDir String holding the fully qualified path to the DataMINX
     *        configuration folder (or null).
     */
    public DtsBulkCopyJobCLIRunner(final String configDir) {
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
     * The DtsBulkCopyJob Command Line Runner launcher.
     * 
     * @param args command line arguments.
     */
    public static void main(final String[] args) {

        // now lets check for the required cmd line arg
        if (args.length != 1) {
            System.err.println("DtsBulkCopyJobCLIRunner: missing input file");
            System.err.println("Try DtsBulkCopyJobCLIRunner <path-to-dts-job-definition-document>");
            return;
        }

        // lets setup the dataminx config.
        final String configDir = System.getProperty(DATAMINX_CONFIGURATION_KEY);
        final DtsBulkCopyJobCLIRunner jobRunner = new DtsBulkCopyJobCLIRunner(configDir);
        jobRunner.initAppContext();

        File f;
        try {
            f = new FileSystemResource(args[0]).getFile();
            final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(f);
            jobRunner.runJob(UUID.randomUUID().toString(), dtsJob);

        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final JobExecutionAlreadyRunningException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final JobRestartException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (final JobInstanceAlreadyCompleteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Initialise the application context.
     */
    public void initAppContext() {
        final String[] classpath = getSpringClasspath();
        final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(classpath,
                DtsBulkCopyJobCLIRunner.class);
        LOGGER.debug("Spring context loaded from classpath: " + ArrayUtils.toString(classpath));

        // since our messaging container beans are lifecycle aware the application
        // will immediately start processing
        context.start();

        mJobLauncher = (DtsJobLauncher) context.getBean("dtsJobLauncher");
    }

    /**
     * Runs the DTS job.
     * 
     * @param jobId the jobResourceKey or the job's name or ID provided by the
     *        calling application
     * @param job the DTS Job
     * @throws JobExecutionAlreadyRunningException if the job is already running
     * @throws JobRestartException if an error occurs when a job is restarted
     * @throws JobInstanceAlreadyCompleteException if the job has already
     *         successfully finished
     */
    public void runJob(final String jobId, final JobDefinitionDocument job) throws JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {
        mJobLauncher.run(jobId, job);
    }

    /**
     * Sets the job launcher.
     * 
     * @param jobLauncher the DtsJobLauncher
     */
    public void setDtsJobLauncher(final DtsJobLauncher jobLauncher) {
        mJobLauncher = jobLauncher;
    }

}
