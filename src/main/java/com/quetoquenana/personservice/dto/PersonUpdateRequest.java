package com.quetoquenana.personservice.dto;

import lombok.Data;

@Data
public class PersonUpdateRequest {
    private String name;
    private String lastname;
    private Boolean isActive;
}

