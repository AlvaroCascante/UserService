package com.quetoquenana.userservice.dto;

import lombok.Data;

@Data
public class AddressUpdateRequest {
    private String country;
    private String city;
    private String state;
    private String zipCode;
    private String addressType;
    private String address;
}

