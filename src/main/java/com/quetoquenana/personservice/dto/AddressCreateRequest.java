package com.quetoquenana.personservice.dto;

import com.quetoquenana.personservice.model.AddressType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressCreateRequest {
    @NotBlank
    private String country;

    @NotBlank
    private String city;

    private String state;

    private String zipCode;

    @NotBlank
    private AddressType addressType;

    private String address;
}
