
package com.quetoquenana.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhoneCreateRequest {
    @NotBlank(message = "{field.not.blank}")
    private String phoneNumber;

    @NotBlank(message = "{field.not.blank}")
    private String category;

    private Boolean isMain;
}
