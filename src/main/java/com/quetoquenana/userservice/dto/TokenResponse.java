 package com.quetoquenana.userservice.dto;

 import com.fasterxml.jackson.annotation.JsonView;
 import com.quetoquenana.userservice.model.Application;
 import lombok.AllArgsConstructor;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;

 @Getter
 @Setter
 @AllArgsConstructor
 @NoArgsConstructor
 @JsonView(Application.ApplicationDetail.class)
 public class TokenResponse {
     private String accessToken;
     private String refreshToken;
     private long expiresIn;
}

