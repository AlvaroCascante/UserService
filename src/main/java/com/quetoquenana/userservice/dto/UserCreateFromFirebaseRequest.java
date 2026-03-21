package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCreateFromFirebaseRequest {
    @NotNull(message = "{validation.field.not.null}")
    private PersonCreateRequest person;

    private String nickname;

    @Override
    public String toString() {
        return "UserCreateRequest(username="
                + ", person=" + person
                + ", nickname=" + nickname
                + ")";
    }
}
