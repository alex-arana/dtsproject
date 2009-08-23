package org.dataminx.dts.ws.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.schemas.dts.x2009.x07.jsdl.DataTransferType;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxJobDescriptionType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobDefinitionType;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * The default DtsJobDefinitionValidator implementation.
 *
 * @author Gerson Galang
 */
public class DefaultDtsJobDefinitionValidator extends AbstractDtsJobDefinitionValidator
        implements DtsJobDefinitionValidator {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DefaultDtsJobDefinitionValidator.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class clazz) {
        return JobDefinitionType.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Object object, Errors errors) {

        JobDefinitionType jobDefinition = (JobDefinitionType) object;

        // hand over the validation work to the respective DtsJob element validators..
        try {
            errors.pushNestedPath("jobIdentification");
            ValidationUtils.invokeValidator(mJobIdentificationValidator,
                    jobDefinition.getJobDescription().getJobIdentification(), errors);
        }
        finally {
            errors.popNestedPath();
        }

        DataTransferType[] transfers = null;
        try {
            transfers = ((MinxJobDescriptionType) jobDefinition.getJobDescription()).getDataTransferArray();
        }
        catch (ClassCastException e) {
            // TODO: test this one
            errors.rejectValue("jobDefinition.jobDescription", "jobDefinition.jobDescription.wrongType");

            // no need to wait to validate the rest of the Job as there's no way to do that anyway if we
            // have the wrong JobDescription type
            return;
        }

        try {
            errors.pushNestedPath("sourceTarget");
            for (int i = 0; transfers != null && i < transfers.length; i++) {
                ValidationUtils.invokeValidator(mSourceTargetValidator,
                        transfers[i].getSource(), errors);
                ValidationUtils.invokeValidator(mSourceTargetValidator,
                        transfers[i].getTarget(), errors);
            }
        }
        finally {
            errors.popNestedPath();
        }
    }

}
