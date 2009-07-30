package org.dataminx.dts.portal;

import com.opensymphony.xwork2.ActionSupport;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.util.ServletContextAware;

/**
 * The Data Transfer Job Action class.
 *
 * @author Gerson Galang
 */
public class Job extends ActionSupport implements SessionAware, ServletRequestAware,
        ServletResponseAware, ServletContextAware {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(User.class);

    /** The job name. */
    private String mName;

    /** The job description. */
    private String mDescription;

    /** The source URI. */
    private String mSourceUri;

    /** The target URI. */
    private String mTargetUri;

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
     * Gets the name of the job.
     *
     * @return the name of the job
     */
    public String getName() {
        return mName;
    }

    /**
     * Sets the name of the job.
     *
     * @param name the name of the job
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Gets the job description.
     *
     * @return the job description
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * Sets the job description.
     *
     * @param description the job description
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    /**
     * Gets the source uri.
     *
     * @return the source uri
     */
    public String getSourceUri() {
        return mSourceUri;
    }

    /**
     * Sets the source uri.
     *
     * @param sourceUri the source uri
     */
    public void setSourceUri(String sourceUri) {
        mSourceUri = sourceUri;
    }

    /**
     * Gets the target uri.
     *
     * @return the target uri
     */
    public String getTargetUri() {
        return mTargetUri;
    }

    /**
     * Sets the target uri.
     *
     * @param targetUri the target uri
     */
    public void setTargetUri(String targetUri) {
        mTargetUri = targetUri;
    }

    /**
     * Submits the Data Transfer Job.
     *
     * @return the result
     */
    public String submit() {

        return SUCCESS;
    }

    /**
     * Performs a check when the input method is performed on this Action class.
     *
     * @return the result
     */
    public String input() {
        return checkReferer();
    }

    /**
     * Peforms a check when the back method is performed on this Action class.
     *
     * @return the result
     */
    public String back() {
        return checkReferer();
    }

    /**
     * Checks if a particular action is being accessed directly or referred by another action.
     *
     * @return the result
     */
    private String checkReferer() {
        // make sure that anyone accessing this page has successfully authenticated
        String referer = mServletRequest.getHeader("referer");
        if (referer != null) {
            return SUCCESS;
        }
        else {
            // anyone who hasn't logged in, send them to the login page
            return LOGIN;
        }
    }


}
