package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppRoleUserCreateRequest {
    @NotBlank
    private UserCreateRequest user;

    @NotBlank
    private String roleName;
}
