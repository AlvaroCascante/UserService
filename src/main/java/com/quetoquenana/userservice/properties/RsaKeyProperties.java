package com.quetoquenana.userservice.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@ConfigurationProperties(prefix = "security.rsa")
public record RsaKeyProperties(
        RSAPublicKey publicKey,
        RSAPrivateKey privateKey,
        String keyId
) {
    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Returns the configured key-id prefix with the current year-month suffix (yyyy-MM).
     * Example: if keyId == "user-service-key-id-" and today is 2025-12-25 -> "user-service-key-id-2025-12"
     */
    public String currentKeyId() {
        return keyId + LocalDate.now().format(YEAR_MONTH);
    }
}