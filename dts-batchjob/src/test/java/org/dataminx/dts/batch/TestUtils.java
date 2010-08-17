/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataminx.dts.batch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import org.dataminx.dts.common.DtsConstants;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;

/**
 * Some stuff that is usefull in all tests.
 * @author David Meredith
 */
public class TestUtils {


    /**
     * Test that the test environment has been set up ok, inc. setting of the
     * -Ddataminx.dir, jobsteps dir, testfiles.
     * @throws IllegalStateException if the test environment is not setup ok.
     */
    protected static void assertTestEnvironmentOk() throws IllegalStateException {
        //if(true) throw new IllegalStateException("assert test fail");
        // check -Ddataminx.dir
        // before upgrading to maven-surefire-plugin version 2.5, the dataminx.dir
        // system property had to be set. Surefire 2.5 can accept any value from
        // Maven's properties that can be converted to String value !!
        // can therefore specify the -Ddataminx.dir=/path/to/dataminx/dir on the
        // command line when running tests
        try{
        if (!System.getProperties().containsKey(DtsConstants.DATAMINX_CONFIGURATION_KEY)) {
            throw new IllegalStateException("Please specify full path of your dataminx.dir using: " +
                    "'mvn -Ddataminx.dir=/full/path/to/.dataminx.dir test'");
        }
        // check config dir
        File configdir = new File(System.getProperty(DtsConstants.DATAMINX_CONFIGURATION_KEY));
        if (!configdir.exists() || !configdir.isDirectory() || !configdir.canWrite()) {
            throw new IllegalStateException(
                    String.format(" Invalid DataMINX configuration folder: '%s'.  Check your configuration",
                    configdir.getAbsolutePath()));
        }
        // Check jobsteps dir
        File jobStepsDir = new File(configdir, "jobsteps");
        if (!jobStepsDir.exists() || !jobStepsDir.isDirectory() || !jobStepsDir.canWrite()) {
            throw new IllegalStateException(
                    String.format(" Invalid DataMINX jobStepsDir folder: '%s'.  Check your configuration",
                    jobStepsDir.getAbsolutePath()));
        }
        // check test files present
        File testFilesDir = new File(System.getProperty("user.home"), "testfiles");
        if (!testFilesDir.exists() || !testFilesDir.isDirectory() || !testFilesDir.canRead()) {
             throw new IllegalStateException(
            String.format(" Invalid testfiles folder: '%s'.  Please unpack the 'testfiles.zip' resource in your home directory to run tests",
                    testFilesDir.getAbsolutePath()));
        }
        }catch(IllegalStateException ex ){
            throw new IllegalStateException("=================================================================\n"
                    +ex.getMessage()+"\n=================================================================");
        }

    }



    /**
     * Return the jobDefDoc from the given file. Perform required filtering of test docs. 
     * @param f
     * @return
     * @throws Exception
     */
    protected static JobDefinitionDocument getTestJobDefinitionDocument(final File f) throws Exception {
        //final File f = new ClassPathResource("/org/dataminx/dts/batch/transfer-1file.xml").getFile();
        String docString = TestUtils.readFileAsString(f.getAbsolutePath());
        String homeDir = System.getProperty("user.home").replaceAll("\\\\", "/");
        docString = docString.replaceAll("@home.dir.replacement@", homeDir);
        //System.out.println(docString);
        final JobDefinitionDocument dtsJob = JobDefinitionDocument.Factory.parse(docString);
        return dtsJob; 
    }

    /**
     * Return the file contents as a string
     * @param filePath
     * @return
     * @throws java.io.IOException
     */
    private static String readFileAsString(String filePath) throws java.io.IOException {
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
        } finally {
            if (f != null) {
                f.close();
            }
        }
        return new String(buffer);
    }
}
