package org.dataminx.dts.ws.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobIdentificationType;
import org.springframework.validation.Errors;

/**
 * The default JobIdentificationValidator implementation.
 *
 * @author Gerson Galang
 */
public class DefaultJobIdentificationValidator implements JobIdentificationValidator {
    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(DefaultJobIdentificationValidator.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class clazz) {
        return JobIdentificationType.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Object object, Errors errors) {
        //ValidationUtils.rejectIfEmpty(errors, "jobName", "jobIdentification.jobName.empty");
        JobIdentificationType jobIdentificationType = (JobIdentificationType) object;
        if (jobIdentificationType.getJobName().trim().equals("")) {
            errors.rejectValue("jobName", "jobIdentification.jobName.empty");
        }
    }

}
