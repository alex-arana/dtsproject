package org.dataminx.dts.security.crypto;

public interface Encrypter {

    static final String CLEAR_TEXT_HASH = "580d331059905a59e46a09a66f55604a";

    void setSalt(byte[] salt);

    void setPassphrase(String passphrase);

    String encrypt(String string);

    String decrypt(String decrypt);

}
