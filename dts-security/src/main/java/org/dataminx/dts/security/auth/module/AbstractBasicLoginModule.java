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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dataminx.dts.security.auth.callback.PassiveCallbackHandler;

/**
 *
 * @author Gerson Galang
 */
public abstract class AbstractBasicLoginModule implements LoginModule {

    // TODO: javadoc

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(AbstractBasicLoginModule.class);

    protected CallbackHandler mCallbackHandler;
    protected Subject mSubject;
    protected Map<String, ?> mSharedState;
    protected Map<String, ?> mOptions;

    /** The authentication status. */
    protected boolean mSuccess;

    /** Temporary holder of Principal objects. */
    protected List<Principal> mTempPrincipals;

    protected List<BasicPrivateCredential> mTempCredentials;

    public AbstractBasicLoginModule() {
        LOGGER.debug("AbstractBasicLoginModule constructor");
        mSuccess = false;
        mTempPrincipals = new ArrayList<Principal>();
        mTempCredentials = new ArrayList<BasicPrivateCredential>();
    }


    @Override
    public boolean abort() throws LoginException {
        LOGGER.debug("AbstractBasicLoginModule abort()");

        mSuccess = false;
        mTempPrincipals.clear();

        if (mCallbackHandler instanceof PassiveCallbackHandler)
            ((PassiveCallbackHandler)mCallbackHandler).clearPassword();

        logout();
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        LOGGER.debug("AbstractBasicLoginModule commit()");

        if (mSuccess) {
            if (mSubject.isReadOnly()) {
                throw new LoginException ("Subject is Readonly");
            }

            try {
                if (LOGGER.isDebugEnabled()) {
                    for (Principal p : mTempPrincipals) {
                        LOGGER.debug(" - Principal: " + p);
                    }
                }

                mSubject.getPrincipals().addAll(mTempPrincipals);
                mTempPrincipals.clear();
                LOGGER.debug("see if we are allowed to edit the private credential?");
                mSubject.getPrivateCredentials().addAll(mTempCredentials);
                LOGGER.debug("yes we are allowed to edit the private credential!");
                mTempCredentials.clear();

                if(mCallbackHandler instanceof PassiveCallbackHandler)
                    ((PassiveCallbackHandler)mCallbackHandler).clearPassword();

                return true;
            } catch (Exception ex) {
                ex.printStackTrace(System.out);
                throw new LoginException(ex.getMessage());
            }
        } else {
            mTempPrincipals.clear();
            mTempCredentials.clear();
            return true;
        }
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState,
            Map<String, ?> options) {
        LOGGER.debug("AbstractBasicLoginModule initialize()");

        // save the initial state
        mCallbackHandler = callbackHandler;
        mSubject = subject;
        mSharedState = sharedState;
        mOptions = options;
    }

    @Override
    public boolean login() throws LoginException {
        LOGGER.debug("AbstractBasicLoginModule login()");

        if (mCallbackHandler == null)
            throw new LoginException("Error: no CallbackHandler available to handle" +
                    " authentication information from the user");

        try {
            // Setup default callback handlers.
            Callback[] callbacks = new Callback[] {
                new NameCallback("Username: "),
                new PasswordCallback("Password: ", false)
            };

            mCallbackHandler.handle(callbacks);

            String username = ((NameCallback)callbacks[0]).getName();
            String password = new String(((PasswordCallback)callbacks[1]).getPassword());

            ((PasswordCallback)callbacks[1]).clearPassword();

            mSuccess = authenticate(username, password);

            callbacks[0] = null;
            callbacks[1] = null;

            if (!mSuccess)
                throw new LoginException("Authentication failed: Username and/or Password does/do not match");

            return true;
        } catch (LoginException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LoginException(ex.getMessage());
        }
    }

    @Override
    public boolean logout() throws LoginException {
        LOGGER.debug("AbstractBasicLoginModule logout()");

        mTempPrincipals.clear();

        if (mCallbackHandler instanceof PassiveCallbackHandler)
            ((PassiveCallbackHandler)mCallbackHandler).clearPassword();

        return true;
    }

    protected abstract boolean authenticate(String username, String password);

}
