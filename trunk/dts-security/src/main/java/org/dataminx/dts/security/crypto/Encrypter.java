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
package org.dataminx.dts.security.crypto;


/**
 * The Encrypter interface that DTS encryption plugins need to implement if the
 * clients want to use a custom written encryption mechanism. This class will be
 * used to encrypt and decrypt the user's password credentials so they are not
 * displayed as clear text in the Message Broker and the Spring Batch Database.
 * 
 * @author Gerson Galang
 */
public interface Encrypter {

    /**
     * Sets the salt to be used by this encrypter. It is up to the implementation
     * of this Encrypter to use a salt in encrypting the provided message.
     *
     * @param salt the salt to be used by this encrypter
     */
    void setSalt(String salt);

    /**
     * Sets the passphrase to be used by this encrypter.
     * 
     * @param passphrase
     */
    void setPassphrase(String passphrase);

    /**
     * Encrypts the given string.
     * <p/>
     * The format of the encrypted string will be the hash of the algorithm used
     * to encrypt the string and the encrypted text (or clear text if Clear Text
     * algorithm is used) separated by a colon.
     * <p/>
     * The Encrypter implementation may support more than one algorithm. So it
     * is up to the decrypter implementation to figure out how the supported
     * algorithms will be decrypted.
     * 
     * @param stringToEncrypt the string to be encrypted
     * @return returns the encrypted string.
     */
    String encrypt(String stringToEncrypt);

    /**
     * Decrypt the given string.
     * 
     * @param stringToDecrypt the string to be decrypted
     * @return the decrypted string
     * @throws UnknownEncryptionAlgorithmException if the string being decrypted could not be decrypted due to an 
     *         unknown algorithm used to decrypt it
     */
    String decrypt(String stringToDecrypt)
        throws UnknownEncryptionAlgorithmException;

}
