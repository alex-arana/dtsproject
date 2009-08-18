package org.dataminx.dts.ws;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.schemas.dts.x2009.x07.messages.AuthenticationFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.AuthorisationFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.InvalidJobDefinitionFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.JobStatusUpdateFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.NonExistentJobFaultDocument;
import org.dataminx.schemas.dts.x2009.x07.messages.TransferProtocolNotSupportedFaultDocument;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.server.endpoint.AbstractSoapFaultDefinitionExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.w3c.dom.Node;

public class DtsFaultMappingExceptionResolver extends AbstractSoapFaultDefinitionExceptionResolver {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DtsFaultMappingExceptionResolver.class);

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
        else {
            super.customizeFault(endpoint, ex, fault);
        }
    }


    @Override
    protected SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
        LOGGER.debug("DtsFaultMappingExceptionResolver getFaultDefinition()");
        if (ex instanceof AuthorisationException) {
            SoapFaultDefinition result = new SoapFaultDefinition();
            result.setFaultCode(QName.valueOf("CLIENT"));
            result.setFaultStringOrReason(ex.getMessage());
            return result;
        }
        else if (ex instanceof AuthenticationException) {
            SoapFaultDefinition result = new SoapFaultDefinition();
            result.setFaultCode(QName.valueOf("CLIENT"));
            result.setFaultStringOrReason(ex.getMessage());
            return result;
        }
        else if (ex instanceof InvalidJobDefinitionException) {
            SoapFaultDefinition result = new SoapFaultDefinition();
            result.setFaultCode(QName.valueOf("CLIENT"));
            result.setFaultStringOrReason(ex.getMessage());
            return result;
        }
        else if (ex instanceof JobStatusUpdateException) {
            SoapFaultDefinition result = new SoapFaultDefinition();
            result.setFaultCode(QName.valueOf("CLIENT"));
            result.setFaultStringOrReason(ex.getMessage());
            return result;
        }
        else if (ex instanceof NonExistentJobException) {
            SoapFaultDefinition result = new SoapFaultDefinition();
            result.setFaultCode(QName.valueOf("CLIENT"));
            result.setFaultStringOrReason(ex.getMessage());
            return result;
        }
        else if (ex instanceof TransferProtocolNotSupportedException) {
            SoapFaultDefinition result = new SoapFaultDefinition();
            result.setFaultCode(QName.valueOf("CLIENT"));
            result.setFaultStringOrReason(ex.getMessage());
            return result;
        }
        // TODO: remember to add newly defined exception to fault mapping conditions here...
        else{
            return null;
        }
    }

    private AuthorisationFaultDocument translate(AuthorisationException ex) {
        AuthorisationFaultDocument fault = AuthorisationFaultDocument.Factory.newInstance();

        // add fault specific details in here...

        return fault;
    }

    private AuthenticationFaultDocument translate(AuthenticationException ex) {
        AuthenticationFaultDocument fault = AuthenticationFaultDocument.Factory.newInstance();

        // add fault specific details in here...

        return fault;
    }

    private InvalidJobDefinitionFaultDocument translate(InvalidJobDefinitionException ex) {
        InvalidJobDefinitionFaultDocument fault = InvalidJobDefinitionFaultDocument.Factory.newInstance();

        // add fault specific details in here...

        return fault;
    }

    private JobStatusUpdateFaultDocument translate(JobStatusUpdateException ex) {
        JobStatusUpdateFaultDocument fault = JobStatusUpdateFaultDocument.Factory.newInstance();

        // add fault specific details in here...

        return fault;
    }

    private NonExistentJobFaultDocument translate(NonExistentJobException ex) {
        NonExistentJobFaultDocument fault = NonExistentJobFaultDocument.Factory.newInstance();

        // add fault specific details in here...

        return fault;
    }

    private TransferProtocolNotSupportedFaultDocument translate(TransferProtocolNotSupportedException ex) {
        TransferProtocolNotSupportedFaultDocument fault =
            TransferProtocolNotSupportedFaultDocument.Factory.newInstance();

        // add fault specific details in here...

        return fault;
    }

    private void transform(Node faultNode, SoapFault fault) {
        LOGGER.debug("DtsFaultMappingExceptionResolver transform()");
        DOMSource source = new DOMSource(faultNode);
        Transformer transformer;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.transform(source, fault.addFaultDetail().getResult());
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

}
