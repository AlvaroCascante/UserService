package com.quetoquenana.userservice.dto;

import lombok.Data;

@Data
public class PersonUpdateRequest {
    private String name;
    private String lastname;
    private Boolean isActive;
}

