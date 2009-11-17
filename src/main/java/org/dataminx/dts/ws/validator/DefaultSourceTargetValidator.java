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

import javax.xml.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.dataminx.schemas.dts.x2009.x07.jsdl.MinxSourceTargetType;
import org.ggf.schemas.jsdl.x2005.x11.jsdl.SourceTargetType;
import org.springframework.validation.Errors;

/**
 * The default SourceTargetValidator implementation to use.
 *
 * @author Gerson Galang
 */
public class DefaultSourceTargetValidator implements SourceTargetValidator {

    /** The PasswordString's QName. */
    private static final QName PASSWORD_STRING_QNAME = new QName(
            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "PasswordString");

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class clazz) {
        return SourceTargetType.class.isAssignableFrom(clazz);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate(Object object, Errors errors) {
        MinxSourceTargetType sourceTarget = (MinxSourceTargetType) object;

        if (StringUtils.isBlank(sourceTarget.getURI())) {
            errors.rejectValue("uri", "sourceTarget.uri.empty");
        }
        // TODO: uncomment this block once testing phase is finished
        /*if (source.getURI().startsWith("file://")) {
            errorMessages.append("  Unsupported transfer protocol on SourceURI.\n");
        }*/
        passwordExistsInUsernameToken(sourceTarget, errors);
    }

    /**
     * Check for the PasswordString's existence if UsernameToken credential is used.
     *
     * @param sourceOrTarget the source or target element
     * @param errors the validation errors
     */
    private void passwordExistsInUsernameToken(MinxSourceTargetType sourceOrTarget, Errors errors) {
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
                    errors.rejectValue("credential.usertoken.password",
                            "sourceTarget.credential.usertoken.password.empty");
                }
            }
        }
    }


}
