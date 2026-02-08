package com.quetoquenana.userservice.service.impl;

import com.google.firebase.auth.FirebaseToken;
import com.quetoquenana.userservice.dto.PersonCreateRequest;
import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.exception.DuplicateRecordException;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.AuthUserService;
import com.quetoquenana.userservice.service.PersonService;
import com.quetoquenana.userservice.service.UserService;
import com.quetoquenana.userservice.exception.EmailConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PersonService personService;

    @Override
    public ResolveResult resolveOrCreateFromFirebase(FirebaseToken decoded) {
        String uid = decoded.getUid();
        String email = decoded.getEmail();
        String name = decoded.getName();
        boolean emailVerified = Boolean.TRUE.equals(decoded.isEmailVerified());

        // try to find existing user by external provider/id
        Optional<User> existing = userRepository.findByExternalProviderIgnoreCaseAndExternalIdIgnoreCase("firebase", uid);
        if (existing.isPresent()) {
            return new ResolveResult(existing.get(), false);
        }

        // Create minimal person + user using existing services
        PersonCreateRequest personReq = new PersonCreateRequest();
        // idNumber is required by PersonCreateRequest - use uid as fallback
        personReq.setIdNumber(uid);
        // try to split name into first/lastname
        if (name != null && name.contains(" ")) {
            String[] parts = name.split(" ", 2);
            personReq.setName(parts[0]);
            personReq.setLastname(parts[1]);
        } else {
            personReq.setName(email != null ? email : uid);
            personReq.setLastname("");
        }

        UserCreateRequest userReq = new UserCreateRequest();
        userReq.setUsername(email != null ? email : uid);
        userReq.setPerson(personReq);
        userReq.setNickname(name != null ? name : null);

        try {
            User created = userService.save(userReq);
            // set externalProvider/externalId on user and persist directly via repository
            created.setExternalProvider("firebase");
            created.setExternalId(uid);
            userRepository.save(created);
            return new ResolveResult(created, true);
        } catch (DuplicateRecordException dre) {
            // username/email already exists
            throw new EmailConflictException("email.already.in.use");
        }
    }
}

