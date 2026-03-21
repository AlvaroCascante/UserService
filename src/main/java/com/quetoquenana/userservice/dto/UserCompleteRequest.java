package com.quetoquenana.userservice.dto;

import com.quetoquenana.userservice.model.UserProvider;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCompleteRequest {
    private PersonCreateRequest person;
    private String firebaseUid;
    private String email;
    private boolean emailVerified;
    private String nickname;
    private UserProvider provider;
}
