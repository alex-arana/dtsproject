package org.dataminx.dts.common.util;

import org.dataminx.dts.ws.DtsJobDefinitionException;
import org.dataminx.schemas.dts._2009._05.dts.DataTransferType;
import org.dataminx.schemas.dts._2009._05.dts.JobDefinitionType;

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
            if (transfer.getSource().getURI().trim().equals("")) {
                errorMessages.append("  Empty SourceURI.\n");
            }
            if (transfer.getTarget().getURI().trim().equals("")) {
                errorMessages.append("  Empty TargetURI.\n");
            }
        }

        // TODO: check for PasswordString existence if UsernameToken is provided


        if (errorMessages.length() > 0) {
            throw new DtsJobDefinitionException("Invalid request.\n" + errorMessages);
        }
    }

}
