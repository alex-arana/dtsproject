package org.dataminx.dts.security.crypt;

public interface Encrypter {

    void setSalt(byte[] salt);

    void setPassphrase(String passphrase);

    String encrypt(String string);

    String decrypt(String decrypt);

}
