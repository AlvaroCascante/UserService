package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.Phone;
import com.quetoquenana.userservice.service.PhoneService;
import com.quetoquenana.userservice.dto.PhoneCreateRequest;
import com.quetoquenana.userservice.dto.PhoneUpdateRequest;
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
public class PhoneController {

    private final PhoneService phoneService;

    @PostMapping("/{idPerson}/phone")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("@securityService.canAccessId(authentication, #idNumber)")
    public ResponseEntity<Phone> addPhone(
            @PathVariable UUID idPerson,
            @RequestBody PhoneCreateRequest request
    ) {
        log.info("POST /api/persons/{}/phone called with payload: {}", idPerson, request);
        Phone created = phoneService.addPhoneToPerson(idPerson, request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/phone/{idPhone}")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("@securityService.canAccessId(authentication, #idNumber)")
    public ResponseEntity<Phone> updatePhone(
            @PathVariable UUID idPhone,
            @RequestBody PhoneUpdateRequest request
    ) {
        log.info("PUT /api/persons/phone/{} called with payload: {}", idPhone, request);
        Phone updated = phoneService.updatePhone(idPhone, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/phone/{id}")
    @PreAuthorize("@securityService.canAccessId(authentication, #idNumber)")
    public ResponseEntity<Void> deletePhone(@PathVariable UUID id) {
        log.info("DELETE /api/persons/phone/{} called", id);
        phoneService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
