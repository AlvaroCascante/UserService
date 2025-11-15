package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppRoleUserCreateRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String roleName;
}
