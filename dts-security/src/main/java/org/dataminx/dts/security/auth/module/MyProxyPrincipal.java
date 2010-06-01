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

    /** The username of the MyProxyPrincipal. */
    private final String mName;

    /**
     * The default MyProxyPrincipal which uses an empty string as the username for the
     * MyProxyPrincipal object.
     */
    public MyProxyPrincipal() {
        mName = "";
    }

    /**
     * The MyProxyPrincipal constructor that takes in a username to be used by this object.
     *
     * @param name the username of the MyProxyPrincipal
     */
    public MyProxyPrincipal(final String name) {
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
    @Override
    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        }

        if (this == o) {
            return true;
        }

        if (o instanceof MyProxyPrincipal) {
            return ((MyProxyPrincipal) o).getName().equals(mName);
        }
        else {
            return false;
        }
    }

    /**
     * Return a hash code for this <code>MyProxyPrincipal</code>.
     *
     * @return a hash code for this <code>MyProxyPrincipal</code>
     */
    @Override
    public int hashCode() {
        return mName.hashCode();
    }

    /**
     * Return a string representation of this <code>MyProxyPrincipal</code>.
     *
     * @return a string representation of this <code>MyProxyPrincipal</code>
     */
    @Override
    public String toString() {
        return mName;
    }

    /**
     * Return the user name for this <code>MyProxyPrincipal</code>.
     *
     * @return the user name for this <code>MyProxyPrincipal</code>
     */
    public String getName() {
        return mName;
    }
}
