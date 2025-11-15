package com.quetoquenana.userservice.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.quetoquenana.userservice.dto.ProfileCreateRequest;
import com.quetoquenana.userservice.dto.ProfileUpdateRequest;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.Profile;
import com.quetoquenana.userservice.service.ProfileService;
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
    @PreAuthorize("@securityService.canAccessIdPerson(authentication, #idPerson)")
    public ResponseEntity<Profile> addProfile(
            @PathVariable UUID idPerson,
            @RequestBody ProfileCreateRequest request
    ) {
        log.info("POST /api/persons/{}/profile called with payload: {}", idPerson, request);
        Profile createdProfile = profileService.addProfile(idPerson, request);
        return ResponseEntity.ok(createdProfile);
    }

    @PutMapping("/profile/{idProfile}")
    @JsonView(Person.PersonDetail.class)
    @PreAuthorize("@securityService.canAccessIdProfile(authentication, #idProfile)")
    public ResponseEntity<Profile> updateProfile(
            @PathVariable UUID idProfile,
            @RequestBody ProfileUpdateRequest request) {
        log.info("PUT /api/persons/profile/{} called with payload: {}", idProfile, request);
        Profile updatedProfile = profileService.updateProfile(idProfile, request);
        return ResponseEntity.ok(updatedProfile);
    }
}
