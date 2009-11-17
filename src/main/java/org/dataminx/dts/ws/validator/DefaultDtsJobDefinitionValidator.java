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
        LOGGER.debug("DefaultDtsJobDefinitionValidator validate()");
        // now let's check for semantic issues..
        // assume we require the following fields to be filled up in the job definition document
        //   * jobname - can't be an empty string
        //   * uri - can't be an empty string
        //mJobValidator.validate(submitJobRequest.getSubmitJobRequest().getJobDefinition());

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
