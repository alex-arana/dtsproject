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
package org.dataminx.dts.ws.client.security;

import java.io.IOException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.TransformerException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

/**
 * The DtsWsUsernameAuthenticationCallback adds a SOAP header to the web service message containing a UsernameToken
 * needed to properly authenticate to the web service.
 *
 * @author Gerson Galang
 */
public class DtsWsUsernameAuthenticationCallback implements WebServiceMessageCallback {

    /** The WS Security schema prefix. */
    private static final String WSSE_PREFIX = "wsse";

    /** The WS Security schema namespace. */
    private static final String WSSE_NAMESPACE =
        "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";

    /** Username. */
    private String mUsername;

    /** Password. */
    private String mPassword;

    /**
     * Default constructor.
     */
    public DtsWsUsernameAuthenticationCallback() {

    }

    /**
     * Constructor that sets the username and password to be used for the SOAP security header.
     *
     * @param username the username
     * @param password the password
     */
    public DtsWsUsernameAuthenticationCallback(String username, String password) {
        mUsername = username;
        mPassword = password;
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
    public void setPassword(String password) {
        mPassword = password;
    }



    /**
     * Add authentication details in the SOAP header of the message.
     *
     * @param message the webservice message
     * @throws IOException in case I/O errors occur
     * @throws TransformerException in case transformation errors occur
     */
    public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {

        if (mUsername == null && mPassword == null) {
            throw new IllegalStateException("Username and/or Password is not set.");
        }

        try {
            // Assumption: We are using the default SAAJWebMessageFactory
            SaajSoapMessage    saajSoapMessage = (SaajSoapMessage) message;
            SOAPMessage soapMessage = saajSoapMessage.getSaajMessage();

            SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope();
            SOAPHeader soapHeader = soapEnvelope.getHeader();

            Name headerElementName = soapEnvelope.createName(
                "Security",
                WSSE_PREFIX,
                WSSE_NAMESPACE);

            // Add "Security" soapHeaderElement to soapHeader
            SOAPHeaderElement soapHeaderElement = soapHeader.addHeaderElement(headerElementName);

            // Add usernameToken to "Security" soapHeaderElement
            SOAPElement usernameTokenSOAPElement = soapHeaderElement.addChildElement(
                "UsernameToken",
                WSSE_PREFIX,
                WSSE_NAMESPACE);

            // Add username to usernameToken
            SOAPElement userNameSOAPElement = usernameTokenSOAPElement.addChildElement(
                "Username",
                WSSE_PREFIX,
                WSSE_NAMESPACE);

            userNameSOAPElement.addTextNode(mUsername);

            // Add password to usernameToken
            SOAPElement passwordSOAPElement = usernameTokenSOAPElement.addChildElement(
                "Password",
                WSSE_PREFIX,
                WSSE_NAMESPACE);

            passwordSOAPElement.addTextNode(mPassword);

        }
        catch (SOAPException soapException) {
            throw new RuntimeException("WSSESecurityHeaderRequestWebServiceMessageCallback", soapException);
        }
    }
}
