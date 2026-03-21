package com.quetoquenana.userservice.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PersonCreateCommand {
    private String idNumber;

    private String name;

    private String lastname;
}
