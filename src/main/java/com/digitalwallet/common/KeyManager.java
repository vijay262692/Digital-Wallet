package com.digitalwallet.common;

import org.springframework.stereotype.Component;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

@Component
public class KeyManager {
    private final KeyPair keyPair;

    public KeyManager() throws NoSuchAlgorithmException {
        this.keyPair = CryptoUtil.generateKeyPair();
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
}
