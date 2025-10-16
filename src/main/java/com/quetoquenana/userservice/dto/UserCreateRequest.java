package com.quetoquenana.userservice.dto;

import com.quetoquenana.userservice.model.UserStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UserCreateRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotNull
    private PersonCreateRequest person;

    private String nickname;

    private UserStatus userStatus;
}
