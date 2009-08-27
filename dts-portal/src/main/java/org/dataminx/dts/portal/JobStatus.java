package org.dataminx.dts.portal;

import static org.dataminx.dts.portal.DtsAction.SOAP_FAULT_CLIENT_ERROR;
import static org.dataminx.dts.portal.DtsAction.WEB_SERVICE_IO_ERROR;
import static org.dataminx.dts.portal.util.PageValidator.isRefererProvided;
import static org.dataminx.dts.portal.util.PageValidator.isUserLoggedIn;

import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.validator.annotations.Validation;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.util.ServletContextAware;
import org.dataminx.dts.client.sws.DataTransferServiceClient;
import org.dataminx.dts.client.sws.security.DtsWsUsernameAuthenticationCallback;
import org.dataminx.dts.security.auth.module.MyProxyCredential;
import org.dataminx.dts.ws.AuthenticationException;
import org.dataminx.dts.ws.CustomException;
import org.dataminx.dts.ws.NonExistentJobException;
import org.springframework.util.Assert;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceMessageCallback;

@Validation()
public class JobStatus extends ActionSupport implements SessionAware, ServletRequestAware,
        ServletResponseAware, ServletContextAware {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(Job.class);

    private DataTransferServiceClient mDtsClient;

    /** The auto injected HTTP session object. */
    private Map mSessionMap;

    /** The auto injected servlet request object. */
    private HttpServletRequest mServletRequest;

    /** The auto injected servlet response object. */
    private HttpServletResponse mServletResponse;

    /** The auto injected servlet context object. */
    private ServletContext mServletContext;

    private String mJobResourceKey;

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

    public void setDataTransferServiceClient(DataTransferServiceClient dtsClient) {
        mDtsClient = dtsClient;
    }

    public String getJobResourceKey() {
        return mJobResourceKey;
    }

    //@RequiredStringValidator(message = "Please enter a job id", trim = true)
    public void setJobResourceKey(String jobResourceKey) {
        mJobResourceKey = jobResourceKey;
    }

    public String getDetails() {
        LOGGER.debug("Job getDetails()");
        String result = SUCCESS;

        // make sure no one is accessing this page directly
        if (isRefererProvided(mServletRequest) && isUserLoggedIn(mSessionMap)) {

            LoginContext loginContext = (LoginContext) mSessionMap.get("loginContext");
            Subject subject = loginContext.getSubject();

            MyProxyCredential myProxyCredential = (MyProxyCredential)
                subject.getPrivateCredentials(MyProxyCredential.class).toArray()[0];

            String username = myProxyCredential.getUsername();
            String password = myProxyCredential.getPassword();

            // then submit the job...
            WebServiceMessageCallback wsMessageCallback = new DtsWsUsernameAuthenticationCallback(
                    username, password);
            mDtsClient.setWebServiceMessageCallback(wsMessageCallback);

            // done with loginContext and its attributes, nullify the references to them
            loginContext = null;
            subject = null;
            myProxyCredential = null;
            username = null;
            password = null;

            String jobStatus = null;

            try {
                Assert.notNull(mJobResourceKey);
                jobStatus = mDtsClient.getJobStatus(mJobResourceKey);
            }
            catch (AuthenticationException e) {
                LOGGER.debug("An AuthenticationFault was thrown by the DTS Web Service. " + e.getMessage());
                mServletRequest.setAttribute("submitJobErrorMessage", e.getMessage() + " Try again.");
                result = SOAP_FAULT_CLIENT_ERROR;
            }
            catch (NonExistentJobException e) {
                LOGGER.debug("A NonExistentFault was thrown by the DTS Web Service. " + e.getMessage());
                mServletRequest.setAttribute("submitJobErrorMessage", e.getMessage() + " Try again.");
                result = SOAP_FAULT_CLIENT_ERROR;
            }
            catch (CustomException e) {
                LOGGER.debug("A CustomFault was thrown by the DTS Web Service. " + e.getMessage());
                mServletRequest.setAttribute("submitJobErrorMessage", e.getMessage() + " Try again.");
                result = SOAP_FAULT_CLIENT_ERROR;
            }
            catch (WebServiceIOException e) {
                LOGGER.debug("A WebServiceIOException was thrown by the DTS Web Service. " + e.getMessage());
                mServletRequest.setAttribute("submitJobErrorMessage", e.getMessage());
                result = WEB_SERVICE_IO_ERROR;
                throw e;
            }

            if (jobStatus != null) {
                mServletRequest.setAttribute("jobStatus", jobStatus);
                mServletRequest.setAttribute("jobResourceKey", mJobResourceKey);
            }

        }
        else {
            LOGGER.error("DtsJobStatus_getDetails.action is being accessed directly.");
            result = INPUT;
        }
        return result;
    }

    /**
     * Performs a check when the input method is performed on this Action class.
     *
     * @return the result
     */
    public String input() {
        return checkPageRequirements();
    }

    /**
     * Peforms a check when the back method is performed on this Action class.
     *
     * @return the result
     */
    public String back() {
        return checkPageRequirements();
    }

    /**
     * Checks if a particular action is being accessed directly or referred by another action AND if the session hasn't
     * expired yet.
     *
     * @return the result
     */
    private String checkPageRequirements() {
        // make sure that anyone accessing this page has successfully authenticated
        if (isRefererProvided(mServletRequest) && isUserLoggedIn(mSessionMap)) {
            return SUCCESS;
        }
        else {
            // anyone who hasn't logged in, send them to the login page
            return LOGIN;
        }
    }

}
