/**
 * Copyright (c) 2010, VeRSI Consortium
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
package org.dataminx.dts.common.util;

//import org.dataminx.schemas.dts.x2009.x07.jsdl.CredentialType;

import org.proposal.dmi.schemas.dts.x2010.dmiCommon.CredentialType;


/**
 * The CredentialStore will be the container for real credentials that users
 * might want to persist (or not) in a database. Think of the credential store
 * as a Map where each credential is referenced by a key, the credential key.
 * There has always been an issue with persisting the user's credential details
 * on the database and having the credentials stored in memory or in database
 * through the CredentialStore container will resolve that issue.
 *
 * @author Gerson Galang
 */
public interface CredentialStore {

    /**
     * Save the credential in memory.
     *
     * @param credUUID the credential key
     * @param credential the credential
     */
    void writeToMemory(String credUUID, CredentialType credential);

    /**
     * Save the credential in the database so restarting of failed jobs would
     * be possible.
     *
     * @param credUUID the credential key
     * @param credential the credential
     */
    void writeToDatabase(String credUUID, CredentialType credential);

    /**
     * Gets the credential.
     *
     * @param credUUID the credential key
     * @return the credential
     */
    CredentialType getCredential(String credUUID);

}
