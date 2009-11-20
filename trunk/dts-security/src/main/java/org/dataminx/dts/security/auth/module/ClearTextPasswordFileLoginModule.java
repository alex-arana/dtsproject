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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Gerson Galang
 */
public class ClearTextPasswordFileLoginModule extends AbstractBasicLoginModule {

    // TODO: javadoc

    private static final Log LOGGER = LogFactory.getLog(ClearTextPasswordFileLoginModule.class);

    private long mLastModified = 0;
    private Map<String, String> mUsers = null;
    private String mPasswordFile = null;

    private void load(File file) throws LoginException {
        //System.out.println("Reading " + f);
        mLastModified = file.lastModified();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));

            mUsers = new HashMap<String, String>();
            String line = reader.readLine();

            String username = "";
            String password = "";

            while (line != null) {
                line = line.trim();
                if (line.indexOf("#") != 0 && line.indexOf(":") > 1) {
                    username = line.substring(0, line.indexOf(":"));
                    password = line.substring(line.indexOf(":") + 1);
                    mUsers.put(username, password);
                }
                line = reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            throw new LoginException(e.getMessage());
        }
    }

    private void reload() throws LoginException {
        File file = new File(mPasswordFile);
        if (mUsers == null || file.lastModified() != mLastModified) {
           load(file);
        }
    }

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        LOGGER.debug("ClearTextPasswordFileLoginModule initialize()");

        super.initialize(subject, callbackHandler, sharedState, options);

        mPasswordFile = (String) options.get("password.file");
        LOGGER.debug(" - password.file: " + mPasswordFile);
    }

    @Override
    public boolean logout() throws LoginException {
        super.logout();

        // remove the Principals the login module added
        for (PasswordFilePrincipal p : mSubject.getPrincipals(PasswordFilePrincipal.class)) {
            mSubject.getPrincipals().remove(p);
        }

        // remove the MyProxyCredentials the login module added
        for (BasicPrivateCredential c :
                mSubject.getPrivateCredentials(BasicPrivateCredential.class)) {
            c.clearCredential();
            mSubject.getPrivateCredentials().remove(c);
        }

        return true;
    }

    protected boolean authenticate(String username, String password) {
        try {
            reload();
            if (mUsers.get(username) != null && mUsers.get(username).equals(password)) {
                mTempPrincipals.add(new PasswordFilePrincipal(username));
                BasicPrivateCredential credential = new BasicPrivateCredential();
                credential.setUsername(username);
                credential.setPassword(password);
                mTempCredentials.add(credential);
                return true;
            }
        } catch (LoginException e) {
            LOGGER.error("LoginException was thrown in ClearTextPasswordFileLoginModule.authenticate()"
                    + e.getMessage());
        }
        return false;
    }

}
