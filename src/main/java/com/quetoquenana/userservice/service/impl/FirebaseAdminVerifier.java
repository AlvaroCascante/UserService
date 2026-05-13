package com.quetoquenana.userservice.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.quetoquenana.userservice.exception.InvalidFirebaseTokenException;
import com.quetoquenana.userservice.service.FirebaseTokenVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Verifies Firebase ID tokens using Firebase Admin SDK with some additional optional checks.
 * <p>
 * Supported additional checks (configurable via env/properties):
 * - checkRevoked (FIREBASE_CHECK_REVOKED): if true, compare token auth_time vs user's tokensValidAfterTimestamp
 * - requireEmailVerified (FIREBASE_REQUIRE_EMAIL_VERIFIED): if true, require email_verified claim
 * - expectedAudience (FIREBASE_EXPECTED_AUD): optional audience string expected in the token
 * - maxAuthAgeSeconds (FIREBASE_MAX_AUTH_AGE_SECONDS): if >0, require token.auth_time is recent enough
 */
@Component
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class FirebaseAdminVerifier implements FirebaseTokenVerifier {

    private final FirebaseAuth firebaseAuth;
    private final boolean checkRevoked;
    private final boolean requireEmailVerified;
    private final String expectedAudience;
    private final long maxAuthAgeSeconds;

    @Autowired
    public FirebaseAdminVerifier(
            @Value("${FIREBASE_CHECK_REVOKED:false}") boolean checkRevoked,
            @Value("${FIREBASE_REQUIRE_EMAIL_VERIFIED:false}") boolean requireEmailVerified,
            @Value("${FIREBASE_EXPECTED_AUD:}") String expectedAudience,
            @Value("${FIREBASE_MAX_AUTH_AGE_SECONDS:0}") long maxAuthAgeSeconds
    ) {
        this(FirebaseAuth.getInstance(),
                checkRevoked,
                requireEmailVerified,
                expectedAudience,
                maxAuthAgeSeconds
        );
    }

    FirebaseAdminVerifier(
            FirebaseAuth firebaseAuth,
            boolean checkRevoked,
            boolean requireEmailVerified,
            String expectedAudience,
            long maxAuthAgeSeconds
    ) {
        this.firebaseAuth = firebaseAuth;
        this.checkRevoked = checkRevoked;
        this.requireEmailVerified = requireEmailVerified;
        this.expectedAudience = expectedAudience;
        this.maxAuthAgeSeconds = maxAuthAgeSeconds;
    }

    @Override
    public FirebaseToken verify(String idToken) {
        log.debug("Verifying Firebase token with options: checkRevoked={}, requireEmailVerified={}, expectedAudience={}, maxAuthAgeSeconds={}",
                checkRevoked, requireEmailVerified, expectedAudience, maxAuthAgeSeconds);

        String normalizedToken = normalizeAndValidateRawIdToken(idToken);

        try {
            FirebaseToken decoded = firebaseAuth.verifyIdToken(normalizedToken);
            if (decoded == null || decoded.getUid() == null || decoded.getUid().isBlank()) {
                throw new InvalidFirebaseTokenException("invalid.firebase.token.missing.uid");
            }
            log.debug("Decoded Firebase token: uid={}, claims={}", decoded.getUid(), decoded.getClaims());

            Map<String, Object> claims = decoded.getClaims();

            // Optional: revocation check
            if (checkRevoked) {
                try {
                    UserRecord userRecord = firebaseAuth.getUser(decoded.getUid());
                    log.debug("Fetched user record for revocation check: uid={}, tokensValidAfter={}", userRecord.getUid(), userRecord.getTokensValidAfterTimestamp());
                    long tokensValidAfter = userRecord.getTokensValidAfterTimestamp();
                    // normalize tokensValidAfter: some SDKs return millis, some seconds. Convert to seconds if appears to be millis
                    long tokensValidAfterSec = tokensValidAfter > 1_000_000_000_000L ? tokensValidAfter / 1000L : tokensValidAfter;
                    // auth_time claim is seconds since epoch
                    Object authTimeObj = claims.get("auth_time");
                    if (authTimeObj instanceof Number) {
                        long authTimeSec = ((Number) authTimeObj).longValue();
                        if (authTimeSec < tokensValidAfterSec) {
                            throw new InvalidFirebaseTokenException("firebase.token.revoked");
                        }
                    }
                } catch (FirebaseAuthException fae) {
                    // If we cannot fetch user for revocation decision, treat as invalid
                    throw new InvalidFirebaseTokenException("invalid.firebase.token.cannot.verify.revocation", fae);
                }
            }

            // Optional: require email verified
            if (requireEmailVerified) {
                Object ev = claims.get("email_verified");
                if (!(ev instanceof Boolean) || !(Boolean) ev) {
                    throw new InvalidFirebaseTokenException("firebase.email.not.verified");
                }
            }

            // Optional: audience check
            if (expectedAudience != null && !expectedAudience.isBlank()) {
                Object audClaim = claims.get("aud");
                boolean ok = false;
                if (audClaim instanceof String) {
                    ok = expectedAudience.equals(audClaim);
                } else if (audClaim instanceof List) {
                    ok = ((List<?>) audClaim).contains(expectedAudience);
                }
                if (!ok) {
                    throw new InvalidFirebaseTokenException("firebase.invalid.audience");
                }
            }

            // Optional: auth_time recency
            if (maxAuthAgeSeconds > 0) {
                Object authTimeObj = claims.get("auth_time");
                if (!(authTimeObj instanceof Number)) {
                    throw new InvalidFirebaseTokenException("firebase.missing.auth_time");
                }
                long authTimeSec = ((Number) authTimeObj).longValue();
                long nowSec = Instant.now().getEpochSecond();
                if (nowSec - authTimeSec > maxAuthAgeSeconds) {
                    throw new InvalidFirebaseTokenException("firebase.auth_time.too_old");
                }
            }

            return decoded;

        } catch (FirebaseAuthException e) {
            log.error("FirebaseAuthException: ", e);
            throw new InvalidFirebaseTokenException("invalid.firebase.token", e);
        }
    }

    private String normalizeAndValidateRawIdToken(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new InvalidFirebaseTokenException("Firebase ID token is missing. Send the raw Firebase ID token in the Authorization Bearer header.");
        }

        String trimmedToken = idToken.trim();

        try {
            JWT parsedToken = JWTParser.parse(trimmedToken);

            if (!(parsedToken instanceof SignedJWT signedJwt)) {
                throw new InvalidFirebaseTokenException(
                        "Received an unsigned Firebase emulator-style token, but FIREBASE_AUTH_EMULATOR_HOST is not configured for this backend. Use a production Firebase ID token or enable the Firebase Auth emulator for local development."
                );
            }

            if (signedJwt.getHeader().getKeyID() == null || signedJwt.getHeader().getKeyID().isBlank()) {
                throw new InvalidFirebaseTokenException(
                        "Firebase ID token is missing the 'kid' header. Send the raw Firebase ID token from the client SDK, not decoded claims JSON, a custom token, or another JWT type."
                );
            }
            return trimmedToken;
        } catch (ParseException e) {
            throw new InvalidFirebaseTokenException(
                    "Expected a raw Firebase ID token JWT in the Authorization Bearer header, but received a non-JWT or malformed token.",
                    e
            );
        }
    }
}
