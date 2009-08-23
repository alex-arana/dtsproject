package org.dataminx.dts.ws.validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.JobIdentificationType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;

/**
 * The AbstractDtsJobDefinitionValidator provides implementations to the DtsJobDefinitionValidator methods and will
 * let the actual concrete class to provide implementations to {@link org.springframework.validation.Validator}'s
 * <code>validate()</code> and <code>supports()</code> methods.
 *
 * @author Gerson Galang
 */
public abstract class AbstractDtsJobDefinitionValidator implements DtsJobDefinitionValidator {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(AbstractDtsJobDefinitionValidator.class);

    /** The JobIdentificationValidator. */
    protected JobIdentificationValidator mJobIdentificationValidator;

    /** The SourceTargetValidator. */
    protected SourceTargetValidator mSourceTargetValidator;

    /**
     * Sets the JobIdentificationValidator to use by the subclasses of this abstract class.
     *
     * @param jobIdentificationValidator the JobIdentificationValidator
     */
    public void setJobIdentificationValidator(JobIdentificationValidator jobIdentificationValidator) {
        LOGGER.debug("AbstractDtsJobDefinitionValidator setJobIdentificationValidator()");
        if (jobIdentificationValidator == null) {
            throw new IllegalArgumentException("The supplied [JobIdentificationValidator] must not be null.");
        }
        if (!jobIdentificationValidator.supports(JobIdentificationType.class)) {
            throw new IllegalArgumentException(
                "The supplied [JobIdentificationValidator] "
                + "must support the validation of [JobIdentificationType] instances.");
        }
        mJobIdentificationValidator = jobIdentificationValidator;
    }

    /**
     * Sets the SourceTargetValidator to use by the subclasses of this abstract class.
     *
     * @param sourceTargetValidator the SourceTargetValidator
     */
    public void setSourceTargetValidator(SourceTargetValidator sourceTargetValidator) {
        LOGGER.debug("AbstractDtsJobDefinitionValidator setSourceTargetValidator()");
        if (sourceTargetValidator == null) {
            throw new IllegalArgumentException("The supplied [SourceTargetValidator] must not be null.");
        }
        if (!sourceTargetValidator.supports(SourceTargetType.class)) {
            throw new IllegalArgumentException(
                "The supplied [SourceTargetValidator] "
                +  "must support the validation of [SourceTargetType] instances.");
        }
        mSourceTargetValidator = sourceTargetValidator;
    }
}
