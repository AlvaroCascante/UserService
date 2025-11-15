package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AppRoleUserDeleteRequest {
    @NotBlank
    private String username;
}
