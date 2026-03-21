package com.quetoquenana.userservice.controller;

import com.quetoquenana.userservice.dto.ChangePasswordRequest;
import com.quetoquenana.userservice.dto.RefreshRequest;
import com.quetoquenana.userservice.dto.ResetUserRequest;
import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.dto.FirebaseAuthResponse;
import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.service.TokenService;
import com.quetoquenana.userservice.service.FirebaseTokenVerifier;
import com.quetoquenana.userservice.service.AuthUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import static com.quetoquenana.userservice.util.Constants.Headers.APP_NAME;
import static com.quetoquenana.userservice.util.Constants.Headers.AUTHORIZATION;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final SecurityService securityService;
    private final TokenService tokenService;
    private final FirebaseTokenVerifier firebaseTokenVerifier;
    private final AuthUserService authUserService;

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

    @PostMapping("/firebase")
    public ResponseEntity<FirebaseAuthResponse> firebaseLogin(
            @RequestHeader(value = AUTHORIZATION) String authorization,
            @RequestHeader(value = APP_NAME) String applicationName
    ) {
        // extract bearer token
        if (authorization == null || !authorization.toLowerCase().startsWith("bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        String idToken = authorization.substring(7).trim();
        if (idToken.isEmpty()) {
            throw new IllegalArgumentException("Missing Firebase ID token in Authorization header");
        }

        // verify token with Firebase
        var decoded = firebaseTokenVerifier.verify(idToken);

        // resolve or create user from firebase token
        var result = authUserService.resolveOrCreateFromFirebase(decoded);

        // create tokens for the user using the provided application name as audience
        var tokenResponse = tokenService.createTokensForUser(result.getUser(), applicationName);

        var resp = new FirebaseAuthResponse(
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn(),
                new FirebaseAuthResponse.UserInfo(result.getUser().getId(), decoded.getEmail(), decoded.isEmailVerified(), decoded.getName()),
                result.isNewUser()
        );

        return ResponseEntity.ok(resp);
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
