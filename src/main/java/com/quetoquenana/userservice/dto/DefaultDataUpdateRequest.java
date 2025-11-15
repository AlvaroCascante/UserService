package com.quetoquenana.userservice.dto;

import lombok.Data;

@Data
public class DefaultDataUpdateRequest {
    private String name;

    private String description;

    private Boolean isActive;

    private String dataCategory;
}

