package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.*;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

import static com.quetoquenana.userservice.util.Constants.Headers.APP_NAME;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final SecurityService securityService;
    private final TokenService tokenService;
    private final AuthUserService authUserService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            Authentication authentication,
            @RequestHeader(value = APP_NAME) String appCode
    ) {
        log.info("Login attempt for user {} to application {}", authentication.getName(), appCode);
        securityService.login(authentication);
        TokenResponse tokens = tokenService.createTokens(authentication, appCode);
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
    public ResponseEntity<TokenResponse> refresh(
            Authentication authentication,
            @RequestHeader(value = APP_NAME) String appCode
    ) {
        TokenResponse tokens = tokenService.refresh(authentication, appCode);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ResetUserRequest request) {
        log.info("ForgotPassword requested for user: {}", request.getUsername());
        securityService.forgotPassword(request.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/firebase-registration")
    @JsonView(Application.ApplicationDetail.class)
    public ResponseEntity<ApiResponse> completeRegistrationFromFirebase(
            @Valid @RequestBody UserCreateFromFirebaseRequest request,
            @RequestHeader(value = APP_NAME) String appCode
    ) {
        log.info("POST /api/auth/firebase-registration called with payload: {}", request);
        UserCreateFromFirebaseResponse user = authUserService.createFromFirebase(request, appCode);

        TokenResponse tokenResponse = tokenService.createTokensForUser(user.username(), appCode);

        CompleteRegistrationResponse response = new CompleteRegistrationResponse(
                tokenResponse,
                user
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(Collections.singletonMap("registration", response)));
    }


    @GetMapping("/firebase-login")
    @JsonView(Application.ApplicationDetail.class)
    public ResponseEntity<ApiResponse> checkForFirebaseSession(
            @RequestHeader(value = APP_NAME) String appCode
    ) {
        log.info("GET /api/auth/firebase-login");
        UserCreateFromFirebaseResponse user = authUserService.getFirebaseSession(appCode);

        TokenResponse tokenResponse = tokenService.createTokensForUser(user.username(), appCode);

        CompleteRegistrationResponse response = new CompleteRegistrationResponse(
                tokenResponse,
                user
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(Collections.singletonMap("registration", response)));
    }
}
