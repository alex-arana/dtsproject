package org.dataminx.dts.security.crypto;

public class DummyCryptoLoader implements CryptoLoader {

    private final Encrypter mEncrypter;

    public DummyCryptoLoader() {
        mEncrypter = new DummyEncrypter();
    }

    public Encrypter getEncrypter() {
        return mEncrypter;
    }

}
