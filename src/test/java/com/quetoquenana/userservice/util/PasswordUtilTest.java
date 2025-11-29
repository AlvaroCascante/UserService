package com.quetoquenana.userservice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void defaultPassword_hasDefaultLengthAndClasses() {
        String pw = PasswordUtil.generateRandomPassword();
        assertNotNull(pw);
        assertEquals(12, pw.length(), "default password length should be 12");
        assertTrue(containsUpper(pw));
        assertTrue(containsLower(pw));
        assertTrue(containsDigit(pw));
        assertTrue(containsSymbol(pw));
    }

    @Test
    void customLength_respected() {
        String pw = PasswordUtil.generateRandomPassword(16);
        assertNotNull(pw);
        assertEquals(16, pw.length());
    }

    @Test
    void tooShort_throws() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.generateRandomPassword(6));
    }

    private boolean containsUpper(String s) {
        return s.chars().anyMatch(Character::isUpperCase);
    }

    private boolean containsLower(String s) {
        return s.chars().anyMatch(Character::isLowerCase);
    }

    private boolean containsDigit(String s) {
        return s.chars().anyMatch(Character::isDigit);
    }

    private boolean containsSymbol(String s) {
        return s.chars().anyMatch(c -> "!@#$%&*()-_=+[]{};:,.<>?".indexOf(c) >= 0);
    }
}

