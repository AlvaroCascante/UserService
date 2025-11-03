package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationUpdateRequest {
    @NotBlank
    private String name;

    private String description;

    private Boolean isActive;
}

