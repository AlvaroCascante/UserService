package com.quetoquenana.userservice.util;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility for generating secure random passwords.
 * Methods are static for easy reuse across the codebase.
 */
@Slf4j
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%&*()-_=+[]{};:,.<>?";

    private static final String ALL = UPPER + LOWER + DIGITS + SYMBOLS;

    private PasswordUtil() {
        // utility class
    }

    /**
     * Generates a secure random password with the requested length.
     * The generated password will contain at least one upper, lower, digit and symbol when length >= 4.
     *
     * @param length desired password length (min 8)
     * @return generated password
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Password length must be at least 8");
        }

        List<Character> chars = new ArrayList<>(length);

        // ensure at least one from each class
        chars.add(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        chars.add(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        chars.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        chars.add(SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length())));

        for (int i = 4; i < length; i++) {
            chars.add(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }

        // shuffle to avoid predictable placement
        Collections.shuffle(chars, RANDOM);

        StringBuilder sb = new StringBuilder(length);
        for (char c : chars) sb.append(c);
        return sb.toString();
    }

    /**
     * Convenience method: generates a secure random password with a sensible default length (12).
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(12);
    }
}

