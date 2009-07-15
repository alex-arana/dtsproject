package org.dataminx.dts.common.util;

import org.dataminx.dts.ws.DtsJobDefinitionException;
import org.dataminx.schemas.dts._2009._05.dts.DataTransferType;
import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;
import org.dataminx.schemas.dts._2009._05.dts.SourceTargetType;
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

    /**
     * Checks for the validity of the contents of the Job Definition document.
     *
     * @param job the job definition
     * @throws DtsJobDefinitionException if at least one of the elements in the Job definition document is invalid
     */
    public void validate(JobDefinitionType job) throws DtsJobDefinitionException {
        StringBuffer errorMessages = new StringBuffer();

        if (job.getJobDescription().getJobIdentification().getJobName().trim().equals("")) {
            errorMessages.append("  Empty job name.\n");
        }
        // TODO: think of a cleaner way of returning back an error message for a number of
        // missing source/target URIs
        for (DataTransferType transfer : job.getJobDescription().getDataTransfer()) {
            SourceTargetType source = transfer.getSource();
            SourceTargetType target = transfer.getTarget();

            if (source.getURI().trim().equals("")) {
                errorMessages.append("  Empty SourceURI.\n");
            }
            if (target.getURI().trim().equals("")) {
                errorMessages.append("  Empty TargetURI.\n");
            }
            passwordExistsInUsernameToken(source, errorMessages);
            passwordExistsInUsernameToken(target, errorMessages);
        }

        if (errorMessages.length() > 0) {
            throw new DtsJobDefinitionException("Invalid request.\n" + errorMessages);
        }
    }

    /**
     * Check for the PasswordString's existence if UsernameToken credential is used.
     *
     * @param sourceOrTarget the source or target element
     * @param errorMessages the error messages buffer
     */
    private void passwordExistsInUsernameToken(SourceTargetType sourceOrTarget, StringBuffer errorMessages) {
        if (sourceOrTarget.getCredential() != null) {
            // check if credential provided is UsernameToken type
            if (sourceOrTarget.getCredential().getUsernameToken() != null) {
                // make sure password is also provided as it's only considered as the wsse schema doesn't
                // really force everyone to have a PasswordString inside the UsernameToken element
                boolean passwordFound = false;
                for (Object element : sourceOrTarget.getCredential().getUsernameToken().getAny()) {
                    // just in case there are other elements within a UsernameToken, ignore them
                    // unless it's a PasswordString
                    if (((Element) element).getLocalName().equals("PasswordString")) {
                        passwordFound = true;
                    }
                }
                if (!passwordFound) {
                    errorMessages.append("  PasswordString is missing.\n");
                }
            }
        }
    }

}
