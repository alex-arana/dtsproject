package org.dataminx.dts.security.auth.module;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

public class MyProxyCredential {

    private GSSCredential mGssCredential;
    private char[] mPassword;
    private String mUsername;

    /** The logger. */
    private static final Log LOGGER = LogFactory.getLog(MyProxyCredential.class);

    private Map mCredentialMap;

    public MyProxyCredential() {
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public void setPassword(String password) {
        mPassword = password.toCharArray();
    }

    public String getPassword() {
        return String.valueOf(mPassword);
    }

    public void setGssCredential(GSSCredential gssCredential) {
        mGssCredential = gssCredential;
    }

    public GSSCredential getGssCredential() {
        return mGssCredential;
    }

    public void clearCredential() {

        if (mPassword != null) {
            for (int i = 0; i < mPassword.length; i++) {
                mPassword[i] = ' ';
            }
            mPassword = null;
        }

        if (mGssCredential != null) {
            try {
                mGssCredential.dispose();
            }
            catch (GSSException gssEx) {
                LOGGER.warn("GSSException thrown: " + gssEx.getMessage());
            }
        }
        if (mUsername != null) {
            mUsername = null;
        }
    }


}
