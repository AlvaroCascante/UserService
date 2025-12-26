package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.model.User;

import java.util.Locale;

public interface EmailService {

    /**
     * Send a password-related email (e.g., when a new temporary password is generated).
     */
    void sendPasswordEmail(User user, String plainPassword);

    /**
     * Send a password-related email using an explicit Locale.
     */
    void sendPasswordEmail(User user, String plainPassword, Locale locale);

    /**
     * Send a welcome/new-user email (e.g., after a user account has been created).
     */
    void sendNewUserEmail(User user, String initialPassword);

    /**
     * Send a welcome/new-user email using an explicit Locale.
     */
    void sendNewUserEmail(User user, String initialPassword, Locale locale);
}
