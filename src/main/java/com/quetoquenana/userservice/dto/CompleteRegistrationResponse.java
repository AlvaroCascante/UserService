package com.quetoquenana.userservice.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.model.Application;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonView(Application.ApplicationDetail.class)
public class CompleteRegistrationResponse {
    private TokenResponse tokenResponse;
    private UserCreateFromFirebaseResponse user;
}
