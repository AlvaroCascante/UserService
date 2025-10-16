package com.quetoquenana.personservice.dto;

import com.quetoquenana.personservice.model.AddressType;
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

