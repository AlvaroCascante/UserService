package com.quetoquenana.userservice.command;

import com.quetoquenana.userservice.model.UserProvider;
import com.quetoquenana.userservice.model.UserStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateUserCommand {
    private String idNumber;
    private String name;
    private String lastname;
    private String firebaseUid;
    private String email;
    private boolean emailVerified;
    private String nickname;
    private UserProvider provider;
    private String roleName;
    private String applicationCode;
    private UserStatus userStatus;
}
