package com.quetoquenana.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FirebaseAuthResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserInfo user;
    private boolean isNewUser;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String email;
        private boolean emailVerified;
        private String displayName;
    }
}

