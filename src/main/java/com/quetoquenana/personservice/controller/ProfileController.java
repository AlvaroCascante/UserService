package com.quetoquenana.personservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.personservice.dto.ProfileCreateRequest;
import com.quetoquenana.personservice.dto.ProfileUpdateRequest;
import com.quetoquenana.personservice.model.Person;
import com.quetoquenana.personservice.model.Profile;
import com.quetoquenana.personservice.service.ProfileService;
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
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/{idPerson}/profile")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')") // ADMIN, AUDITOR and USER roles can access
    public ResponseEntity<Profile> addProfile(
            @PathVariable UUID idPerson,
            @RequestBody ProfileCreateRequest request
    ) {
        log.info("POST /api/persons/{}/profile called with payload: {}", idPerson, request);
        Profile createdProfile = profileService.addProfileToPerson(idPerson, request);
        return ResponseEntity.ok(createdProfile);
    }

    @PutMapping("/profile/{idPersonProfile}")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')") // ADMIN, AUDITOR and USER roles can access
    public ResponseEntity<Profile> updateProfile(
            @PathVariable UUID idPersonProfile,
            @RequestBody ProfileUpdateRequest request) {
        log.info("PUT /api/persons/profile/{} called with payload: {}", idPersonProfile, request);
        Profile updatedProfile = profileService.updateProfile(idPersonProfile, request);
        return ResponseEntity.ok(updatedProfile);
    }
}
