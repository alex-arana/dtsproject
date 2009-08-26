package org.dataminx.dts.portal;

import static org.dataminx.dts.portal.DtsAction.SOAP_FAULT_CLIENT_ERROR;
import static org.dataminx.dts.portal.DtsAction.WEB_SERVICE_IO_ERROR;
import static org.dataminx.dts.portal.util.PageValidator.isRefererProvided;
import static org.dataminx.dts.portal.util.PageValidator.isUserLoggedIn;

import com.opensymphony.xwork2.ActionSupport;
import java.io.File;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.interceptor.SessionAware;
import org.apache.struts2.util.ServletContextAware;
import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.client.sws.DataTransferServiceClient;
import org.dataminx.dts.client.sws.security.DtsWsUsernameAuthenticationCallback;
import org.dataminx.dts.security.auth.module.MyProxyCredential;
import org.dataminx.dts.ws.AuthenticationException;
import org.dataminx.dts.ws.AuthorisationException;
import org.dataminx.dts.ws.CustomException;
import org.dataminx.dts.ws.InvalidJobDefinitionException;
import org.dataminx.dts.ws.TransferProtocolNotSupportedException;
import org.dataminx.schemas.dts.x2009.x07.jsdl.CredentialType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxSourceTargetType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MyProxyTokenDocument;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MyProxyTokenType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.oasisOpen.docs.wss.x2004.x01.oasis200401WssWssecuritySecext10.UsernameTokenDocument;
import org.oasisOpen.docs.wss.x2004.x01.oasis200401WssWssecuritySecext10.UsernameTokenType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceMessageCallback;

/**
 * The Data Transfer Job Action class.
 *
 * @author Gerson Galang
 */
public class Job extends ActionSupport implements SessionAware, ServletRequestAware,
        ServletResponseAware, ServletContextAware {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(Job.class);

    /** The PasswordString's QName. */
    private static final QName PASSWORD_STRING_QNAME = new QName(
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "PasswordString");


    /** The job name. */
    private String mName;

    /** The job description. */
    private String mDescription;

    /** The source URI. */
    private String mSourceUri;

    /** The target URI. */
    private String mTargetUri;

    private String mSourceCredUsername;
    private String mSourceCredPassword;

    private String mTargetCredUsername;
    private String mTargetCredPassword;

    private DataTransferServiceClient mDtsClient;

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

    public void setDataTransferServiceClient(DataTransferServiceClient dtsClient) {
        mDtsClient = dtsClient;
    }

    /**
     * Sets the target uri.
     *
     * @param targetUri the target uri
     */
    public void setTargetUri(String targetUri) {
        mTargetUri = targetUri;
    }

    public String getSourceCredUsername() {
        return mSourceCredUsername;
    }

    public void setSourceCredUsername(String sourceCredUsername) {
        mSourceCredUsername = sourceCredUsername;
    }

    public String getSourceCredPassword() {
        return mSourceCredPassword;
    }

    public void setSourceCredPassword(String sourceCredPassword) {
        mSourceCredPassword = sourceCredPassword;
    }

    public String getTargetCredUsername() {
        return mTargetCredUsername;
    }

    public void setTargetCredUsername(String targetCredUsername) {
        mTargetCredUsername = targetCredUsername;
    }

    public String getTargetCredPassword() {
        return mTargetCredPassword;
    }

    public void setTargetCredPassword(String targetCredPassword) {
        mTargetCredPassword = targetCredPassword;
    }

    /**
     * Submits the Data Transfer Job.
     *
     * @return the result
     */
    public String submit() {
        LOGGER.debug("Job submit()");

        String result = SUCCESS;

        // make sure no one is accessing this page directly
        if (isRefererProvided(mServletRequest) && isUserLoggedIn(mSessionMap)) {
            JobDefinitionDocument dtsJob = getDtsJobTemplate();
            if (dtsJob == null) {
                result = ERROR;
            }

            //  change the name of the job template
            MinxJobDescriptionType minxJobDescription =
                (MinxJobDescriptionType) dtsJob.getJobDefinition().getJobDescription();

            minxJobDescription.getJobIdentification().setJobName(mName);

            if (mSourceCredPassword.length() > 0) {
                minxJobDescription.getDataTransferArray(0).setSource(
                    getSourceTargetType(mSourceUri, mSourceCredUsername, mSourceCredPassword));
            }
            else {
                minxJobDescription.getDataTransferArray(0).getSource().setURI(mSourceUri);
            }
            if (mTargetCredPassword.length() > 0) {
                minxJobDescription.getDataTransferArray(0).setTarget(
                    getSourceTargetType(mTargetUri, mTargetCredUsername, mTargetCredPassword));
            }
            else {
                minxJobDescription.getDataTransferArray(0).getTarget().setURI(mTargetUri);
            }

            // TODO: remove this debug line later on
            LOGGER.debug("DtsJob:\n" + dtsJob);

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

            String jobResourceKey = null;

            try {
                jobResourceKey = mDtsClient.submitJob(dtsJob);
            }
            catch (AuthenticationException e) {
                LOGGER.debug("An AuthenticationFault was thrown by the DTS Web Service. " + e.getMessage());
                mServletRequest.setAttribute("submitJobErrorMessage", e.getMessage() + " Try again.");
                result = SOAP_FAULT_CLIENT_ERROR;
            }
            catch (AuthorisationException e) {
                LOGGER.debug("An AuthorisationFault was thrown by the DTS Web Service. " + e.getMessage());
                mServletRequest.setAttribute("submitJobErrorMessage", e.getMessage() + " Try again.");
                result = SOAP_FAULT_CLIENT_ERROR;
            }
            catch (InvalidJobDefinitionException e) {
                LOGGER.debug("An InvalidJobDefinitionFault was thrown by the DTS Web Service. " + e.getMessage());
                mServletRequest.setAttribute("submitJobErrorMessage", e.getMessage() + " Try again.");
                result = SOAP_FAULT_CLIENT_ERROR;
            }
            catch (TransferProtocolNotSupportedException e) {
                LOGGER.debug("A TransferProtocolNotSupportedFault was thrown by the DTS Web Service. "
                        + e.getMessage());
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
            }

            if (jobResourceKey != null)
                mServletRequest.setAttribute("jobResourceKey", jobResourceKey);
        }
        else {
            LOGGER.error("DtsJob_submit.action is being accessed directly.");
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

    private JobDefinitionDocument getDtsJobTemplate() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            File f = new ClassPathResource("minx-dts-job-template.xml").getFile();
            return JobDefinitionDocument.Factory.parse(f);
        } catch (Exception e) {
            LOGGER.error("Error occurred while accessing the DTS Job Template: " + e.getMessage());
            return null;
        }
    }

    private UsernameTokenType getUsernameTokenTypeCredentialTemplate() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            File f = new ClassPathResource("minx-dts-username-cred-template.xml").getFile();
            return UsernameTokenDocument.Factory.parse(f).getUsernameToken();
        } catch (Exception e) {
            LOGGER.error("Error occurred while accessing the UsernameToken Template: " + e.getMessage());
            return null;
        }
    }

    private MyProxyTokenType getMyProxyTokenTypeCredentialTemplate() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            File f = new ClassPathResource("minx-dts-myproxy-cred-template.xml").getFile();
            return MyProxyTokenDocument.Factory.parse(f).getMyProxyToken();
        } catch (Exception e) {
            LOGGER.error("Error occurred while accessing the MyProxyToken Template: " + e.getMessage());
            return null;
        }
    }

    private MinxSourceTargetType getSourceTargetType(String uri, String username, String password) {
        CredentialType credential = CredentialType.Factory.newInstance();

        MinxSourceTargetType sourceTarget = MinxSourceTargetType.Factory.newInstance();
        sourceTarget.setURI(uri);


        if (uri.startsWith("gsiftp:")) {
            credential.setMyProxyToken(getMyProxyTokenTypeCredentialTemplate());
            MyProxyTokenType myProxyToken = credential.getMyProxyToken();
            myProxyToken.setMyProxyUsername(username);
            myProxyToken.setMyProxyPassword(password);

        }
        else if (uri.startsWith("ftp:")) {
            credential.setUsernameToken(getUsernameTokenTypeCredentialTemplate());
            UsernameTokenType usernameToken = credential.getUsernameToken();
            usernameToken.getUsername().setStringValue(username);
            XmlObject[] passwordString = usernameToken.selectChildren(PASSWORD_STRING_QNAME);
            passwordString[0].newCursor().setTextValue(password);
        }

        sourceTarget.setCredential(credential);

        return sourceTarget;
    }

}
