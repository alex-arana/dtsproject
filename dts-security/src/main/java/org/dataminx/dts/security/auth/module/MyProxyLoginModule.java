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
import org.globus.myproxy.MyProxy;
import org.globus.myproxy.MyProxyException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class MyProxyLoginModule implements LoginModule {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(MyProxyLoginModule.class);

    private CallbackHandler mCallbackHandler;
    private Subject mSubject;
    private Map<String, ?> mSharedState;
    private Map<String, ?> mOptions;

    private boolean mDebug;

    /** The myproxy server. */
    private String mMyProxyHost;

    /** The myproxy port. */
    private int mMyProxyPort;

    /** The myproxy lifetime. */
    private int mMyProxyLifetime;

    /** The authentication status. */
    private boolean mSuccess;

    /** Temporary holder of Principal objects. */
    private List<MyProxyPrincipal> mTempPrincipals;

    private List<MyProxyCredential> mTempCredentials;

    public MyProxyLoginModule() {
        LOGGER.debug("MyProxyLoginModule constructor");
        mSuccess = false;
        mTempPrincipals = new ArrayList<MyProxyPrincipal>();
        mTempCredentials = new ArrayList<MyProxyCredential>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean abort() throws LoginException {
        LOGGER.debug("MyProxyLoginModule abort()");

        mSuccess = false;
        mTempPrincipals.clear();

        if (mCallbackHandler instanceof PassiveCallbackHandler)
            ((PassiveCallbackHandler)mCallbackHandler).clearPassword();

        logout();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean commit() throws LoginException {
        LOGGER.debug("MyProxyLoginModule commit()");

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
            Map<String, ?> sharedState, Map<String, ?> options) {
        LOGGER.debug("MyProxyLoginModule initialize()");

        // save the initial state
        mCallbackHandler = callbackHandler;
        mSubject = subject;
        mSharedState = sharedState;
        mOptions = options;

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
    public boolean login() throws LoginException {
        LOGGER.debug("MyProxyLoginModule login()");

        if (mCallbackHandler == null)
            throw new LoginException("Error: no CallbackHandler available to handle" +
                    " authentication information from the user");

        try {
            // Setup default callback handlers.
            Callback[] callbacks = new Callback[] {
                new NameCallback("MyProxy Username: "),
                new PasswordCallback("MyProxy Password: ", false)
            };

            mCallbackHandler.handle(callbacks);

            String username = ((NameCallback)callbacks[0]).getName();
            String password = new String(((PasswordCallback)callbacks[1]).getPassword());

            ((PasswordCallback)callbacks[1]).clearPassword();

            mSuccess = myproxyAuthenticate(username, password);

            callbacks[0] = null;
            callbacks[1] = null;

            if (!mSuccess)
                throw new LoginException("MyProxy Authentication failed: Username and/or Password does/do not match");

            return true;
        } catch (LoginException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new LoginException(ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean logout() throws LoginException {
        LOGGER.debug("MyProxyLoginModule logout()");

        mTempPrincipals.clear();

        if (mCallbackHandler instanceof PassiveCallbackHandler)
            ((PassiveCallbackHandler)mCallbackHandler).clearPassword();

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

    private boolean myproxyAuthenticate(String username, String password) {

        MyProxy myProxy = new MyProxy(mMyProxyHost, mMyProxyPort);
        GSSCredential credential = null;
        String commonName = null;
        boolean hasSuccessfullyAuthenticated = false;
        try {
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
