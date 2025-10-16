package com.quetoquenana.personservice.service;

import com.quetoquenana.personservice.dto.PhoneCreateRequest;
import com.quetoquenana.personservice.dto.PhoneUpdateRequest;
import com.quetoquenana.personservice.model.Phone;

import java.util.UUID;

public interface PhoneService {
    Phone addPhoneToPerson(UUID idPerson, PhoneCreateRequest request);
    Phone updatePhone(UUID idPhone, PhoneUpdateRequest request);
    void deleteById(UUID idPhone);
}
