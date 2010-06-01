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
package org.dataminx.dts.security.auth.callback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PassiveCallbackHandler is used if username and password can't be provided from within the handle() method.
 *
 * @author Gerson Galang
 */

public class PassiveCallbackHandler implements CallbackHandler {

    /** The logger. */
    private static final Log LOGGER = LogFactory
        .getLog(PassiveCallbackHandler.class);

    /** The username. */
    private final String mUsername;

    /** The password. */
    private char[] mPassword;

    /**
     * Creates a callback handler given the username and password.
     *
     * @param user the username
     * @param password the password
     */
    public PassiveCallbackHandler(final String user, final String password) {
        LOGGER.debug("PassiveCallbackHandler constructor");
        mUsername = user;
        mPassword = password.toCharArray();
    }

    /**
     * {@inheritDoc}
     */
    public void handle(final Callback[] callbacks) throws java.io.IOException,
        UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                ((NameCallback) callbacks[i]).setName(mUsername);
            }
            else if (callbacks[i] instanceof PasswordCallback) {
                ((PasswordCallback) callbacks[i]).setPassword(mPassword);
            }
            else {
                throw new UnsupportedCallbackException(callbacks[i],
                    "Callback class not supported");
            }
        }
    }

    /**
     * Clears out password state.
     */
    public void clearPassword() {
        if (mPassword != null) {
            for (int i = 0; i < mPassword.length; i++) {
                mPassword[i] = ' ';
            }
            mPassword = null;
        }
    }
}
