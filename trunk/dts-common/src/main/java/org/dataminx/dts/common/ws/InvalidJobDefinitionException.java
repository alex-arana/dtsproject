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
package org.dataminx.dts.common.ws;

/**
 * An exception that is thrown if the job failed to meet the minimum validity requirements.
 *
 * @author Gerson Galang
 */
public class InvalidJobDefinitionException extends DtsFaultException {

    /**
     * Constructs an instance of {@link InvalidJobDefinitionException}.
     */
    public InvalidJobDefinitionException() {
        super();
    }

    /**
     * Constructs an instance of {@link InvalidJobDefinitionException} given the specified message.
     *
     * @param msg the exception message
     */
    public InvalidJobDefinitionException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of {@link InvalidJobDefinitionException} given the specified message and cause.
     *
     * @param msg the exception message
     * @param cause the error or exception that cause this exception to be thrown
     */
    public InvalidJobDefinitionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs an instance of {@link InvalidJobDefinitionException} given the cause.
     *
     * @param cause the error or exception that cause this exception to be thrown
     */
    public InvalidJobDefinitionException(Throwable cause) {
        super(cause);
    }

}
