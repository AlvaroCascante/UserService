package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetUserRequest {

    @NotBlank
    private String username;
}

