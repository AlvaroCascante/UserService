
package com.quetoquenana.userservice.dto;

import com.quetoquenana.userservice.model.PhoneCategory;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PhoneCreateRequest {
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private PhoneCategory category;
    private Boolean isMain;
}
