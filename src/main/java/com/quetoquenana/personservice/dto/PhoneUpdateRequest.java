package com.quetoquenana.personservice.dto;

import lombok.Data;

@Data
public class PhoneUpdateRequest {
    private String phoneNumber;
    private String category;
    private Boolean isMain;
}
