package com.quetoquenana.userservice.util;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.util.Locale;

public final class JwtExceptionClassifier {

    private JwtExceptionClassifier() {
    }

    public static boolean isExpired(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof JwtValidationException jwtValidationException
                    && jwtValidationException.getErrors().stream()
                    .map(OAuth2Error::getDescription)
                    .filter(description -> description != null && !description.isBlank())
                    .map(description -> description.toLowerCase(Locale.ROOT))
                    .anyMatch(description -> description.contains("expired"))) {
                return true;
            }

            String message = current.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains("expired")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

