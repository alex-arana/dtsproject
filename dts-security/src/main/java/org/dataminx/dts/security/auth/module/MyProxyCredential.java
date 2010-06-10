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
package org.dataminx.dts.security.auth.module;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 * This class represents the MyProxy credential which is basically a GSSCredential. It extends
 * from the BasicPrivateCredential so username and password to access the credential from the
 * MyProxy repository can be done by providing the username and password.
 *
 * @author Gerson Galang
 */
public class MyProxyCredential extends BasicPrivateCredential {

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(MyProxyCredential.class);

    /** A reference to the GSS Credential. */
    private GSSCredential mGssCredential;

    /**
     * MyProxyCredential default constructor.
     */
    public MyProxyCredential() {
    }

    /**
     * Sets the GSS Credential for this object.
     *
     * @param gssCredential the GSS Credential
     */
    public void setGssCredential(final GSSCredential gssCredential) {
        mGssCredential = gssCredential;
    }

    public GSSCredential getGssCredential() {
        return mGssCredential;
    }

    /**
     * Removes the GSS Credential from the memory.
     */
    @Override
    public void clearCredential() {
        super.clearCredential();

        if (mGssCredential != null) {
            try {
                mGssCredential.dispose();
            }
            catch (final GSSException gssEx) {
                LOGGER.warn("GSSException thrown: " + gssEx.getMessage());
            }
        }
    }

}
