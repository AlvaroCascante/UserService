package com.quetoquenana.userservice.util;

public class FirebaseAuthUtils {
    public static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException("Missing Authorization header");
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        String token = authorizationHeader.substring(7).trim();

        if (token.isEmpty()) {
            throw new IllegalArgumentException("Missing Bearer token");
        }

        return token;
    }
}
