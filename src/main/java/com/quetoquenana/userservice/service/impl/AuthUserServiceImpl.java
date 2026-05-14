package com.quetoquenana.userservice.service.impl;

import com.google.firebase.auth.FirebaseToken;
import com.quetoquenana.userservice.command.CreateUserCommand;
import com.quetoquenana.userservice.dto.UserCreateFromFirebaseRequest;
import com.quetoquenana.userservice.dto.UserCreateFromFirebaseResponse;
import com.quetoquenana.userservice.exception.DuplicateRecordException;
import com.quetoquenana.userservice.exception.EmailConflictException;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.UserProvider;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.service.ApplicationService;
import com.quetoquenana.userservice.service.AuthUserService;
import com.quetoquenana.userservice.service.FirebaseTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

import static com.quetoquenana.userservice.util.Constants.Headers.AUTHORIZATION;
import static com.quetoquenana.userservice.util.Constants.Roles.ROLE_NAME_USER;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUserServiceImpl implements AuthUserService {

    private final ApplicationService  applicationService;
    private final FirebaseTokenVerifier firebaseTokenVerifier;

    @Override
    public UserCreateFromFirebaseResponse createFromFirebase(
            UserCreateFromFirebaseRequest request,
            String appCode
    ) {
        FirebaseToken decoded = firebaseTokenVerifier.verify(getToken());
        String signInProvider = getSignInProvider(decoded);

        CreateUserCommand createUserCommand = CreateUserCommand.builder()
                .idNumber(request.getPerson().getIdNumber())
                .name(request.getPerson().getName())
                .lastname(request.getPerson().getLastname())
                .firebaseUid(decoded.getUid())
                .email(decoded.getEmail())
                .emailVerified(decoded.isEmailVerified())
                .nickname(request.getNickname() == null? decoded.getName() : request.getNickname())
                .provider(UserProvider.fromSignInProvider(signInProvider))
                .roleName(ROLE_NAME_USER)
                .applicationCode(appCode)
                .userStatus(UserStatus.ACTIVE)
                .build();

        try {
            AppRoleUser appRoleUser = applicationService.addUser(createUserCommand);
            return new UserCreateFromFirebaseResponse(
                    appRoleUser.getUser().getId().toString(),
                    appRoleUser.getUser().getPerson().getIdNumber(),
                    appRoleUser.getUser().getPerson().getName(),
                    appRoleUser.getUser().getPerson().getLastname(),
                    appRoleUser.getUser().getUsername(),
                    appRoleUser.getUser().getNickname(),
                    appRoleUser.getRole().getApplication().getName(),
                    appRoleUser.getRole().getApplication().getCode()
            );
        } catch (DuplicateRecordException dre) {
            // username/email already exists
            throw new EmailConflictException("email.already.in.use");
        }
    }

    @Override
    public UserCreateFromFirebaseResponse getFirebaseSession(String appCode) {
        log.debug("AuthUserServiceImpl getFirebaseSession: appCode={}", appCode);
        FirebaseToken decoded = firebaseTokenVerifier.verify(getToken());

        log.debug("AuthUserServiceImpl getFirebaseSession: Uid={}", decoded.getUid());
        AppRoleUser appRoleUser = applicationService.getUser(appCode, decoded.getUid());
        return new UserCreateFromFirebaseResponse(
                appRoleUser.getUser().getId().toString(),
                appRoleUser.getUser().getPerson().getIdNumber(),
                appRoleUser.getUser().getPerson().getName(),
                appRoleUser.getUser().getPerson().getLastname(),
                appRoleUser.getUser().getUsername(),
                appRoleUser.getUser().getNickname(),
                appRoleUser.getRole().getApplication().getName(),
                appRoleUser.getRole().getApplication().getCode()
        );
    }

    private String getSignInProvider(FirebaseToken decoded) {
        Map<String, Object> firebaseClaims = (Map<String, Object>) decoded.getClaims().get("firebase");
        return firebaseClaims != null
                ? (String) firebaseClaims.get("sign_in_provider")
                : "unknown";
    }

    private String getToken() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String authorization = attrs.getRequest().getHeader(AUTHORIZATION);

            if (authorization == null || authorization.isBlank()) {
                throw new IllegalArgumentException("Missing Authorization header");
            }

            if (!authorization.startsWith("Bearer ")) {
                throw new IllegalArgumentException("Invalid Authorization header format");
            }

            String token = authorization.substring(7).trim();

            if (token.isEmpty()) {
                throw new IllegalArgumentException("Missing token after Bearer");
            }
            return token;
        }
        throw new IllegalArgumentException("Missing Authorization header");
    }
}

