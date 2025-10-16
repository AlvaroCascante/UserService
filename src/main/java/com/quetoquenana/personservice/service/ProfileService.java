package com.quetoquenana.personservice.service;

import com.quetoquenana.personservice.dto.ProfileCreateRequest;
import com.quetoquenana.personservice.dto.ProfileUpdateRequest;
import com.quetoquenana.personservice.model.Profile;

import java.util.UUID;

public interface ProfileService {
    Profile addProfileToPerson(UUID idPerson, ProfileCreateRequest request);
    Profile updateProfile(UUID idProfile, ProfileUpdateRequest request);
}
