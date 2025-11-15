package com.quetoquenana.userservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Setter
@Getter
@ConfigurationProperties(prefix = "security.rsa")
public class RsaKeyProperties {

    private String publicKey;
    private String privateKey;

    public RSAPublicKey getPublicRsaKey() {
        try {
            byte[] der = loadKeyBytes(publicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(der);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) kf.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA public key", e);
        }
    }

    public RSAPrivateKey getPrivateRsaKey() {
        try {
            byte[] der = loadKeyBytes(privateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA private key", e);
        }
    }

    private byte[] loadKeyBytes(String value) throws IOException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException("Key value is null or empty");
        }

        String v = value.trim();

        // base64:<b64> -> decode to bytes, then handle as below
        if (v.startsWith("base64:")) {
            byte[] decoded = Base64.getDecoder().decode(v.substring("base64:".length()));
            return normalizeDecodedBytes(decoded);
        }

        // classpath:
        if (v.startsWith("classpath:")) {
            String path = v.substring("classpath:".length());
            var res = getClass().getClassLoader().getResourceAsStream(path);
            if (res == null) {
                throw new IllegalStateException("Resource not found: " + path);
            }
            byte[] bytes = res.readAllBytes();
            return normalizeDecodedBytes(bytes);
        }

        // file: or filesystem path
        if (v.startsWith("file:")) {
            String path = v.substring("file:".length());
            byte[] bytes = Files.readAllBytes(Path.of(path));
            return normalizeDecodedBytes(bytes);
        }

        // treat as a plain path
        Path p = Path.of(v);
        if (Files.exists(p)) {
            byte[] bytes = Files.readAllBytes(p);
            return normalizeDecodedBytes(bytes);
        }

        // otherwise treat as inline PEM/BASE64 text
        return normalizeDecodedBytes(v.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] normalizeDecodedBytes(byte[] input) {
        String asText = new String(input, StandardCharsets.UTF_8);

        // If PEM (contains header), extract inner base64
        if (asText.contains("-----BEGIN")) {
            String inner = asText
                    .replaceAll("(?s)-----BEGIN .*?-----", "")
                    .replaceAll("(?s)-----END .*?-----", "")
                    .replaceAll("\\s+", "");
            return Base64.getDecoder().decode(inner);
        }

        // If the content looks like base64 text (letters, digits, +/=/whitespace) -> decode it
        if (asText.trim().matches("^[A-Za-z0-9+/=\\s]+$")) {
            String cleaned = asText.replaceAll("\\s+", "");
            return Base64.getDecoder().decode(cleaned);
        }

        // Otherwise assume it's already DER binary
        return input;
    }
}