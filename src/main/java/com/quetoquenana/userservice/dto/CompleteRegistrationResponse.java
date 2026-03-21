package com.quetoquenana.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompleteRegistrationResponse {
    private TokenResponse tokenResponse;
    private UserCreateFromFirebaseResponse user;
}
