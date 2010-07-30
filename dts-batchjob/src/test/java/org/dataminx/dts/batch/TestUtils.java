/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dataminx.dts.batch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Some stuff that is usefull in all tests.
 * @author David Meredith
 */
public class TestUtils {

    public static String readFileAsString(String filePath) throws java.io.IOException {
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
