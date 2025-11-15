package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppRoleCreateRequest {
    @NotBlank
    private String roleName;

    private String description;
}

