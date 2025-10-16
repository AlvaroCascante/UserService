package com.quetoquenana.userservice.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileCreateRequest {
    private LocalDate birthday;
    private String gender;
    private String nationality;
    private String maritalStatus;
    private String occupation;
    private String profilePictureUrl;
}
