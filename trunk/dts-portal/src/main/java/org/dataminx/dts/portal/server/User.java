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
package org.dataminx.dts.portal.server;

import static org.dataminx.dts.portal.util.PageValidator.isRefererProvided;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validation;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.util.ServletContextAware;
import org.dataminx.dts.security.auth.callback.PassiveCallbackHandler;
import org.dataminx.dts.security.auth.module.MyProxyPrincipal;

/**
 * The User Action class handles the portal's user authentication.
 *
 * @author Gerson Galang
 */
@Validation()
public class User extends ActionSupport implements SessionAware, ServletRequestAware,
        ServletResponseAware, ServletContextAware {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(User.class);

    /** A string that will be used to find the actual Common Name value in a Distinguished Name. */
    private static final String CN_EQUALS = "CN=";

    /** The username used to login. */
    private String mUsername;

    /** The password used to login. */
    private String mPassword;

    /** The auto injected HTTP session object. */
    private Map mSessionMap;

    /** The auto injected servlet request object. */
    private HttpServletRequest mServletRequest;

    /** The auto injected servlet response object. */
    private HttpServletResponse mServletResponse;

    /** The auto injected servlet context object. */
    private ServletContext mServletContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSession(Map session) {
        mSessionMap = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServletRequest(HttpServletRequest request) {
        mServletRequest = request;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServletResponse(HttpServletResponse response) {
        mServletResponse = response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setServletContext(ServletContext context) {
        mServletContext = context;
    }


    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return mUsername;
    }

    /**
     * Sets the username.
     *
     * @param username the new username
     */
    @RequiredStringValidator(message = "Please enter your username", trim = true)
    public void setUsername(String username) {
        mUsername = username;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return mPassword;
    }

    /**
     * Sets the password.
     *
     * @param password the new password
     */
    @RequiredStringValidator(message = "Please enter your password")
    @StringLengthFieldValidator(message = "Password should be at least 6 chars long", minLength = "6")
    public void setPassword(String password) {
        mPassword = password;
    }

    /**
     * Performs the necessary checks to see if a user is allowed to get past beyond the login page.
     *
     * @return the result
     */
    public String login() {
        String result = SUCCESS;

        // make sure no one is accessing this page directly
        if (isRefererProvided(mServletRequest)) {

            try {
                PassiveCallbackHandler callbackHandler = new PassiveCallbackHandler(mUsername, mPassword);
                LoginContext loginContext = new LoginContext("DtsPortal", callbackHandler);
                loginContext.login();

                Subject subject = loginContext.getSubject();

                // use the first principal entry as this user's distinguishedName and process it to extract commonName
                // from the DN
                String distinguishedName = subject.getPrincipals(MyProxyPrincipal.class).toArray()[0].toString();
                LOGGER.debug("User with DN: '" + distinguishedName  + "' logging in.");
                String commonName = distinguishedName;
                if (distinguishedName.indexOf(CN_EQUALS) > 0) {
                    commonName = distinguishedName.substring(distinguishedName.indexOf(
                        CN_EQUALS) + CN_EQUALS.length());
                }

                // commonName will be used from now on as an 'isLoggedIn' sort of attribute
                mSessionMap.put("commonName", commonName);
                mSessionMap.put("loginContext", loginContext);


            }
            catch (LoginException le) {
                LOGGER.debug("Cannot create LoginContext. " + le.getMessage());
                mServletRequest.setAttribute("loginErrorMessage", "The myproxy details you provided might be wrong. Try again.");
                result = INPUT;
            }
            catch (SecurityException se) {
                LOGGER.debug("Cannot create LoginContext. " + se.getMessage());
                mServletRequest.setAttribute("loginErrorMessage", "The myproxy details you provided might be wrong. Try again.");
                result = INPUT;
            }

        }
        else {
            LOGGER.error("User_login.action is being accessed directly.");
            result = INPUT;
        }
        return result;
    }

    /**
     * Successfully logout a user.
     *
     * @return the result
     */
    public String logout() {
        if (mSessionMap.get("loginContext") != null) {
            LOGGER.debug("logging out loginContext");
            LoginContext loginContext = (LoginContext) mSessionMap.get("loginContext");
            // we won't be needing loginContext anymore after the last call
            try {
                loginContext.logout();
            } catch (LoginException e) {
                LOGGER.warn("Cannot logout LoginContext. " + e.getMessage());
            }
        }


        if (mSessionMap.get("commonName") != null) {
            LOGGER.info(String.format("User '%s' logging out",
                    mSessionMap.get("commonName").toString()));
        }
        if (mSessionMap instanceof SessionMap) {
            ((SessionMap) mSessionMap).invalidate();
        }
        return SUCCESS;
    }
}
