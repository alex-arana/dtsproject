package org.dataminx.dts.security.auth.module;

import java.io.Serializable;
import java.security.Principal;

/**
 * The MyProxyPrincipal is an implementation of @see java.security.Principal class. Having our own
 * Principal class implementation lets us remove the particular Principal object that exists
 * in an authenticated Subject.
 *
 * @author Gerson Galang
 */
public class MyProxyPrincipal implements Principal, Serializable {

    private String mName;

    public MyProxyPrincipal() {
        mName = "";
    }

    public MyProxyPrincipal(String name) {
        mName = name;
    }

    /**
     * Compares the specified Object with this <code>MyProxyPrincipal</code> for equality.
     * Returns true if the given object is also a <code>MyProxyPrincipal</code> and the two
     * MyProxyPrincipals have the same name.
     *
     * @param o Object to be compared for equality with this <code>MyProxyPrincipal</code>
     * @return true if the specified Object is equal equal to this <code>MyProxyPrincipal</code>
     */
    public boolean equals(Object o) {
        if (o == null)
            return false;

        if (this == o)
            return true;

        if (o instanceof MyProxyPrincipal) {
            if (((MyProxyPrincipal) o).getName().equals(mName))
                return true;
            else
                return false;
        }
        else
            return false;
    }

    /**
     * Return a hash code for this <code>MyProxyPrincipal</code>.
     *
     * @return a hash code for this <code>MyProxyPrincipal</code>
     */
    public int hashCode() {
        return mName.hashCode();
    }

    /**
     * Return a string representation of this <code>MyProxyPrincipal</code>.
     *
     * @return a string representation of this <code>MyProxyPrincipal</code>
     */
    public String toString() {
        return mName;
    }

    /**
     * Return the user name for this <code>MyProxyPrincipal</code>.
     *
     * @return the user name for this <code>MyProxyPrincipal</code>
     */
    @Override
    public String getName() {
        return mName;
    }
}
