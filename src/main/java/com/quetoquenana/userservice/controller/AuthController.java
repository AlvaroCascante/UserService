package com.quetoquenana.userservice.controller;

import com.quetoquenana.userservice.dto.RefreshRequest;
import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final SecurityService securityService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            Authentication authentication,
            @RequestHeader(value = "X-Application-Name") String applicationName) {

        log.info("Login attempt for application: {}", applicationName);
        // Spring's BasicAuthenticationFilter will have populated 'authentication' when Authorization: Basic is provided
        if (authentication == null || !authentication.isAuthenticated()) {
            // If no credentials were provided or authentication failed, let the framework return 401
            // Here we return 401 to be explicit
            return ResponseEntity.status(401).build();
        }

        String username = authentication.getName();
        // Build an application-scoped Authentication (authorities come from DB for the application)
        Authentication appAuth = securityService.getAuthenticationForApplication(username, applicationName);

        TokenResponse tokens = tokenService.createTokens(appAuth);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse tokens = tokenService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(tokens);
    }
}
