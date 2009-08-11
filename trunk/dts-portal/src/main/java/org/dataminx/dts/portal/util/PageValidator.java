package org.dataminx.dts.portal.util;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * The PageValidator provides utility methods to check for required fields/attributes in a page before it
 * actually gets loaded.
 *
 * @author Gerson Galang
 */
public class PageValidator {

    /** The Constant LOGGED_IN_KEY. */
    private static final String LOGGED_IN_KEY = "commonName";

    /**
     * Checks if a referer provided in the request header.
     *
     * @param servletRequest the servlet request
     *
     * @return true, if the referer attribute is provided in the servlet request header
     */
    public static boolean isRefererProvided(HttpServletRequest servletRequest) {
        if (servletRequest.getHeader("referer") != null) {
            return true;
        }
        return false;
    }

    /**
     * Checks if is LOGGED_IN_KEY exists in the sessionMap.
     *
     * @param sessionMap the session map
     *
     * @return true, if the LOGGED_IN_KEY exists in the sessionMap
     */
    public static boolean isUserLoggedIn(Map sessionMap) {
        if (sessionMap.get(LOGGED_IN_KEY) != null) {
            return true;
        }
        return false;
    }

}
