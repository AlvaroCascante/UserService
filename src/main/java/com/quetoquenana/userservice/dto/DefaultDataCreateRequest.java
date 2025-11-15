package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DefaultDataCreateRequest {
    @NotBlank
    private String name;

    private String description;

    private Boolean isActive;

    private String dataCategory;
}

