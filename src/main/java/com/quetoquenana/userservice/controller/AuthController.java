package com.quetoquenana.userservice.controller;

import com.quetoquenana.userservice.dto.ChangePasswordRequest;
import com.quetoquenana.userservice.dto.RefreshRequest;
import com.quetoquenana.userservice.dto.ResetUserRequest;
import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
        log.info("Login attempt for user {} to application {}", authentication.getName(), applicationName);
        securityService.login(authentication);
        TokenResponse tokens = tokenService.createTokens(authentication);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> reset(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        log.info("Reset attempt for user: {}", authentication.getName());
        securityService.resetUser(authentication, request);
        return ResponseEntity.noContent().build();
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
