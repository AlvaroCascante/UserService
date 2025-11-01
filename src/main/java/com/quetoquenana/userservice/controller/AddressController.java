package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.AddressCreateRequest;
import com.quetoquenana.userservice.dto.AddressUpdateRequest;
import com.quetoquenana.userservice.model.Address;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    @PostMapping("/{idPerson}/address")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("@securityService.canAccessId(authentication, #idNumber)")
    public ResponseEntity<Address> createAddress(
            @PathVariable UUID idPerson,
            @RequestBody AddressCreateRequest request
    ) {
        log.info("POST /api/persons/{}/address called with payload: {}", idPerson, request);
        Address created = addressService.addAddressToPerson(idPerson, request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/address/{idAddress}")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("@securityService.canAccessId(authentication, #idNumber)")
    public ResponseEntity<Address> updateAddress(
            @PathVariable UUID idAddress,
            @RequestBody AddressUpdateRequest request
    ) {
        log.info("PUT /api/persons/address/{} called with payload: {}", idAddress, request);
        Address updated = addressService.updateAddress(idAddress, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/address/{id}")
    @PreAuthorize("@securityService.canAccessId(authentication, #idNumber)")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID id) {
        log.info("DELETE /api/persons/address/{} called", id);
        addressService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
