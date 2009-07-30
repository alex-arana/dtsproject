package org.dataminx.dts.portal;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validation;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.dispatcher.SessionMap;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.util.ServletContextAware;
import org.dataminx.dts.common.DtsConfigManager;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

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

    /** The DTS Configuration. */
    private Configuration mDtsConfig;

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
     * Sets the dts config manager.
     *
     * @param dtsConfigManager the new dts config manager
     */
    public void setDtsConfigManager(DtsConfigManager dtsConfigManager) {
        mDtsConfig = dtsConfigManager.getDtsConfig();
    }

    /**
     * Performs the necessary checks to see if a user is allowed to get past beyond the login page.
     *
     * @return the result
     */
    public String login() {
        String result = SUCCESS;

        // make sure no one is accessing this page directly
        String referer = mServletRequest.getHeader("referer");
        if (referer != null) {

            // TODO: there might be a better way of having these fields be accessed from a static/constant
            // class somewhere
            String myProxyHost = mDtsConfig.getString("default.myproxy.host");
            int myProxyPort = mDtsConfig.getInt("default.myproxy.port");
            int myProxyCredentialLifetime = mDtsConfig.getInt("default.myproxy.lifetime");

            MyProxy myProxy = new MyProxy(myProxyHost, myProxyPort);
            GSSCredential credential = null;
            String commonName = null;
            try {
                credential = myProxy.get(mUsername, mPassword, myProxyCredentialLifetime);
                LOGGER.info(String.format("Successfully downloaded a proxy credential from myproxy server, '%s:%s'\n",
                        myProxyHost,
                        myProxyPort));

                String distinguishedName = credential.getName().toString();
                commonName = distinguishedName.substring(distinguishedName.indexOf(
                    CN_EQUALS) + CN_EQUALS.length());

                // commonName will be used from now on as an 'isLoggedIn' sort of attribute
                mSessionMap.put("commonName", commonName);
                LOGGER.info(String.format("User '%s' logging in", commonName));
                // TODO: check if disposing this credential will still let us download a proxy credential later on

            }
            catch (MyProxyException ex) {
                LOGGER.error(String.format("Could not get delegated proxy from server '%s:%s'\n%s",
                    myProxyHost,
                    myProxyPort,
                    ex.getMessage()));
                mSessionMap.put("loginErrorMessage", "The myproxy details you provided might be wrong. Try again.");
                result = INPUT;
            }
            catch (GSSException ex) {
                // let's ignore this exception
                LOGGER.error(String.format("Couldn't perform GSSCredential method calls.\n%s", ex.getMessage()));
                mSessionMap.put("commonName", commonName);
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
