package com.quetoquenana.personservice.service;

import com.quetoquenana.personservice.dto.AddressCreateRequest;
import com.quetoquenana.personservice.dto.AddressUpdateRequest;
import com.quetoquenana.personservice.dto.PhoneCreateRequest;
import com.quetoquenana.personservice.dto.PhoneUpdateRequest;
import com.quetoquenana.personservice.model.Address;
import com.quetoquenana.personservice.model.Phone;

import java.util.UUID;

public interface AddressService {
    Address addAddressToPerson(UUID idPerson, AddressCreateRequest request);
    Address updateAddress(UUID idPhone, AddressUpdateRequest request);
    void deleteById(UUID idPhone);
}
