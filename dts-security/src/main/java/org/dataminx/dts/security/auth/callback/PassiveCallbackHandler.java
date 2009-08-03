package org.dataminx.dts.security.auth.callback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * PassiveCallbackHandler is used if username and password can't be provided from within the handle() method
 *
 * @author Gerson Galang
 */

public class PassiveCallbackHandler implements CallbackHandler {

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(PassiveCallbackHandler.class);

    private String mUsername;
    char[] mPassword;

    /**
     * Creates a callback handler given the username and password.
     *
     * @param user the username
     * @param password the password
     */
    public PassiveCallbackHandler(String user, String password) {
        LOGGER.debug("PassiveCallbackHandler constructor");
        mUsername = user;
        mPassword = password.toCharArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(Callback[] callbacks)
        throws java.io.IOException, UnsupportedCallbackException  {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                ((NameCallback)callbacks[i]).setName(mUsername);
            } else if (callbacks[i] instanceof PasswordCallback) {
                ((PasswordCallback)callbacks[i]).setPassword(mPassword);
            } else {
                throw new UnsupportedCallbackException(
                            callbacks[i], "Callback class not supported");
            }
        }
    }

    /**
     * Clears out password state.
     */
    public void clearPassword() {
        if (mPassword != null) {
            for (int i = 0; i < mPassword.length; i++)
                mPassword[i] = ' ';
            mPassword = null;
        }
    }
}
