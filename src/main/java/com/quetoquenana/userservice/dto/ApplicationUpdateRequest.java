package com.quetoquenana.userservice.dto;

import lombok.Data;

@Data
public class ApplicationUpdateRequest {
    private String name;

    private String description;

    private Boolean isActive;
}

