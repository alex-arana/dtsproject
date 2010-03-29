package org.dataminx.dts.security.crypt;

class DummyEncrypter implements Encrypter {

    public String decrypt(final String stringToDecrypt) {
        return stringToDecrypt;
    }

    public String encrypt(final String stringToEncrypt) {
        return stringToEncrypt;
    }

    public void setPassphrase(final String passphrase) {
        // don't do anything
    }

    public void setSalt(final byte[] salt) {
        // don't do anything
    }

}
