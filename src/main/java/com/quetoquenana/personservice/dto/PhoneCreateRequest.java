
package com.quetoquenana.personservice.dto;

import com.quetoquenana.personservice.model.PhoneCategory;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Data
public class PhoneCreateRequest {
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private PhoneCategory category;
    private Boolean isMain;
}
