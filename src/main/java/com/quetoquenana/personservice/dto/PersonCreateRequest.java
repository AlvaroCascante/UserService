package com.quetoquenana.personservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PersonCreateRequest {
    @NotBlank
    private String idNumber;

    @NotBlank
    private String name;

    @NotBlank
    private String lastname;

    private Boolean isActive;
}

