package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.model.User;
import org.springframework.security.core.Authentication;

public interface TokenService {
    TokenResponse createTokens(Authentication authentication, String appCode);
    TokenResponse refresh(String refreshToken, String appCode);
    TokenResponse createTokensForUser(String username, String appCode);
}
