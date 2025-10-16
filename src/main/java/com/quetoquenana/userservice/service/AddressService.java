package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.AddressCreateRequest;
import com.quetoquenana.userservice.dto.AddressUpdateRequest;
import com.quetoquenana.userservice.model.Address;

import java.util.UUID;

public interface AddressService {
    Address addAddressToPerson(UUID idPerson, AddressCreateRequest request);
    Address updateAddress(UUID idPhone, AddressUpdateRequest request);
    void deleteById(UUID idPhone);
}
