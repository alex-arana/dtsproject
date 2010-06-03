package org.dataminx.dts.common.util;

/**
 * TestFileChooser.
 *
 * @author Gerson Galang
 */
public class TestFileChooser {

    /**
     * Returns the type of file to use as an input file based on the OS the user
     * is running the test on.
     *
     * @return "-win" if windows, "-mac" if mac, "-nix" if unix/linux
     */
    public static String getTestFilePostfix() {
        final String os = System.getProperty("os.name").toLowerCase();
        String postfix = "";
        if (os.indexOf("win") >= 0) {
            postfix = "-win";
        }
        else if (os.indexOf("mac") >= 0) {
            postfix = "-mac";
        }
        else if (os.indexOf("nix") >= 0 || os.indexOf("linux") >= 0) {
            postfix = "-nix";
        }
        return postfix;
    }

}
