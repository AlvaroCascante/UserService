package com.quetoquenana.userservice.service.impl;

import com.quetoquenana.userservice.dto.UserEmailInfo;
import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.dto.UserUpdateRequest;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.exception.DuplicateRecordException;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.CurrentUserService;
import com.quetoquenana.userservice.service.EmailService;
import com.quetoquenana.userservice.service.PersonService;
import com.quetoquenana.userservice.service.UserService;
import com.quetoquenana.userservice.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final AppRoleUserRepository appRoleUserRepository;
    private final UserRepository userRepository;
    private final PersonService personService;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final Executor emailExecutor;

    @Transactional
    @Override
    public User save(UserCreateRequest request) {
        // Check username uniqueness (case-insensitive)
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateRecordException("user.username.duplicate");
        }

        // Create or get person
        Person person = personService.findByIdNumber(request.getPerson().getIdNumber())
            .map(found -> {
                // Ensure person is active
                if (!found.getIsActive()) {
                    found.setIsActive(true);
                    personService.activateById(found.getId());
                }
                return found;
            })
            .orElseGet(() ->
                personService.save(request.getPerson())
            );

        String plain = PasswordUtil.generateRandomPassword();
        User user = User.fromCreateRequest(
            request,
            passwordEncoder.encode(plain),
            UserStatus.RESET,
                personService.getReference(person.getId())
        );

        user.setCreatedAt(LocalDateTime.now());
        user.setCreatedBy(currentUserService.getCurrentUsername());

        userRepository.save(user);

        // capture locale here (request thread) so it's preserved for the async task
        Locale locale = LocaleContextHolder.getLocale();
        UserEmailInfo emailInfo = UserEmailInfo.builder()
                .personLastname(person.getLastname())
                .personName(person.getName())
                .username(request.getUsername())
                .build();
        sendNewUserEmailAsync(emailInfo, plain, locale);
        return user;
    }

    @Transactional
    @Override
    public void resetUser(Authentication authentication, String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(AuthenticationException::new);

        String plain = PasswordUtil.generateRandomPassword();
        String passwordHash = passwordEncoder.encode(plain);
        user.updateStatus(UserStatus.RESET, passwordHash, authentication.getName());
        userRepository.save(user);

        // capture locale here (request thread) so it's preserved for the async task
        Locale locale = LocaleContextHolder.getLocale();
        UserEmailInfo emailInfo = UserEmailInfo.builder()
                .personName(user.getPerson().getName())
                .personLastname(user.getPerson().getLastname())
                .username(user.getUsername())
                .build();
        sendPasswordEmailAsync(emailInfo, plain, locale);
    }

    @Transactional
    @Override
    public User update(UUID id, UserUpdateRequest request) {
        User existing = userRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);

        existing.updateFromRequest(request, currentUserService.getCurrentUsername());

        return userRepository.save(existing);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        User existing = userRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);
        existing.updateStatus(UserStatus.INACTIVE, currentUserService.getCurrentUsername());
        userRepository.save(existing);
    }

    @Transactional
    @Override
    public void resetPassword(UUID id, String newPassword) {
        User existing = userRepository.findById(id)
                .orElseThrow(RecordNotFoundException::new);

        existing.updateStatus(UserStatus.ACTIVE, passwordEncoder.encode(newPassword), currentUserService.getCurrentUsername());
        userRepository.save(existing);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<AppRoleUser> findAllAppRoleByApplicationId(UUID idUser, UUID idApplication) {
        return appRoleUserRepository.findByUserIdAndRoleApplicationId(idUser, idApplication);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    // --- async email helpers ---
    private void sendNewUserEmailAsync(UserEmailInfo user, String plain, Locale locale) {
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendNewUserEmail(user, plain, locale);
            } catch (Exception e) {
                log.error("Error sending new user email to {}", user.getUsername(), e);
            }
        }, emailExecutor);
    }

    private void sendPasswordEmailAsync(UserEmailInfo user, String plain, Locale locale) {
        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendPasswordEmail(user, plain, locale);
            } catch (Exception e) {
                log.error("Error sending password reset email to {}", user.getUsername(), e);
            }
        }, emailExecutor);
    }
}

