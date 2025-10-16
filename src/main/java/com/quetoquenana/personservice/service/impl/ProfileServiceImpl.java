package com.quetoquenana.personservice.service.impl;

import com.quetoquenana.personservice.dto.ProfileCreateRequest;
import com.quetoquenana.personservice.dto.ProfileUpdateRequest;
import com.quetoquenana.personservice.exception.DuplicateRecordException;
import com.quetoquenana.personservice.exception.InactiveRecordException;
import com.quetoquenana.personservice.exception.RecordNotFoundException;
import com.quetoquenana.personservice.model.Person;
import com.quetoquenana.personservice.model.Profile;
import com.quetoquenana.personservice.repository.ProfileRepository;
import com.quetoquenana.personservice.repository.PersonRepository;
import com.quetoquenana.personservice.service.ProfileService;
import com.quetoquenana.personservice.service.UserService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final PersonRepository personRepository;
    private final ProfileRepository profileRepository;
    private final UserService userService;

    public ProfileServiceImpl(PersonRepository personRepository, ProfileRepository profileRepository, UserService userService) {
        this.personRepository = personRepository;
        this.profileRepository = profileRepository;
        this.userService = userService;
    }

    @Override
    public Profile addProfileToPerson(UUID idPerson, ProfileCreateRequest request) {
        Person person = personRepository.findById(idPerson)
            .map(it -> {
                if (!it.isActive()) {
                    throw new InactiveRecordException("person.inactive");
                }
                if (it.getProfile() != null) {
                    throw new DuplicateRecordException("person.profile.already.exists");
                }
                return it;
            })
            .orElseThrow(RecordNotFoundException::new);
        Profile profile = Profile.fromAddRequest(request);
        profile.setPerson(person);
        profile.setCreatedAt(java.time.LocalDateTime.now());
        profile.setCreatedBy(userService.getCurrentUsername());
        return profileRepository.save(profile);
    }

    @Override
    public Profile updateProfile(UUID idProfile, ProfileUpdateRequest request) {
        Profile profile = profileRepository.findById(idProfile)
            .orElseThrow(RecordNotFoundException::new);
        profile.updateFromRequest(request, userService.getCurrentUsername());
        return profileRepository.save(profile);
    }
}
