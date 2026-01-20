package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.ChangePasswordRequest;
import com.quetoquenana.userservice.dto.UserEmailInfo;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.Profile;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.repository.AddressRepository;
import com.quetoquenana.userservice.repository.PhoneRepository;
import com.quetoquenana.userservice.repository.ProfileRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.EmailService;
import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Default implementation of {@link SecurityService} used from method-security SpEL (bean name: "securityService").
 */
@Service("securityService")
@RequiredArgsConstructor
@Slf4j
public class SecurityServiceImpl implements SecurityService {

    private final AddressRepository addressRepository;
    private final PhoneRepository phoneRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Executor emailExecutor;

    @Override
    public void login(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(AuthenticationException::new);

        if (user.getUserStatus().equals(UserStatus.RESET)) {
            throw new AuthenticationException("error.authentication.reset");
        }
    }

    @Override
    public void resetUser(Authentication authentication, ChangePasswordRequest request) {
        String username = authentication.getName();
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(AuthenticationException::new);

        String passwordHash = passwordEncoder.encode(request.getNewPassword());
        user.updateStatus(UserStatus.ACTIVE, passwordHash, username);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(AuthenticationException::new);

        String plain = PasswordUtil.generateRandomPassword();
        String passwordHash = passwordEncoder.encode(plain);
        user.updateStatus(UserStatus.RESET, passwordHash, username);
        userRepository.save(user);

        // capture Locale and lightweight DTO inside the request thread to avoid ThreadLocal loss and lazy-loading
        Locale locale = LocaleContextHolder.getLocale();
        UserEmailInfo emailInfo = UserEmailInfo.builder()
                .personName(user.getPerson().getName())
                .personLastname(user.getPerson().getLastname())
                .username(user.getUsername())
                .build();
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendPasswordEmail(emailInfo, plain, locale);
            } catch (Exception e) {
                log.error("Error sending password reset email to {}", username, e);
            }
        }, emailExecutor);
    }

    @Override
    public boolean canAccessIdNumber(Authentication authentication, String idNumber) {
        if (authentication == null || authentication.getName() == null) return false;
        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(u -> u.getPerson() != null && idNumber != null && idNumber.equals(u.getPerson().getIdNumber()))
                .orElse(false);
    }

    @Override
    public boolean canAccessIdProfile(Authentication authentication, UUID idProfile) {
        if (authentication == null || authentication.getName() == null || idProfile == null) return false;

        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(User::getPerson)
                .filter(person -> person.getId() != null)
                .flatMap(person -> profileRepository.findByPersonId(person.getId()))
                .map(Profile::getId)
                .map(id -> id.equals(idProfile))
                .orElse(false);
    }

    @Override
    public boolean canAccessIdPerson(Authentication authentication, UUID idPerson) {
        if (authentication == null || authentication.getName() == null || idPerson == null) return false;

        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(User::getPerson)
                .map(Person::getId)
                .map(idPerson::equals)
                .orElse(false);
    }

    @Override
    public boolean canAccessIdAddress(Authentication authentication, UUID idAddress) {
        if (!paramsValidation(authentication, idAddress)) return false;

        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(User::getPerson)
                .filter(person -> person.getId() != null)
                .map(person -> addressRepository.findByPersonId(person.getId())
                        .stream()
                        .anyMatch(address -> address.getId().equals(idAddress)))
                .orElse(false);
    }

    @Override
    public boolean canAccessIdPhone(Authentication authentication, UUID idPhone) {
        if (!paramsValidation(authentication, idPhone)) return false;

        return userRepository.findByUsernameIgnoreCase(authentication.getName())
                .map(User::getPerson)
                .filter(person -> person.getId() != null)
                .map(person -> phoneRepository.findByPersonId(person.getId())
                        .stream()
                        .anyMatch(phone -> phone.getId().equals(idPhone)))
                .orElse(false);
    }

    @Override
    public boolean canAccessIdUser(Authentication authentication, UUID idUser) {
        if (!paramsValidation(authentication, idUser)) return false;

        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(authentication.getName());
        return userOpt.map(user -> user.getId().equals(idUser)).orElse(false);
    }

    private boolean paramsValidation(Authentication authentication, UUID id) {
        return authentication != null && authentication.getName() != null && id != null;
    }
}
