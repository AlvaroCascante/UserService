package com.quetoquenana.userservice.controller;

import com.quetoquenana.userservice.dto.RefreshRequest;
import com.quetoquenana.userservice.dto.ResetUserRequest;
import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.quetoquenana.userservice.util.Constants.Headers.APP_NAME;

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
            @RequestHeader(value = APP_NAME) String applicationName
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

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ResetUserRequest request) {
        log.info("ForgotPassword requested for user: {}", request.getUsername());
        securityService.forgotPassword(request.getUsername());
        return ResponseEntity.noContent().build();
    }
}
