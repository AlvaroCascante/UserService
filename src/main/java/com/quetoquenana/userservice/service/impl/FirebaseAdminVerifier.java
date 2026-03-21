package com.quetoquenana.userservice.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.quetoquenana.userservice.exception.InvalidFirebaseTokenException;
import com.quetoquenana.userservice.service.FirebaseTokenVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
public class FirebaseAdminVerifier implements FirebaseTokenVerifier {

    private final FirebaseAuth firebaseAuth;
    private final boolean checkRevoked;
    private final boolean requireEmailVerified;
    private final String expectedAudience;
    private final long maxAuthAgeSeconds;

    public FirebaseAdminVerifier(
            @Value("${FIREBASE_CHECK_REVOKED:false}") boolean checkRevoked,
            @Value("${FIREBASE_REQUIRE_EMAIL_VERIFIED:false}") boolean requireEmailVerified,
            @Value("${FIREBASE_EXPECTED_AUD:}") String expectedAudience,
            @Value("${FIREBASE_MAX_AUTH_AGE_SECONDS:0}") long maxAuthAgeSeconds
    ) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.checkRevoked = checkRevoked;
        this.requireEmailVerified = requireEmailVerified;
        this.expectedAudience = expectedAudience;
        this.maxAuthAgeSeconds = maxAuthAgeSeconds;
    }

    @Override
    public FirebaseToken verify(String idToken) {
        try {
            FirebaseToken decoded = firebaseAuth.verifyIdToken(idToken);

            if (decoded == null || decoded.getUid() == null || decoded.getUid().isBlank()) {
                throw new InvalidFirebaseTokenException("invalid.firebase.token.missing.uid");
            }

            Map<String, Object> claims = decoded.getClaims();

            // Optional: revocation check
            if (checkRevoked) {
                try {
                    UserRecord userRecord = firebaseAuth.getUser(decoded.getUid());
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
            throw new InvalidFirebaseTokenException("invalid.firebase.token", e);
        }
    }
}
