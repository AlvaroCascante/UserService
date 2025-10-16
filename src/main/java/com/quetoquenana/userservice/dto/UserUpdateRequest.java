package com.quetoquenana.userservice.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String nickname;
    private String userStatus;
}

