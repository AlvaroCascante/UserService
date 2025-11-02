package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressCreateRequest {

    @NotBlank(message = "{field.not.blank}")
    private String address;

    @NotBlank(message = "{field.not.blank}")
    private String addressType;

    @NotBlank(message = "{field.not.blank}")
    private String city;private String country;

    private String state;

    private String zipCode;
}
