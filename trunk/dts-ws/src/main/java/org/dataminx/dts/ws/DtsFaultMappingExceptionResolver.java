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
package org.dataminx.dts.ws;

import java.util.Calendar;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.common.ws.AuthenticationException;
import org.dataminx.dts.common.ws.AuthorisationException;
import org.dataminx.dts.common.ws.CustomException;
import org.dataminx.dts.common.ws.InvalidJobDefinitionException;
import org.dataminx.dts.common.ws.JobStatusUpdateException;
import org.dataminx.dts.common.ws.NonExistentJobException;
import org.dataminx.dts.common.ws.TransferProtocolNotSupportedException;
import org.dataminx.schemas.dts.x2009.x07.messages.AuthenticationFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.AuthorisationFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.CustomFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.InvalidJobDefinitionFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.JobStatusUpdateFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.NonExistentJobFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.TransferProtocolNotSupportedFaultDocument;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.server.endpoint.AbstractSoapFaultDefinitionExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.w3c.dom.Node;

/**
 * The DtsFaultMappingExceptionResolver will resolve DTS exceptions thrown by the Data Transfer Service to the DTS
 * SOAPFaults defined in the minx-dts-messages schema.
 *
 * @author Gerson Galang
 */
public class DtsFaultMappingExceptionResolver extends
    AbstractSoapFaultDefinitionExceptionResolver {

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(DtsFaultMappingExceptionResolver.class);

    /**
     * Customises the {@link SoapFault} by adding exception specific details into the SoapFault's detail element.
     *
     * @param endpoint the executed endpoint, or null if none chosen at the time of the exception
     * @param ex the exception to be handled
     * @param fault the created fault
     */
    @Override
    protected void customizeFault(final Object endpoint, final Exception ex,
        final SoapFault fault) {
        if (ex instanceof AuthorisationException) {
            final AuthorisationException exception = (AuthorisationException) ex;
            final AuthorisationFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof AuthenticationException) {
            final AuthenticationException exception = (AuthenticationException) ex;
            final AuthenticationFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof InvalidJobDefinitionException) {
            final InvalidJobDefinitionException exception = (InvalidJobDefinitionException) ex;
            final InvalidJobDefinitionFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof JobStatusUpdateException) {
            final JobStatusUpdateException exception = (JobStatusUpdateException) ex;
            final JobStatusUpdateFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof NonExistentJobException) {
            final NonExistentJobException exception = (NonExistentJobException) ex;
            final NonExistentJobFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof TransferProtocolNotSupportedException) {
            final TransferProtocolNotSupportedException exception = (TransferProtocolNotSupportedException) ex;
            final TransferProtocolNotSupportedFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof CustomException) {
            final CustomException exception = (CustomException) ex;
            final CustomFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else {
            super.customizeFault(endpoint, ex, fault);
        }
    }

    /**
     * Template method that returns the {@link SoapFaultDefinition} for the given exception.
     *
     * @param endpoint the executed endpoint, or null if none chosen at the time of the exception
     * @param ex the exception to be handled
     *
     * @return the definition mapped to the exception, or null if none is found.
     */
    @Override
    protected SoapFaultDefinition getFaultDefinition(final Object endpoint,
        final Exception ex) {
        LOGGER.debug("DtsFaultMappingExceptionResolver getFaultDefinition()");
        // TODO: remember to add newly defined exception to fault mapping conditions here...
        if (ex instanceof AuthorisationException
            || ex instanceof AuthenticationException
            || ex instanceof InvalidJobDefinitionException
            || ex instanceof JobStatusUpdateException
            || ex instanceof NonExistentJobException
            || ex instanceof TransferProtocolNotSupportedException
            || ex instanceof CustomException) {
            final SoapFaultDefinition result = new SoapFaultDefinition();
            result.setFaultCode(QName.valueOf("CLIENT"));
            result.setFaultStringOrReason(ex.getMessage());
            return result;
        }
        else {
            return null;
        }
    }

    /**
     * Translates the {@link AuthorisationException} into a {@link AuthorisationFaultDocument}.
     *
     * @param ex the AuthorisationException
     *
     * @return the AuthorisationFaultDocument
     */
    private AuthorisationFaultDocument translate(final AuthorisationException ex) {
        final AuthorisationFaultDocument fault = AuthorisationFaultDocument.Factory
            .newInstance();
        final Calendar timestamp = Calendar.getInstance();
        timestamp.setTime(ex.getTimestamp());
        fault.addNewAuthorisationFault().setMessage(ex.getMessage());
        fault.getAuthorisationFault().setTimestamp(timestamp);
        return fault;
    }

    /**
     * Translates an {@link AuthenticationException} to an {@link AuthenticationFaultDocument}.
     *
     * @param ex the AuthenticationException
     *
     * @return the AuthenticationFaultDocument
     */
    private AuthenticationFaultDocument translate(
        final AuthenticationException ex) {
        final AuthenticationFaultDocument fault = AuthenticationFaultDocument.Factory
            .newInstance();
        final Calendar timestamp = Calendar.getInstance();
        timestamp.setTime(ex.getTimestamp());
        fault.addNewAuthenticationFault().setMessage(ex.getMessage());
        fault.getAuthenticationFault().setTimestamp(timestamp);
        return fault;
    }

    /**
     * Translates an {@link InvalidJobDefinitionException} to a {@link InvalidJobDefinitionFaultDocument}.
     *
     * @param ex the InvalidJobDefinitionException
     *
     * @return the InvalidJobDefinitionFaultDocument
     */
    private InvalidJobDefinitionFaultDocument translate(
        final InvalidJobDefinitionException ex) {
        final InvalidJobDefinitionFaultDocument fault = InvalidJobDefinitionFaultDocument.Factory
            .newInstance();
        final Calendar timestamp = Calendar.getInstance();
        timestamp.setTime(ex.getTimestamp());
        fault.addNewInvalidJobDefinitionFault().setMessage(ex.getMessage());
        fault.getInvalidJobDefinitionFault().setTimestamp(timestamp);
        return fault;
    }

    /**
     * Translates {@link JobStatusUpdateException} to a {@link JobStatusUpdateFaultDocument}.
     *
     * @param ex the JobStatusUpdateException
     *
     * @return the JobStatusUpdateFaultDocument
     */
    private JobStatusUpdateFaultDocument translate(
        final JobStatusUpdateException ex) {
        final JobStatusUpdateFaultDocument fault = JobStatusUpdateFaultDocument.Factory
            .newInstance();
        final Calendar timestamp = Calendar.getInstance();
        timestamp.setTime(ex.getTimestamp());
        fault.addNewJobStatusUpdateFault().setMessage(ex.getMessage());
        fault.getJobStatusUpdateFault().setTimestamp(timestamp);
        return fault;
    }

    /**
     * Translates {@link NonExistentJobException} to a {@link NonExistentJobFaultDocument}.
     *
     * @param ex the NonExistentJobException
     *
     * @return the NonExistentJobFaultDocument
     */
    private NonExistentJobFaultDocument translate(
        final NonExistentJobException ex) {
        final NonExistentJobFaultDocument fault = NonExistentJobFaultDocument.Factory
            .newInstance();
        final Calendar timestamp = Calendar.getInstance();
        timestamp.setTime(ex.getTimestamp());
        fault.addNewNonExistentJobFault().setMessage(ex.getMessage());
        fault.getNonExistentJobFault().setTimestamp(timestamp);
        return fault;
    }

    /**
     * Translates the {@link TransferProtocolNotSupportedException} to a
     * {@link TransferProtocolNotSupportedFaultDocument}.
     *
     * @param ex the TransferProtocolNotSupportedException
     *
     * @return the TransferProtocolNotSupportedFaultDocument
     */
    private TransferProtocolNotSupportedFaultDocument translate(
        final TransferProtocolNotSupportedException ex) {
        final TransferProtocolNotSupportedFaultDocument fault = TransferProtocolNotSupportedFaultDocument.Factory
            .newInstance();
        final Calendar timestamp = Calendar.getInstance();
        timestamp.setTime(ex.getTimestamp());
        fault.addNewTransferProtocolNotSupportedFault().setMessage(
            ex.getMessage());
        fault.getTransferProtocolNotSupportedFault().setTimestamp(timestamp);
        return fault;
    }

    /**
     * Translates the {@link CustomException} to a
     * {@link CustomFaultDocument}.
     *
     * @param ex the CustomException
     *
     * @return the CustomFaultDocument
     */
    private CustomFaultDocument translate(final CustomException ex) {
        final CustomFaultDocument fault = CustomFaultDocument.Factory
            .newInstance();
        final Calendar timestamp = Calendar.getInstance();
        timestamp.setTime(ex.getTimestamp());
        fault.addNewCustomFault().setMessage(ex.getMessage());
        fault.getCustomFault().setTimestamp(timestamp);
        return fault;
    }

    /**
     * Transforms fault {@link Source} into a {@link Result} object.
     *
     * @param faultNode the {@link Node} representation of the FaultDocument
     * @param fault the {@link SoapFault} to be transformed
     */
    private void transform(final Node faultNode, final SoapFault fault) {
        LOGGER.debug("DtsFaultMappingExceptionResolver transform()");
        final DOMSource source = new DOMSource(faultNode);
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, fault.addFaultDetail().getResult());
        }
        catch (final TransformerException e) {
            LOGGER.error(e.getMessage());
        }
    }

}
