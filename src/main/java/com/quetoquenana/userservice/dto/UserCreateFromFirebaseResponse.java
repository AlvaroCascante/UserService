package com.quetoquenana.userservice.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.model.Application;

@JsonView(Application.ApplicationDetail.class)
public record UserCreateFromFirebaseResponse(
        String idNumber,
        String name,
        String lastname,
        String username,
        String nickname,
        String applicationName,
        String applicationCode
) { }
