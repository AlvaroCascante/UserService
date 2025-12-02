package com.quetoquenana.userservice.config;

import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;

class RsaKeyPropertiesTest {

    @Test
    void loadKeysFromClasspath_shouldParseKeys() {
        RsaKeyProperties props = new RsaKeyProperties();
        props.setPublicKey("classpath:keys/user_service_public_key.pem");
        props.setPrivateKey("classpath:keys/user_service_private_key.pem");

        PublicKey pub = props.getPublicRsaKey();
        PrivateKey priv = props.getPrivateRsaKey();

        assertThat(pub).isNotNull();
        assertThat(priv).isNotNull();
    }
}

