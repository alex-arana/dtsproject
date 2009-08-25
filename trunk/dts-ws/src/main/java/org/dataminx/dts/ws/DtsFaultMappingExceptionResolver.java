package org.dataminx.dts.ws;

import java.util.Calendar;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class DtsFaultMappingExceptionResolver extends AbstractSoapFaultDefinitionExceptionResolver {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DtsFaultMappingExceptionResolver.class);

    /**
     * Customises the {@link SoapFault} by adding exception specific details into the SoapFault's detail element.
     *
     * @param endpoint the executed endpoint, or null if none chosen at the time of the exception
     * @param ex the exception to be handled
     * @param fault the created fault
     */
    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
        if (ex instanceof AuthorisationException) {
            AuthorisationException exception = (AuthorisationException) ex;
            AuthorisationFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof AuthenticationException) {
            AuthenticationException exception = (AuthenticationException) ex;
            AuthenticationFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof InvalidJobDefinitionException) {
            InvalidJobDefinitionException exception = (InvalidJobDefinitionException) ex;
            InvalidJobDefinitionFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof JobStatusUpdateException) {
            AuthenticationException exception = (AuthenticationException) ex;
            AuthenticationFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof NonExistentJobException) {
            NonExistentJobException exception = (NonExistentJobException) ex;
            NonExistentJobFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof TransferProtocolNotSupportedException) {
            TransferProtocolNotSupportedException exception = (TransferProtocolNotSupportedException) ex;
            TransferProtocolNotSupportedFaultDocument faultDocument = translate(exception);
            transform(faultDocument.getDomNode(), fault);
        }
        else if (ex instanceof CustomException) {
            CustomException exception = (CustomException) ex;
            CustomFaultDocument faultDocument = translate(exception);
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
    protected SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
        LOGGER.debug("DtsFaultMappingExceptionResolver getFaultDefinition()");
        // TODO: remember to add newly defined exception to fault mapping conditions here...
        if (ex instanceof AuthorisationException
                || ex instanceof AuthenticationException
                || ex instanceof InvalidJobDefinitionException
                || ex instanceof JobStatusUpdateException
                || ex instanceof NonExistentJobException
                || ex instanceof TransferProtocolNotSupportedException
                || ex instanceof CustomException) {
            SoapFaultDefinition result = new SoapFaultDefinition();
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
    private AuthorisationFaultDocument translate(AuthorisationException ex) {
        AuthorisationFaultDocument fault = AuthorisationFaultDocument.Factory.newInstance();
        Calendar timestamp = Calendar.getInstance();
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
    private AuthenticationFaultDocument translate(AuthenticationException ex) {
        AuthenticationFaultDocument fault = AuthenticationFaultDocument.Factory.newInstance();
        Calendar timestamp = Calendar.getInstance();
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
    private InvalidJobDefinitionFaultDocument translate(InvalidJobDefinitionException ex) {
        InvalidJobDefinitionFaultDocument fault = InvalidJobDefinitionFaultDocument.Factory.newInstance();
        Calendar timestamp = Calendar.getInstance();
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
    private JobStatusUpdateFaultDocument translate(JobStatusUpdateException ex) {
        JobStatusUpdateFaultDocument fault = JobStatusUpdateFaultDocument.Factory.newInstance();
        Calendar timestamp = Calendar.getInstance();
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
    private NonExistentJobFaultDocument translate(NonExistentJobException ex) {
        NonExistentJobFaultDocument fault = NonExistentJobFaultDocument.Factory.newInstance();
        Calendar timestamp = Calendar.getInstance();
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
    private TransferProtocolNotSupportedFaultDocument translate(TransferProtocolNotSupportedException ex) {
        TransferProtocolNotSupportedFaultDocument fault =
            TransferProtocolNotSupportedFaultDocument.Factory.newInstance();
        Calendar timestamp = Calendar.getInstance();
        timestamp.setTime(ex.getTimestamp());
        fault.addNewTransferProtocolNotSupportedFault().setMessage(ex.getMessage());
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
    private CustomFaultDocument translate(CustomException ex) {
        CustomFaultDocument fault =
            CustomFaultDocument.Factory.newInstance();
        Calendar timestamp = Calendar.getInstance();
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
    private void transform(Node faultNode, SoapFault fault) {
        LOGGER.debug("DtsFaultMappingExceptionResolver transform()");
        DOMSource source = new DOMSource(faultNode);
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, fault.addFaultDetail().getResult());
        }
        catch (TransformerException e) {
            LOGGER.error(e.getMessage());
        }
    }

}
