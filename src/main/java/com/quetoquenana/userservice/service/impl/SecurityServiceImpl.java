package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.model.*;
import com.quetoquenana.userservice.repository.*;
import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.service.EmailService;
import com.quetoquenana.userservice.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final ApplicationRepository applicationRepository;
    private final AppRoleUserRepository appRoleUserRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public Authentication authenticate(String username, String password, String applicationName) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(AuthenticationException::new);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException();
        }

        // find application
        Application application = applicationRepository.findByName(applicationName)
                .orElseThrow(() -> new AuthenticationException("error.authentication.application"));

        // get roles for user in the application
        List<AppRoleUser> roleMappings = appRoleUserRepository.findByUserIdAndRoleApplicationId(user.getId(), application.getId());
        List<GrantedAuthority> authorities = roleMappings.stream()
                .map(mapping -> new SimpleGrantedAuthority(mapping.getRole().getRoleName()))
                .collect(Collectors.toList());

        // create an authenticated token with authorities
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    @Override
    public Authentication getAuthenticationForApplication(String username, String applicationName) {
        // find user existence
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(AuthenticationException::new);

        if (user.getUserStatus().equals(UserStatus.RESET)) {
            throw new AuthenticationException("error.authentication.reset");
        }

        Application application = applicationRepository.findByName(applicationName)
                .orElseThrow(() -> new AuthenticationException("error.authentication.application"));

        List<AppRoleUser> roleMappings = appRoleUserRepository.findByUserIdAndRoleApplicationId(user.getId(), application.getId());
        List<GrantedAuthority> authorities = roleMappings.stream()
                .map(mapping -> new SimpleGrantedAuthority(mapping.getRole().getRoleName()))
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }

    @Override
    public void recoverPassword(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(AuthenticationException::new);

        String plain = PasswordUtil.generateRandomPassword();
        String passwordHash = passwordEncoder.encode(plain);
        user.updateStatus(UserStatus.RESET, passwordHash, username);
        userRepository.save(user);
        // send email with the temporary password (do not log plain value)
        try {
            emailService.sendPasswordEmail(user, plain, org.springframework.context.i18n.LocaleContextHolder.getLocale());
        } catch (Exception e) {
            log.error("Error sending password reset email to {}", username, e);
        }
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
        if (authentication == null || authentication.getName() == null || idAddress == null) return false;

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
        if (authentication == null || authentication.getName() == null || idPhone == null) return false;

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
        if (authentication == null || authentication.getName() == null || idUser == null) return false;

        Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(authentication.getName());
        return userOpt.map(user -> user.getId().equals(idUser)).orElse(false);
    }
}
