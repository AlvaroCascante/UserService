package com.quetoquenana.personservice.service.impl;

import com.quetoquenana.personservice.dto.AddressCreateRequest;
import com.quetoquenana.personservice.dto.AddressUpdateRequest;
import com.quetoquenana.personservice.exception.InactiveRecordException;
import com.quetoquenana.personservice.exception.RecordNotFoundException;
import com.quetoquenana.personservice.model.Address;
import com.quetoquenana.personservice.model.Person;
import com.quetoquenana.personservice.repository.AddressRepository;
import com.quetoquenana.personservice.repository.PersonRepository;
import com.quetoquenana.personservice.service.AddressService;
import com.quetoquenana.personservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final PersonRepository personRepository;
    private final UserService userService;

    @Transactional
    @Override
    public Address addAddressToPerson(UUID personId, AddressCreateRequest request) {
        Person person = personRepository.findById(personId)
                .orElseThrow(RecordNotFoundException::new);
        if (!person.isActive()) {
            throw new InactiveRecordException("person.inactive");
        }
        Address address = Address.fromCreateRequest(request);
        address.setPerson(person);

        person.addAddress(address);
        person.setUpdatedAt(LocalDateTime.now());
        person.setUpdatedBy(userService.getCurrentUsername());
        addressRepository.save(address);
        return address;
    }

    @Transactional
    @Override
    public Address updateAddress(UUID addressId, AddressUpdateRequest request) {
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(RecordNotFoundException::new);
        Person person = existingAddress.getPerson();
        if (!person.isActive()) {
            throw new InactiveRecordException("person.inactive");
        }
        existingAddress.updateFromRequest(request);
        return addressRepository.save(existingAddress);
    }

    @Transactional
    @Override
    public void deleteById(UUID addressId) {
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(RecordNotFoundException::new);
        if (!existingAddress.getPerson().isActive()) {
            throw new InactiveRecordException();
        }
        addressRepository.delete(existingAddress);
    }
}