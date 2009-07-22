package org.dataminx.dts.common.util;

import javax.xml.namespace.QName;

import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxSourceTargetType;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.dataminx.dts.ws.DtsJobDefinitionException;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionDocument;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.w3c.dom.Element;

/**
 * A validator of the contents of the Job definition document. Having a valid XML document
 * doesn't necessarily mean the file has all the necessary documents for a job to successfully run.
 * This class will check for the missing information that the user needs to provide and throw an
 * Exception with the list of missing information as the message of the Exception.
 *
 * @author Gerson Galang
 */
public class JobContentValidator {

	// TODO: try and make this into a proper Validator..
	// http://static.springframework.org/spring/docs/2.5.x/reference/validation.html#validator

	private static final QName PASSWORD_STRING_QNAME =
		new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
			"PasswordString");

    /**
     * Checks for the validity of the contents of the Job Definition document.
     *
     * @param job the job definition
     * @throws DtsJobDefinitionException if at least one of the elements in the Job definition document is invalid
     */
    public void validate(JobDefinitionType job) throws DtsJobDefinitionException {
        StringBuffer errorMessages = new StringBuffer();

        if (StringUtils.isBlank(job.getJobDescription().getJobIdentification().getJobName())) {
            errorMessages.append("  Empty job name.\n");
        }
        // TODO: think of a cleaner way of returning back an error message for a number of
        // missing source/target URIs

        // make sure that MinxJobDescriptionType is used instead of just plain JobDescriptionType
        DataTransferType[] transfers;
        try {
        	transfers = ((MinxJobDescriptionType) job.getJobDescription()).getDataTransferArray();
        } catch (ClassCastException e) {
        	errorMessages.append("  Plain JobDescriptionType is not supported.");
        	throw new DtsJobDefinitionException("Invalid request1.\n" + errorMessages);
        }

        for (int i=0; i < transfers.length; i++) {

            MinxSourceTargetType source = transfers[i].getSource();
            MinxSourceTargetType target = transfers[i].getTarget();

            if (StringUtils.isBlank(source.getURI())) {
                errorMessages.append("  Empty SourceURI.\n");
            }
            if (StringUtils.isBlank(target.getURI())) {
                errorMessages.append("  Empty TargetURI.\n");
            }
            passwordExistsInUsernameToken(source, errorMessages);
            passwordExistsInUsernameToken(target, errorMessages);
        }

        if (errorMessages.length() > 0) {
            throw new DtsJobDefinitionException("Invalid request2.\n" + errorMessages);
        }
    }

    /**
     * Check for the PasswordString's existence if UsernameToken credential is used.
     *
     * @param sourceOrTarget the source or target element
     * @param errorMessages the error messages buffer
     */
    private void passwordExistsInUsernameToken(MinxSourceTargetType sourceOrTarget, StringBuffer errorMessages) {
        if (sourceOrTarget.getCredential() != null) {
            // check if credential provided is UsernameToken type
            if (sourceOrTarget.getCredential().getUsernameToken() != null) {
                // make sure password is also provided as it's only considered as the wsse schema doesn't
                // really force everyone to have a PasswordString inside the UsernameToken element
                XmlObject[] passwordString = sourceOrTarget.getCredential()
                	.getUsernameToken().selectChildren(PASSWORD_STRING_QNAME);

                if (passwordString.length > 0) {
                	// we'll assume first that there will always only be one PasswordString element
                	passwordString[0].xmlText();

                }
                else {
                    errorMessages.append("  PasswordString is missing.\n");
                }
            }
        }
    }

}
