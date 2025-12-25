package com.quetoquenana.userservice.properties;

import com.quetoquenana.userservice.util.KeyUtils;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Component
public class RsaKeyConverter {

    @Bean
    @ConfigurationPropertiesBinding
    Converter<String, RSAPrivateKey> privateKeyConverter() {
        return KeyUtils::parsePrivateKey;
    }

    @Bean
    @ConfigurationPropertiesBinding
    Converter<String, RSAPublicKey> publicKeyConverter() {
        return KeyUtils::parsePublicKey;
    }
}