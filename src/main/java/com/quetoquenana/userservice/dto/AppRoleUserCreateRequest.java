package com.quetoquenana.userservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppRoleUserCreateRequest {
    @NotNull
    @Valid
    private UserCreateRequest user;

    @NotBlank
    private String roleName;
}
