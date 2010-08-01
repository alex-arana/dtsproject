/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataminx.dts.batch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;

/**
 * Some stuff that is usefull in all tests.
 * @author David Meredith
 */
public class TestUtils {

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
