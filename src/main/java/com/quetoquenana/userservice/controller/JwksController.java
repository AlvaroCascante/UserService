package com.quetoquenana.userservice.controller;

import com.quetoquenana.userservice.config.RsaKeyProperties;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/jwks")
public class JwksController {

    private final RsaKeyProperties rsaKeys;

    public JwksController(RsaKeyProperties rsaKeys) {
        this.rsaKeys = rsaKeys;
    }

    @GetMapping(path = "/.well-known/jwks.json", produces = "application/json")
    public ResponseEntity<Map<String, Object>> keys() {
        RSAPublicKey pub = rsaKeys.publicKey();

        String kid = computeKeyId(pub);
        String n = base64Url(pub.getModulus());
        String e = base64Url(pub.getPublicExponent());

        Map<String, Object> jwkMap = new HashMap<>();
        jwkMap.put("kty", "RSA");
        jwkMap.put("use", "sig");
        jwkMap.put("alg", "RS256");
        jwkMap.put("kid", kid);
        jwkMap.put("n", n);
        jwkMap.put("e", e);

        String eTag = "W/\"" + Integer.toHexString(jwkMap.hashCode()) + "\"";
        Map<String, Object> body = Map.of("keys", List.of(jwkMap));

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .eTag(eTag)
                .body(body);
    }

    private static String base64Url(BigInteger v) {
        byte[] raw = unsignedBigIntBytes(v);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }

    private static byte[] unsignedBigIntBytes(BigInteger value) {
        byte[] raw = value.toByteArray();
        if (raw.length > 1 && raw[0] == 0) {
            byte[] stripped = new byte[raw.length - 1];
            System.arraycopy(raw, 1, stripped, 0, stripped.length);
            return stripped;
        }
        return raw;
    }

    private static String computeKeyId(RSAPublicKey key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(unsignedBigIntBytes(key.getModulus()));
            md.update(unsignedBigIntBytes(key.getPublicExponent()));
            byte[] digest = md.digest();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            // fallback to a deterministic but less compact id
            return Integer.toHexString(key.getModulus().hashCode()) + ":" + Integer.toHexString(key.getPublicExponent().hashCode());
        }
    }
}
