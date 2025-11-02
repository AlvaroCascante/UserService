package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "{field.not.blank}")
    private String newPassword;
}

