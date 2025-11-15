package com.quetoquenana.userservice.controller;

import com.quetoquenana.userservice.dto.RefreshRequest;
import com.quetoquenana.userservice.dto.ResetPasswordRequest;
import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
            @RequestHeader(value = "X-Application-Name") String applicationName
    ) {
        log.info("Login attempt for application: {}", applicationName);
        Authentication appAuth = securityService.getAuthenticationForApplication(authentication.getName(), applicationName);

        TokenResponse tokens = tokenService.createTokens(appAuth);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        TokenResponse tokens = tokenService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(
            Authentication authentication,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        log.info("POST /api/auth/reset called");
        securityService.resetPassword(authentication.getName(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
