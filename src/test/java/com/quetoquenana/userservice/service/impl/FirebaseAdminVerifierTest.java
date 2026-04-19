package com.quetoquenana.userservice.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.quetoquenana.userservice.exception.InvalidFirebaseTokenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FirebaseAdminVerifierTest {

    private final FirebaseAuth firebaseAuth = mock(FirebaseAuth.class);
    private final FirebaseAdminVerifier verifier = new FirebaseAdminVerifier(firebaseAuth, false, false, "", 0);

    @Test
    @DisplayName("verify rejects non-JWT payloads with a clear message")
    void verify_rejectsNonJwtPayload() {
        InvalidFirebaseTokenException ex = assertThrows(
                InvalidFirebaseTokenException.class,
                () -> verifier.verify("{\"email\":\"test@gmail.com\"}")
        );

        assertTrue(ex.getMessage().contains("raw Firebase ID token JWT"));
    }

    @Test
    @DisplayName("verify rejects JWTs that do not contain a kid header")
    void verify_rejectsJwtWithoutKidHeader() {
        String tokenWithoutKid = unsignedJwt(null);

        InvalidFirebaseTokenException ex = assertThrows(
                InvalidFirebaseTokenException.class,
                () -> verifier.verify(tokenWithoutKid)
        );

        assertTrue(ex.getMessage().contains("missing the 'kid' header"));
    }

    @Test
    @DisplayName("verify delegates to Firebase Admin when the token shape matches an ID token")
    void verify_delegatesToFirebaseAdminForJwtWithKid() throws Exception {
        String tokenWithKid = unsignedJwt("firebase-key-1");
        FirebaseToken decoded = mock(FirebaseToken.class);
        when(decoded.getUid()).thenReturn("Ako3Vt678p3yqC7BAZsDeJvkFme3");
        when(firebaseAuth.verifyIdToken(tokenWithKid)).thenReturn(decoded);

        FirebaseToken result = verifier.verify(tokenWithKid);

        assertEquals(decoded, result);
        verify(firebaseAuth).verifyIdToken(tokenWithKid);
    }

    private static String unsignedJwt(String kid) {
        JWSHeader.Builder headerBuilder = new JWSHeader.Builder(JWSAlgorithm.RS256);
        if (kid != null) {
            headerBuilder.keyID(kid);
        }

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("https://securetoken.google.com/pedalpal-qtn")
                .audience("pedalpal-qtn")
                .subject("Ako3Vt678p3yqC7BAZsDeJvkFme3")
                .issueTime(java.util.Date.from(Instant.now()))
                .expirationTime(java.util.Date.from(Instant.now().plusSeconds(3600)))
                .build();

        String encodedHeader = headerBuilder.build().toBase64URL().toString();
        String encodedClaims = Base64URL.encode(claims.toJSONObject().toString()).toString();
        String encodedSignature = Base64URL.encode("signature").toString();

        return encodedHeader + "." + encodedClaims + "." + encodedSignature;
    }
}

