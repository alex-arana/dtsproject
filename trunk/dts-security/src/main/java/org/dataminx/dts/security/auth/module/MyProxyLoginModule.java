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

import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

/**
 *
 * @author Gerson Galang
 */
public class MyProxyLoginModule extends AbstractBasicLoginModule {
    // TODO: javadoc

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(MyProxyLoginModule.class);


    private boolean mDebug;

    /** The myproxy server. */
    private String mMyProxyHost;

    /** The myproxy port. */
    private int mMyProxyPort;

    /** The myproxy lifetime. */
    private int mMyProxyLifetime;


    public MyProxyLoginModule() {
        super();
        LOGGER.debug("MyProxyLoginModule constructor");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        LOGGER.debug("MyProxyLoginModule initialize()");

        super.initialize(subject, callbackHandler, sharedState, options);

        mMyProxyHost = (String) options.get("myproxy.host");
        LOGGER.debug(" - myproxy.host: " + mMyProxyHost);

        mMyProxyPort = Integer.parseInt((String) options.get("myproxy.port"));
        LOGGER.debug(" - myproxy.port: " + mMyProxyPort);

        mMyProxyLifetime = Integer.parseInt((String) options.get("myproxy.lifetime"));
        LOGGER.debug(" - myproxy.lifetime: " + mMyProxyLifetime);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean logout() throws LoginException {
        LOGGER.debug("MyProxyLoginModule logout()");

        super.logout();

        // remove the Principals the login module added
        for (MyProxyPrincipal p : mSubject.getPrincipals(MyProxyPrincipal.class)) {
            LOGGER.debug("removing " + p);
            mSubject.getPrincipals().remove(p);
        }

        // remove the MyProxyCredentials the login module added
        for (MyProxyCredential c : mSubject.getPrivateCredentials(MyProxyCredential.class)) {
            LOGGER.debug("removing myproxy credential");
            c.clearCredential();
            mSubject.getPrivateCredentials().remove(c);
        }

        return true;
    }

    protected boolean authenticate(String username, String password) {
        LOGGER.debug("MyProxyLoginModule myproxyAuthenticate");
        MyProxy myProxy = new MyProxy(mMyProxyHost, mMyProxyPort);
        GSSCredential credential = null;
        String commonName = null;
        boolean hasSuccessfullyAuthenticated = false;
        try {
            // TODO: remove this debug line later on
            //LOGGER.debug("username: " + username + "; password: " + password);
            credential = myProxy.get(username, password, mMyProxyLifetime);
            LOGGER.info(String.format("Successfully downloaded a proxy credential from myproxy server, '%s:%s'\n",
                    mMyProxyHost,
                    mMyProxyPort));

            MyProxyCredential myProxyCredential = new MyProxyCredential();
            myProxyCredential.setUsername(username);
            myProxyCredential.setPassword(password);
            myProxyCredential.setGssCredential(credential);

            // we'll not worry about authorisation for now since that's one complicated thing that needs thinking of.
            // just work on authentication first.

            // at this point, we know that we've successfully authenticated. get the subject's details (ie DN)
            String distinguishedName = credential.getName().toString();

            mTempPrincipals.add(new MyProxyPrincipal(distinguishedName));
            LOGGER.info(String.format("User '%s' successfully logged in", distinguishedName));

            // TODO: figure out how I can control access to the private credential
            // use jaas.policy perhaps?
            mTempCredentials.add(myProxyCredential);

            // TODO: check if disposing this credential will still let us download a proxy credential later on
            hasSuccessfullyAuthenticated = true;

        }
        catch (MyProxyException ex) {
            LOGGER.error(String.format("Could not get delegated proxy for '%s' from server '%s:%s'\n%s",
                username,
                mMyProxyHost,
                mMyProxyPort,
                ex.getMessage()));
        }
        catch (GSSException ex) {
            // let's ignore this exception
            LOGGER.warn(String.format("Couldn't perform GSSCredential method calls.\n%s", ex.getMessage()));
            mTempPrincipals.add(new MyProxyPrincipal("Unknown User"));
        }

        return hasSuccessfullyAuthenticated;
    }
}
