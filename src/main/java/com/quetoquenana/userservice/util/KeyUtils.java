package com.quetoquenana.userservice.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static com.quetoquenana.userservice.util.Constants.JWTClaims.KEY_FACTORY_ALGORITHM;

public final class KeyUtils {

    private KeyUtils() {}

    public static RSAPrivateKey parsePrivateKey(String base64Der) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Der);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            PrivateKey key = keyFactory.generatePrivate(keySpec);
            return (RSAPrivateKey) key;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse RSA private key", ex);
        }
    }

    public static RSAPublicKey parsePublicKey(String base64Der) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Der);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
            PublicKey key = keyFactory.generatePublic(keySpec);
            return (RSAPublicKey) key;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse RSA public key", ex);
        }
    }
}