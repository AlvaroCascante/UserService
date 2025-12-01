package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateRequest {
    @Email(message = "{user.email.invalid}")
    private String username;

    @NotNull(message = "{field.not.null}")
    private PersonCreateRequest person;

    private String nickname;

    @Override
    public String toString() {
        return "UserCreateRequest(username=" + username
                + ", person=" + person
                + ", nickname=" + nickname
                + ")";
    }
}
