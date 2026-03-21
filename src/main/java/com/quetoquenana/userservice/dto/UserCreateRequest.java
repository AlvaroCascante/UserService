package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequest {
    @Email(message = "{validation.user.email.invalid}")
    private String username;

    @NotNull(message = "{validation.field.not.null}")
    private PersonCreateRequest person;

    private String nickname;
}
