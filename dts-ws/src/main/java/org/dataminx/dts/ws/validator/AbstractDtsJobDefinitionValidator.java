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
