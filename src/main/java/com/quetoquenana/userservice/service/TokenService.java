package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.TokenResponse;
import org.springframework.security.core.Authentication;

public interface TokenService {
    TokenResponse createTokens(Authentication authentication);
    TokenResponse refresh(String refreshToken);
    TokenResponse createTokensForUser(com.quetoquenana.userservice.model.User user, String applicationName);
}
