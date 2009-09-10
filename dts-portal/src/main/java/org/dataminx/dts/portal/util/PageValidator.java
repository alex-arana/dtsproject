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
