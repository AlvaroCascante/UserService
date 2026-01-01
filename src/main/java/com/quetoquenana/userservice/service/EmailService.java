package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.UserEmailInfo;

import java.util.Locale;

public interface EmailService {

    void sendPasswordEmail(UserEmailInfo user, String plainPassword, Locale locale);

    void sendNewUserEmail(UserEmailInfo user, String initialPassword, Locale locale);
}
