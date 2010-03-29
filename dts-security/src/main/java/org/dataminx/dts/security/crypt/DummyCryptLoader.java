package org.dataminx.dts.security.crypt;

public class DummyCryptLoader implements CryptLoader {

    private final Encrypter mEncrypter;

    public DummyCryptLoader() {
        mEncrypter = new DummyEncrypter();
    }

    public Encrypter getEncrypter() {
        return mEncrypter;
    }

}
