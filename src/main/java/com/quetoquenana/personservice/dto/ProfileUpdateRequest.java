package com.quetoquenana.personservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequest {
    private LocalDate birthday;
    private String gender;
    private String nationality;
    private String maritalStatus;
    private String occupation;
    private String profilePictureUrl;
}