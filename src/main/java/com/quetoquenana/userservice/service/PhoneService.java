package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.PhoneCreateRequest;
import com.quetoquenana.userservice.dto.PhoneUpdateRequest;
import com.quetoquenana.userservice.model.Phone;

import java.util.UUID;

public interface PhoneService {
    Phone addPhone(UUID idPerson, PhoneCreateRequest request);

    Phone updatePhone(UUID idPhone, PhoneUpdateRequest request);

    void deleteById(UUID idPhone);
}
