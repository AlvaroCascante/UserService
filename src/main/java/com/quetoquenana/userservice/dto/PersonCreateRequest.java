package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PersonCreateRequest {
    @NotBlank(message = "{field.not.blank}")
    private String idNumber;

    @NotBlank(message = "{field.not.blank}")
    private String name;

    @NotBlank(message = "{field.not.blank}")
    private String lastname;

    private Boolean isActive;
}

