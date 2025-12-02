package com.quetoquenana.userservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;

@Setter
@Getter
@ConfigurationProperties(prefix = "security.rsa")
public class RsaKeyProperties {

    private String publicKey;
    private String privateKey;

    public PrivateKey getPrivateRsaKey() {
        try {
            byte[] keyBytes = normalizeAndDecode(privateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA private key", e);
        }
    }

    public PublicKey getPublicRsaKey() {
        try {
            byte[] keyBytes = normalizeAndDecode(publicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse RSA public key", e);
        }
    }

    private static final Pattern NON_BASE64 = Pattern.compile("[^A-Za-z0-9+/=\\r\\n]\\+");

    private byte[] normalizeAndDecode(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("RSA key property is empty");
        }

        String value = key.trim();

        // support railway-style prefix or similar: "base64:..."
        if (value.startsWith("base64:")) {
            value = value.substring("base64:".length()).trim();
            return safeBase64Decode(value);
        }

        // support classpath resources: classpath:keys/...
        if (value.startsWith("classpath:")) {
            String path = value.substring("classpath:".length());
            if (path.startsWith("/")) path = path.substring(1);
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is == null) {
                    throw new IllegalStateException("Resource not found on classpath: " + path);
                }
                byte[] data = is.readAllBytes();
                String s = new String(data);
                if (s.contains("-----BEGIN")) {
                    // PEM file on classpath
                    String inner = s.replaceAll("-----BEGIN [A-Z0-9 ]+-----", "").replaceAll("-----END [A-Z0-9 ]+-----", "").replaceAll("\\s+", "");
                    return safeBase64Decode(inner);
                } else {
                    // assume DER bytes
                    return data;
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read classpath resource: " + path, e);
            }
        }

        // support file: URIs
        if (value.startsWith("file:")) {
            String path = value.substring("file:".length());
            try {
                byte[] data = Files.readAllBytes(Paths.get(path));
                String s = new String(data);
                if (s.contains("-----BEGIN")) {
                    String inner = s.replaceAll("-----BEGIN [A-Z0-9 ]+-----", "").replaceAll("-----END [A-Z0-9 ]+-----", "").replaceAll("\\s+", "");
                    return safeBase64Decode(inner);
                } else {
                    return data;
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to read file: " + path, e);
            }
        }

        // If full PEM content provided directly, strip headers and decode
        if (value.contains("-----BEGIN")) {
            String inner = value
                    .replaceAll("-----BEGIN [A-Z ]+-----", "")
                    .replaceAll("-----END [A-Z ]+-----", "")
                    .replaceAll("\\s+", "");
            return safeBase64Decode(inner);
        }

        // otherwise assume it's raw base64 (single-line) or tolerant base64
        return safeBase64Decode(value);
    }

    private byte[] safeBase64Decode(String value) {
        if (value == null) throw new IllegalArgumentException("empty base64");
        String v = value.trim();
        // remove characters that are not valid base64 (except = padding)
        v = v.replaceAll("[^A-Za-z0-9+/=]", "");

        IllegalArgumentException last = null;
        try {
            return Base64.getMimeDecoder().decode(v);
        } catch (IllegalArgumentException e) {
            last = e;
        }
        try {
            return Base64.getUrlDecoder().decode(v);
        } catch (IllegalArgumentException e) {
            last = e;
        }
        try {
            return Base64.getDecoder().decode(v);
        } catch (IllegalArgumentException e) {
            last = e;
        }
        throw new IllegalStateException("Failed to base64-decode RSA key (len=" + v.length() + ")", last);
    }
}