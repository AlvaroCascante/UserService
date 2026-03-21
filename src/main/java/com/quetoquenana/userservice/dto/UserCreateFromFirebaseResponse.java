package com.quetoquenana.userservice.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.User;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

public record UserCreateFromFirebaseResponse(
        String idNumber,
        String name,
        String lastname,
        String username,
        String nickname,
        String applicationName,
        String applicationCode
) { }
