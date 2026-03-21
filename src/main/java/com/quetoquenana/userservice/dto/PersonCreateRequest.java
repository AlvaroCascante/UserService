package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonCreateRequest {
    @NotBlank(message = "{field.not.blank}")
    private String idNumber;

    @NotBlank(message = "{field.not.blank}")
    private String name;

    @NotBlank(message = "{field.not.blank}")
    private String lastname;
}

