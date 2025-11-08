 package com.quetoquenana.userservice.dto;

 import lombok.AllArgsConstructor;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;

 @Getter
 @Setter
 @AllArgsConstructor
 @NoArgsConstructor
 public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}

