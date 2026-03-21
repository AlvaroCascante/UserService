package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ApplicationCreateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String code;

    private String description;

    private Boolean isActive;
}
