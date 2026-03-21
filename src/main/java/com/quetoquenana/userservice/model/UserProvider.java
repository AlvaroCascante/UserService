package com.quetoquenana.userservice.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.Locale;

@Getter
public enum UserProvider {
    GOOGLE("google.com"),
    PASSWORD("password"),
    LOCAL_EMAIL("local.email"),
    UNKNOWN("unknown");

    private final String signInProvider;

    UserProvider(String signInProvider) {
        this.signInProvider = signInProvider;
    }

    public static UserProvider from(String value) {
        String normalized = value.toUpperCase(Locale.ROOT);

        return Arrays.stream(values())
                .filter(s -> s.name().equals(normalized))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static UserProvider fromSignInProvider(String value) {
        return Arrays.stream(values())
                .filter(s -> s.signInProvider.equals(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}

