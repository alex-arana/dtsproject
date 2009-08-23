package org.dataminx.dts.ws.validator;

import org.springframework.validation.Validator;

/**
 * The DtsJobDefinitionValidator Interface. This will be used to force all its sub interfaces and implementations
 * to use JobIdentification and SourceTarget Validators.
 *
 * @author Gerson Galang
 */
public interface DtsJobDefinitionValidator extends Validator {

    /**
     * Sets the job identification validator.
     *
     * @param jobIdentificationValidator the new job identification validator
     */
    void setJobIdentificationValidator(JobIdentificationValidator jobIdentificationValidator);

    /**
     * Sets the source target validator.
     *
     * @param sourceTargetValidator the new source target validator
     */
    void setSourceTargetValidator(SourceTargetValidator sourceTargetValidator);

}
