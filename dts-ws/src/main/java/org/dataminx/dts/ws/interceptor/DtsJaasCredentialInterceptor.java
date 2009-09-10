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
package org.dataminx.dts.ws.interceptor;

import java.util.Iterator;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.security.auth.callback.PassiveCallbackHandler;
import org.dataminx.dts.ws.AuthenticationException;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

/**
 * The DtsCredentialInterceptor is a custom interceptor which will handle the credentials provided by the user. A need
 * for this interceptor arised when the <code>org.springframework.ws.soap.security.xwss.XwsSecurityInterceptor</code>
 * did not provide a way of handing the <code>loginContext</code> instantiated from within the
 * <code>org.springframework.ws.soap.security.xwss.callback.jaas.JaasPlainTextPasswordValidationCallbackHandler</code>.
 *
 * @author Gerson Galang
 */
public class DtsJaasCredentialInterceptor implements EndpointInterceptor {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DtsJaasCredentialInterceptor.class);

    /** The WS Security schema prefix. */
    private static final String WSSE_PREFIX = "wsse";

    /** The WS Security schema namespace. */
    private static final String WSSE_NAMESPACE =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    /** The Constant WSSE_SECURITY_QNAME. */
    private static final QName WSSE_SECURITY_QNAME = new QName(WSSE_NAMESPACE, "Security", WSSE_PREFIX);

    /** The Constant WSSE_USERNAME_TOKEN_QNAME. */
    private static final QName WSSE_USERNAME_TOKEN_QNAME = new QName(WSSE_NAMESPACE, "UsernameToken", WSSE_PREFIX);

    /** The Constant WSSE_USERNAME_QNAME. */
    private static final QName WSSE_USERNAME_QNAME = new QName(WSSE_NAMESPACE, "Username", WSSE_PREFIX);

    /** The Constant WSSE_PASSWORD_QNAME. */
    private static final QName WSSE_PASSWORD_QNAME = new QName(WSSE_NAMESPACE, "Password", WSSE_PREFIX);

    /** The m login context name. */
    private String mLoginContextName;

    /** The login context. */
    private LoginContext mLoginContext;

    /**
     * Logs out the {@link LoginContext} if it exists.
     *
     * @param messageContext contains both request and response messages
     * @param endpoint the chosen endpoint to invoke
     *
     * @return true to continue processing of the request interceptor chain; false to indicate blocking of the
     * request endpoint chain, without invoking the endpoint
     *
     * @throws Exception in case error occurs
     */
    public boolean handleFault(MessageContext messageContext, Object endpoint) throws Exception {
        if (mLoginContext != null) {
            mLoginContext.logout();
        }
        return true;
    }


    /**
     * This method will instantiate the {@link LoginContext} and store the {@link Subject} in the
     * <code>HTTPSession</code> <code>subject</code> attribute.
     *
     * @param messageContext contains both request and response messages
     * @param endpoint the chosen endpoint to invoke
     *
     * @return true to continue processing of the request interceptor chain; false to indicate blocking of the
     * request endpoint chain, without invoking the endpoint
     *
     * @throws Exception in case error occurs
     */
    public boolean handleRequest(MessageContext messageContext, Object endpoint) throws Exception {
        LOGGER.debug("DtsJaasCredentialInterceptor handleRequest()");
        try {
            SaajSoapMessage    saajSoapMessage = (SaajSoapMessage) messageContext.getRequest();
            SOAPMessage soapMessage = saajSoapMessage.getSaajMessage();

            SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
            SOAPHeader soapHeader = soapEnvelope.getHeader();

            String username = "";
            String password = "";

            for (Iterator<SOAPHeaderElement>
                soapHeaderElementIter = soapHeader.examineAllHeaderElements(); soapHeaderElementIter.hasNext();) {
                SOAPHeaderElement soapHeaderElement = soapHeaderElementIter.next();
                if (soapHeaderElement.getElementQName().equals(WSSE_SECURITY_QNAME)) {
                    for (Iterator<SOAPElement> usernameTokenIter =
                            soapHeaderElement.getChildElements(WSSE_USERNAME_TOKEN_QNAME);
                            usernameTokenIter.hasNext();) {
                        SOAPElement usernameTokenElement = usernameTokenIter.next();

                        for (Iterator<SOAPElement> usernameTokenChildrenIter =
                                usernameTokenElement.getChildElements();
                                usernameTokenChildrenIter.hasNext();) {

                            SOAPElement usernameTokenChild = usernameTokenChildrenIter.next();
                            if (usernameTokenChild.getElementQName().equals(WSSE_USERNAME_QNAME)) {
                                username = usernameTokenChild.getValue();
                            }

                            if (usernameTokenChild.getElementQName().equals(WSSE_PASSWORD_QNAME)) {
                                password = usernameTokenChild.getValue();
                            }
                        }
                    }
                }
            }

            PassiveCallbackHandler callbackHandler = new PassiveCallbackHandler(username, password);
            mLoginContext = new LoginContext(mLoginContextName, callbackHandler);
            mLoginContext.login();

            Subject subject = mLoginContext.getSubject();

            LOGGER.info("Storing a 'subject' property in the HTTPSession subject attribute");
            TransportContext txContext = TransportContextHolder.getTransportContext();
            HttpServletConnection connection = (HttpServletConnection) txContext.getConnection();
            HttpServletRequest request = connection.getHttpServletRequest();
            request.getSession().setAttribute("subject", subject);

            // doing a logout here will nullify the subject (which means, the endpoint will have no access to it).
            // so do the logout in the handleResponse or handleFault instead
            //mLoginContext.logout();
        }
        catch (LoginException le) {
            LOGGER.debug("Cannot create LoginContext. " + le.getMessage());
            throw new AuthenticationException(le.getMessage());
        }
        catch (SecurityException se) {
            LOGGER.debug("Cannot create LoginContext. " + se.getMessage());
            throw new AuthenticationException(se.getMessage());
        }
        return true;
    }

    /**
     * Logs out the {@link LoginContext} if it exists.
     *
     * @param messageContext contains both request and response messages
     * @param endpoint the chosen endpoint to invoke
     *
     * @return true only
     *
     * @throws Exception in case error occurs
     */
    public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
        if (mLoginContext != null) {
            mLoginContext.logout();
        }
        return true;
    }


    /**
     * Sets the login context name.
     *
     * @param loginContextName the new login context name
     */
    public void setLoginContextName(String loginContextName) {
        mLoginContextName = loginContextName;
    }
}
