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
package org.dataminx.dts.ws.client;

import org.dataminx.dts.common.ws.AuthenticationException;
import org.dataminx.dts.common.ws.AuthorisationException;
import org.dataminx.dts.common.ws.CustomException;
import org.dataminx.dts.common.ws.InvalidJobDefinitionException;
import org.dataminx.dts.common.ws.JobStatusUpdateException;
import org.dataminx.dts.common.ws.NonExistentJobException;
import org.dataminx.dts.common.ws.TransferProtocolNotSupportedException;

import java.io.IOException;
import java.util.Date;
import javax.xml.transform.dom.DOMSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.Assert;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.FaultMessageResolver;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DtsFaultMessageResolver implements FaultMessageResolver {
    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DtsFaultMessageResolver.class);

    private static final String AUTHENTICATION_FAULT = "AuthenticationFault";
    private static final String AUTHORISATION_FAULT = "AuthorisationFault";
    private static final String INVALID_JOB_DEFINITION_FAULT = "InvalidJobDefinitionFault";
    private static final String TRANSFER_PROTOCOL_NOT_SUPPORTED_FAULT = "TransferProtocolNotSupportedFault";
    private static final String NON_EXISTENT_JOB_FAULT = "NonExistentJobFault";
    private static final String JOB_STATUS_UPDATE_FAULT = "JobStatusUpdateFault";
    private static final String CUSTOM_FAULT = "CustomFault";
    private static final String MESSAGE = "Message";
    private static final String TIMESTAMP = "Timestamp";

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";

    private static final String DTS_MESSAGES_NS = "http://schemas.dataminx.org/dts/2009/07/messages";

    public void resolveFault(WebServiceMessage message) throws IOException {
        LOGGER.debug("DtsFaultMessageResolver resolveFault()");
        SoapMessage soapMessage = (SoapMessage) message;
        SoapFault soapFault = soapMessage.getSoapBody().getFault();

        // there are times when the Fault thrown by the web service doesn't contain any fault details. in these
        // occassions, we'll just have to log the basic info we could get from a SoapFault.
        if (soapFault.getFaultDetail() == null) {
            LOGGER.error("A SoapFault was thrown by the DTS Web Service which doesn't contain a Fault Detail. "
                    + "Throwing a CustomException. The SoapFault's StringOrReason: \n"
                    + soapFault.getFaultStringOrReason());
            throw new CustomException(soapFault.getFaultStringOrReason());
        }
        // else we'll map them to known exceptions
        else {
            DOMSource detailSource = (DOMSource) soapFault.getFaultDetail().getSource();
            Node detailNode = detailSource.getNode();
            NodeList childNodes = detailNode.getChildNodes();

            // need to make sure that all the SoapFault detail always exists
            Assert.isTrue(childNodes.getLength() > 0, "SoapFault.detail must not be empty");
            //Assert.assertTrue("SoapFault.detail is not empty", childNodes.getLength() > 0);

            for (int i=0; i < childNodes.getLength(); i++) {
                Node currentNode = childNodes.item(i);
                if (currentNode.getLocalName().equals(AUTHENTICATION_FAULT) &&
                        currentNode.getNamespaceURI().equals(DTS_MESSAGES_NS)) {
                    AuthenticationException e = new AuthenticationException(getMessage(currentNode));
                    e.setTimestamp(getTimeStamp(currentNode));
                    throw e;
                }
                if (currentNode.getLocalName().equals(AUTHORISATION_FAULT) &&
                        currentNode.getNamespaceURI().equals(DTS_MESSAGES_NS)) {
                    AuthorisationException e = new AuthorisationException(getMessage(currentNode));
                    e.setTimestamp(getTimeStamp(currentNode));
                    throw e;
                }
                if (currentNode.getLocalName().equals(INVALID_JOB_DEFINITION_FAULT) &&
                        currentNode.getNamespaceURI().equals(DTS_MESSAGES_NS)) {
                    InvalidJobDefinitionException e = new InvalidJobDefinitionException(getMessage(currentNode));
                    e.setTimestamp(getTimeStamp(currentNode));
                    throw e;
                }
                if (currentNode.getLocalName().equals(TRANSFER_PROTOCOL_NOT_SUPPORTED_FAULT) &&
                        currentNode.getNamespaceURI().equals(DTS_MESSAGES_NS)) {
                    TransferProtocolNotSupportedException e = new TransferProtocolNotSupportedException(
                            getMessage(currentNode));
                    e.setTimestamp(getTimeStamp(currentNode));
                    throw e;
                }
                if (currentNode.getLocalName().equals(NON_EXISTENT_JOB_FAULT) &&
                        currentNode.getNamespaceURI().equals(DTS_MESSAGES_NS)) {
                    NonExistentJobException e = new NonExistentJobException(getMessage(currentNode));
                    e.setTimestamp(getTimeStamp(currentNode));
                    throw e;
                }
                if (currentNode.getLocalName().equals(JOB_STATUS_UPDATE_FAULT) &&
                        currentNode.getNamespaceURI().equals(DTS_MESSAGES_NS)) {
                    JobStatusUpdateException e = new JobStatusUpdateException(getMessage(currentNode));
                    e.setTimestamp(getTimeStamp(currentNode));
                    throw e;
                }
                if (currentNode.getLocalName().equals(CUSTOM_FAULT) &&
                        currentNode.getNamespaceURI().equals(DTS_MESSAGES_NS)) {
                    CustomException e = new CustomException(getMessage(currentNode));
                    e.setTimestamp(getTimeStamp(currentNode));
                    throw e;
                }
            }
        }
    }

    /**
     * Get the Message element's value from the a child instance of the DtsFault.
     *
     * @param dtsFaultNode a subclass of a DtsFault
     * @return the Message element's value from the a child instance of the DtsFault
     */
    private String getMessage(Node dtsFaultNode) {
        NodeList childNodes = dtsFaultNode.getChildNodes();
        for (int i=0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);
            if (currentNode.getLocalName().equals(MESSAGE) &&
                    currentNode.getNamespaceURI().equals(DTS_MESSAGES_NS)) {
                return currentNode.getTextContent();
            }
        }
        return "";
    }

    /**
     * Get the Timestamp element's value from the child instance of DtsFault.
     *
     * @param dtsFaultNode a subclass of a DtsFault
     * @return the Timestamp element's value from the a child instance of the DtsFault
     */
    private Date getTimeStamp(Node dtsFaultNode) {
        NodeList childNodes = dtsFaultNode.getChildNodes();
        for (int i=0; i < childNodes.getLength(); i++) {
            Node currentNode = childNodes.item(i);
            if (currentNode.getLocalName().equals(TIMESTAMP) &&
                    currentNode.getNamespaceURI().equals(DTS_MESSAGES_NS)) {
                LOGGER.debug("converting timestamp to date: " + currentNode.getTextContent());
                DateTimeFormatter fmt = DateTimeFormat.forPattern(DATE_PATTERN);
                DateTime dt = fmt.parseDateTime(currentNode.getTextContent());
                return dt.toDate();
            }
        }
        return new Date();
    }

}
