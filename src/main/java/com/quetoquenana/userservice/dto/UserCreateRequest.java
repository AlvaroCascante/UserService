package com.quetoquenana.userservice.dto;

import com.quetoquenana.userservice.model.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateRequest {
    @Email(message = "{user.email.invalid}")
    private String username;

    @NotBlank(message = "{field.not.blank}")
    private String password;

    @NotNull(message = "{field.not.null}")
    private PersonCreateRequest person;

    private String nickname;

    private UserStatus userStatus;

    @Override
    public String toString() {
        return "UserCreateRequest(username=" + username
                + ", password=<hidden>"
                + ", person=" + person
                + ", nickname=" + nickname
                + ", userStatus=" + userStatus
                + ")";
    }
}
