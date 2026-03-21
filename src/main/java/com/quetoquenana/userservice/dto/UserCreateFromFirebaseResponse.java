package com.quetoquenana.userservice.dto;

public record UserCreateFromFirebaseResponse(
        String idNumber,
        String name,
        String lastname,
        String username,
        String nickname,
        String applicationName,
        String applicationCode
) { }
