package com.quetoquenana.personservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.quetoquenana.personservice.dto.ProfileCreateRequest;
import com.quetoquenana.personservice.dto.ProfileUpdateRequest;
import com.quetoquenana.personservice.exception.DuplicateRecordException;
import com.quetoquenana.personservice.exception.InactiveRecordException;
import com.quetoquenana.personservice.exception.RecordNotFoundException;
import com.quetoquenana.personservice.model.Profile;
import com.quetoquenana.personservice.service.ProfileService;
import com.quetoquenana.personservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ProfileControllerTest {
    @Mock
    private ProfileService profileService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProfileController profileController;

    private Profile profile;
    private ProfileCreateRequest profileCreateRequest;
    private ProfileUpdateRequest profileUpdateRequest;
    private UUID personId;
    private UUID profileId;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        personId = UUID.randomUUID();
        profileId = UUID.randomUUID();
        profile = Profile.builder()
                .id(profileId)
                .gender("M")
                .build();
        profileCreateRequest = new ProfileCreateRequest();
        profileCreateRequest.setGender("M");
        profileUpdateRequest = new ProfileUpdateRequest();
        profileUpdateRequest.setGender("M");
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void testAddProfile_PersonNotFound() {
        when(profileService.addProfileToPerson(any(), any())).thenThrow(new RecordNotFoundException());
        assertThrows(RecordNotFoundException.class, () -> profileController.addProfile(personId, profileCreateRequest));
    }

    @Test
    void testAddProfile_PersonInactive() {
        when(profileService.addProfileToPerson(any(), any())).thenThrow(new InactiveRecordException());
        assertThrows(InactiveRecordException.class, () -> profileController.addProfile(personId, profileCreateRequest));
    }

    @Test
    void testAddProfile_ProfileAlreadyExists() {
        when(profileService.addProfileToPerson(any(), any())).thenThrow(new DuplicateRecordException());
        assertThrows(DuplicateRecordException.class, () -> profileController.addProfile(personId, profileCreateRequest));
    }

    @Test
    void testAddProfile_Success() {
        when(profileService.addProfileToPerson(any(), any())).thenReturn(profile);
        var response = profileController.addProfile(personId, profileCreateRequest);
        assertDoesNotThrow(() -> response);
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(profile.getId(), response.getBody().getId());
        assertEquals(profile.getGender(), response.getBody().getGender());
        // Add more assertions for other fields as needed
    }

    @Test
    void testUpdateProfile_ProfileNotFound() {
        when(profileService.updateProfile(any(), any())).thenThrow(new RecordNotFoundException());
        assertThrows(RecordNotFoundException.class, () -> profileController.updateProfile(profileId, profileUpdateRequest));
    }

    @Test
    void testUpdateProfile_PersonInactive() {
        when(profileService.updateProfile(any(), any())).thenThrow(new InactiveRecordException());
        assertThrows(InactiveRecordException.class, () -> profileController.updateProfile(profileId, profileUpdateRequest));
    }

    @Test
    void testUpdateProfile_Success() {
        when(profileService.updateProfile(any(), any())).thenReturn(profile);
        var response = profileController.updateProfile(profileId, profileUpdateRequest);
        assertDoesNotThrow(() -> response);
        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(profile.getId(), response.getBody().getId());
        assertEquals(profile.getGender(), response.getBody().getGender());
        // Add more assertions for other fields as needed
    }
}
