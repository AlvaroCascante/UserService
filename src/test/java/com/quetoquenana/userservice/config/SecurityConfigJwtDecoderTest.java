package com.quetoquenana.userservice.config;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SecurityConfigJwtDecoderTest {

    private static final String ISSUER = "https://auth.example";
    private static final String AUDIENCE = "USR";

    private RsaKeyProperties rsaKeyProperties;
    private JwtDecoder jwtDecoder;
    private JwtDecoder refreshJwtDecoder;
    private RSAPrivateKey privateKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();
        rsaKeyProperties = new RsaKeyProperties(publicKey, privateKey, "user-service-key-id-", "localv1");

        SecurityConfig securityConfig = new SecurityConfig(mock(CorsConfigProperties.class), rsaKeyProperties);
        jwtDecoder = securityConfig.jwtDecoder(ISSUER, AUDIENCE);
        refreshJwtDecoder = securityConfig.refreshJwtDecoder(ISSUER);
    }

    @Test
    void jwtDecoder_shouldAcceptTokenWithConfiguredKid() throws Exception {
        String token = signedToken(rsaKeyProperties.currentKeyId());

        Jwt jwt = jwtDecoder.decode(token);

        assertEquals("user@example.com", jwt.getSubject());
        assertEquals(rsaKeyProperties.currentKeyId(), jwt.getHeaders().get("kid"));
    }

    @Test
    void jwtDecoder_shouldRejectTokenWhenKidIsMissing() throws Exception {
        String token = signedToken(null);

        assertThrows(JwtValidationException.class, () -> jwtDecoder.decode(token));
    }

    @Test
    void jwtDecoder_shouldRejectTokenWhenKidDoesNotMatchConfiguredKeyId() throws Exception {
        String token = signedToken("different-key-id");

        assertThrows(JwtValidationException.class, () -> jwtDecoder.decode(token));
    }

    @Test
    void refreshJwtDecoder_shouldAcceptRefreshTokenWithoutAudience() throws Exception {
        String token = signedToken(rsaKeyProperties.currentKeyId(), false, "refresh");

        Jwt jwt = refreshJwtDecoder.decode(token);

        assertEquals("user@example.com", jwt.getSubject());
        assertEquals(rsaKeyProperties.currentKeyId(), jwt.getHeaders().get("kid"));
    }

    @Test
    void refreshJwtDecoder_shouldRejectTokenWhenTypeIsNotRefresh() throws Exception {
        String token = signedToken(rsaKeyProperties.currentKeyId(), false, "auth");

        assertThrows(JwtValidationException.class, () -> refreshJwtDecoder.decode(token));
    }

    private String signedToken(String kid) throws Exception {
        return signedToken(kid, true, "auth");
    }

    private String signedToken(String kid, boolean includeAudience, String type) throws Exception {
        Instant now = Instant.now();

        JWSHeader.Builder headerBuilder = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT);
        if (kid != null) {
            headerBuilder.keyID(kid);
        }

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("user@example.com")
                .issuer(ISSUER)
                .issueTime(Date.from(now))
                .notBeforeTime(Date.from(now.minusSeconds(5)))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .jwtID("refresh-jti")
                .claim("type", type)
                .audience(includeAudience ? AUDIENCE : null)
                .build();

        SignedJWT signedJwt = new SignedJWT(headerBuilder.build(), claimsSet);
        signedJwt.sign(new RSASSASigner(privateKey));
        return signedJwt.serialize();
    }
}

