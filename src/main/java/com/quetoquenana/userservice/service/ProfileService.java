package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.dto.ProfileCreateRequest;
import com.quetoquenana.userservice.dto.ProfileUpdateRequest;
import com.quetoquenana.userservice.model.Profile;

import java.util.UUID;

public interface ProfileService {
    Profile addProfileToPerson(UUID idPerson, ProfileCreateRequest request);
    Profile updateProfile(UUID idProfile, ProfileUpdateRequest request);
}
