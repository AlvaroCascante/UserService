package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.AddressCreateRequest;
import com.quetoquenana.userservice.dto.AddressUpdateRequest;
import com.quetoquenana.userservice.model.Address;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.service.AddressService;
import jakarta.validation.Valid;
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
    @PreAuthorize("@securityService.canAccessIdPerson(authentication, #idPerson)")
    public ResponseEntity<Address> createAddress(
            @PathVariable UUID idPerson,
            @Valid @RequestBody AddressCreateRequest request
    ) {
        log.info("POST /api/persons/{}/address called with payload: {}", idPerson, request);
        Address created = addressService.addAddress(idPerson, request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/address/{idAddress}")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("@securityService.canAccessIdAddress(authentication, #idAddress)")
    public ResponseEntity<Address> updateAddress(
            @PathVariable UUID idAddress,
            @RequestBody AddressUpdateRequest request
    ) {
        log.info("PUT /api/persons/address/{} called with payload: {}", idAddress, request);
        Address updated = addressService.updateAddress(idAddress, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/address/{idAddress}")
    @PreAuthorize("@securityService.canAccessIdAddress(authentication, #idAddress)")
    public ResponseEntity<Void> deleteAddress(@PathVariable UUID idAddress) {
        log.info("DELETE /api/persons/address/{} called", idAddress);
        addressService.delete(idAddress);
        return ResponseEntity.noContent().build();
    }
}
