package com.quetoquenana.userservice.config;

import com.quetoquenana.userservice.properties.RsaKeyProperties;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;

class RsaKeyPropertiesTest {

    @Test
    void constructWithGeneratedKeys_shouldHoldRsaKeys() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
        RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();

        RsaKeyProperties props = new RsaKeyProperties(pub, priv, "test-key-id-");

        assertThat(props.publicKey()).isNotNull();
        assertThat(props.privateKey()).isNotNull();
        assertThat(props.publicKey()).isInstanceOf(RSAPublicKey.class);
        assertThat(props.privateKey()).isInstanceOf(RSAPrivateKey.class);
    }
}

