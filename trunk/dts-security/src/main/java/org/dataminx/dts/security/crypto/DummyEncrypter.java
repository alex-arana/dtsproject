package org.dataminx.dts.security.crypto;

class DummyEncrypter implements Encrypter {

    public String decrypt(final String stringToDecrypt) {
        return stringToDecrypt.substring(stringToDecrypt.indexOf(":") + 1);
    }

    public String encrypt(final String stringToEncrypt) {
        return CLEAR_TEXT_HASH + ":" + stringToEncrypt;
    }

    public void setPassphrase(final String passphrase) {
        // don't do anything
    }

    public void setSalt(final byte[] salt) {
        // don't do anything
    }

}
