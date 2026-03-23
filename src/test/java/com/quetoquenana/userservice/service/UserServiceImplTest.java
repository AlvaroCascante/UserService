package com.quetoquenana.userservice.service;

import com.quetoquenana.userservice.command.CreateUserCommand;
import com.quetoquenana.userservice.command.PersonCreateCommand;
import com.quetoquenana.userservice.dto.UserCreateRequest;
import com.quetoquenana.userservice.dto.UserEmailInfo;
import com.quetoquenana.userservice.dto.UserUpdateRequest;
import com.quetoquenana.userservice.exception.AuthenticationException;
import com.quetoquenana.userservice.exception.DuplicateRecordException;
import com.quetoquenana.userservice.exception.RecordNotFoundException;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserProvider;
import com.quetoquenana.userservice.model.UserStatus;
import com.quetoquenana.userservice.repository.AppRoleUserRepository;
import com.quetoquenana.userservice.repository.UserRepository;
import com.quetoquenana.userservice.service.impl.UserServiceImpl;
import com.quetoquenana.userservice.util.TestEntityFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private AppRoleUserRepository appRoleUserRepository;
    private UserRepository userRepository;
    private PersonService personService;
    private CurrentUserService currentUserService;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        appRoleUserRepository = mock(AppRoleUserRepository.class);
        userRepository = mock(UserRepository.class);
        personService = mock(PersonService.class);
        currentUserService = mock(CurrentUserService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);

        Executor directExecutor = Runnable::run;
        userService = new UserServiceImpl(
                appRoleUserRepository,
                userRepository,
                personService,
                currentUserService,
                passwordEncoder,
                emailService,
                directExecutor
        );

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(isNull())).thenReturn("encoded-null");
        when(passwordEncoder.encode(any(CharSequence.class)))
                .thenAnswer(invocation -> "encoded-" + invocation.getArgument(0, CharSequence.class));
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void save_shouldThrowWhenUsernameAlreadyExists() {
        UserCreateRequest request = TestEntityFactory.getUserCreateRequest();
        when(userRepository.existsByUsernameIgnoreCase(request.getUsername())).thenReturn(true);

        assertThrows(DuplicateRecordException.class, () -> userService.save(request));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void save_shouldReuseInactivePersonReactivatePersistUserAndSendEmailAfterCommit() {
        UserCreateRequest request = TestEntityFactory.getUserCreateRequest();
        Person existingPerson = personWithId(request.getPerson().getIdNumber(), false);
        Locale locale = Locale.forLanguageTag("es-CR");
        AtomicReference<User> savedRef = new AtomicReference<>();

        when(userRepository.existsByUsernameIgnoreCase(request.getUsername())).thenReturn(false);
        when(personService.findByIdNumber(request.getPerson().getIdNumber())).thenReturn(Optional.of(existingPerson));
        when(personService.getById(existingPerson.getId())).thenReturn(existingPerson);
        when(currentUserService.getCurrentUsername()).thenReturn("admin.user");

        LocaleContextHolder.setLocale(locale);

        runAndTriggerAfterCommit(() -> savedRef.set(userService.save(request)));

        User saved = savedRef.get();
        assertNotNull(saved);
        assertEquals(request.getUsername(), saved.getUsername());
        assertEquals("encoded-" + lastEncodedPassword(), saved.getPasswordHash());
        assertEquals(UserStatus.RESET, saved.getUserStatus());
        assertEquals(existingPerson, saved.getPerson());
        assertEquals("admin.user", saved.getCreatedBy());
        assertNotNull(saved.getCreatedAt());

        verify(personService).updateStatus(existingPerson.getId(), true);

        ArgumentCaptor<UserEmailInfo> emailCaptor = ArgumentCaptor.forClass(UserEmailInfo.class);
        ArgumentCaptor<String> plainPasswordCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Locale> localeCaptor = ArgumentCaptor.forClass(Locale.class);
        verify(emailService).sendNewUserEmail(emailCaptor.capture(), plainPasswordCaptor.capture(), localeCaptor.capture());

        assertEquals(request.getUsername(), emailCaptor.getValue().getUsername());
        assertEquals(existingPerson.getName(), emailCaptor.getValue().getPersonName());
        assertEquals(existingPerson.getLastname(), emailCaptor.getValue().getPersonLastname());
        assertNotNull(plainPasswordCaptor.getValue());
        assertFalse(plainPasswordCaptor.getValue().isBlank());
        assertEquals(locale, localeCaptor.getValue());
    }

    @Test
    void save_shouldCreatePersonWhenItDoesNotExist() {
        UserCreateRequest request = TestEntityFactory.getUserCreateRequest();
        Person createdPerson = personWithId(request.getPerson().getIdNumber(), true);
        AtomicReference<User> savedRef = new AtomicReference<>();

        when(userRepository.existsByUsernameIgnoreCase(request.getUsername())).thenReturn(false);
        when(personService.findByIdNumber(request.getPerson().getIdNumber())).thenReturn(Optional.empty());
        when(personService.save(request.getPerson())).thenReturn(createdPerson);
        when(personService.getById(createdPerson.getId())).thenReturn(createdPerson);
        when(currentUserService.getCurrentUsername()).thenReturn("creator");

        runAndTriggerAfterCommit(() -> savedRef.set(userService.save(request)));

        User saved = savedRef.get();
        assertNotNull(saved);
        assertEquals(createdPerson, saved.getPerson());
        assertEquals(UserStatus.RESET, saved.getUserStatus());
        assertEquals("creator", saved.getCreatedBy());

        verify(personService).save(request.getPerson());
        verify(personService, never()).updateStatus(any(UUID.class), anyBoolean());
        verify(emailService).sendNewUserEmail(any(UserEmailInfo.class), anyString(), any(Locale.class));
    }

    @Test
    void saveCommand_shouldThrowWhenUsernameAlreadyExists() {
        CreateUserCommand command = createCommand(UserProvider.LOCAL_EMAIL);
        when(userRepository.existsByUsernameIgnoreCase(command.getEmail())).thenReturn(true);

        assertThrows(DuplicateRecordException.class, () -> userService.save(command));

        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void saveCommand_shouldCreateLocalEmailUserAndSendEmailAfterCommit() {
        CreateUserCommand command = createCommand(UserProvider.LOCAL_EMAIL);
        Person createdPerson = personWithId(command.getIdNumber(), true);
        AtomicReference<User> savedRef = new AtomicReference<>();

        when(userRepository.existsByUsernameIgnoreCase(command.getEmail())).thenReturn(false);
        when(personService.findByIdNumber(command.getIdNumber())).thenReturn(Optional.empty());
        when(personService.save(any(PersonCreateCommand.class))).thenReturn(createdPerson);
        when(personService.getById(createdPerson.getId())).thenReturn(createdPerson);
        when(currentUserService.getCurrentUsername()).thenReturn("system");

        runAndTriggerAfterCommit(() -> savedRef.set(userService.save(command)));

        User saved = savedRef.get();
        assertNotNull(saved);
        assertEquals(command.getEmail(), saved.getUsername());
        assertEquals(command.getFirebaseUid(), saved.getExternalId());
        assertEquals(command.getNickname(), saved.getNickname());
        assertEquals(UserProvider.LOCAL_EMAIL, saved.getProvider());
        assertEquals(UserStatus.RESET, saved.getUserStatus());
        assertEquals("system", saved.getCreatedBy());

        ArgumentCaptor<PersonCreateCommand> personCommandCaptor = ArgumentCaptor.forClass(PersonCreateCommand.class);
        verify(personService).save(personCommandCaptor.capture());
        assertEquals(command.getIdNumber(), personCommandCaptor.getValue().getIdNumber());
        assertEquals(command.getName(), personCommandCaptor.getValue().getName());
        assertEquals(command.getLastname(), personCommandCaptor.getValue().getLastname());

        verify(emailService).sendNewUserEmail(any(UserEmailInfo.class), anyString(), any(Locale.class));
    }

    @Test
    void saveCommand_shouldCreateGoogleUserWithoutSendingEmail() {
        CreateUserCommand command = createCommand(UserProvider.GOOGLE);
        Person existingPerson = personWithId(command.getIdNumber(), true);
        AtomicReference<User> savedRef = new AtomicReference<>();

        when(userRepository.existsByUsernameIgnoreCase(command.getEmail())).thenReturn(false);
        when(personService.findByIdNumber(command.getIdNumber())).thenReturn(Optional.of(existingPerson));
        when(personService.getById(existingPerson.getId())).thenReturn(existingPerson);
        when(currentUserService.getCurrentUsername()).thenReturn("system");

        savedRef.set(userService.save(command));

        User saved = savedRef.get();
        assertNotNull(saved);
        assertEquals(UserProvider.GOOGLE, saved.getProvider());
        verifyNoInteractions(emailService);
    }

    @Test
    void resetUser_shouldThrowWhenUserDoesNotExist() {
        when(userRepository.findByUsernameIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> userService.resetUser(mock(Authentication.class), "missing@example.com"));
    }

    @Test
    void resetUser_shouldUpdatePasswordStatusAndSendEmailAfterCommit() {
        User user = userWithPerson("reset@example.com");
        Authentication authentication = mock(Authentication.class);
        Locale locale = Locale.US;

        when(userRepository.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
        when(authentication.getName()).thenReturn("security.admin");

        LocaleContextHolder.setLocale(locale);

        runAndTriggerAfterCommit(() -> userService.resetUser(authentication, user.getUsername()));

        assertEquals(UserStatus.RESET, user.getUserStatus());
        assertEquals("security.admin", user.getUpdatedBy());
        assertNotNull(user.getUpdatedAt());
        assertNotNull(user.getPasswordHash());
        assertTrue(user.getPasswordHash().startsWith("encoded-"));

        verify(userRepository).save(user);
        verify(emailService).sendPasswordEmail(any(UserEmailInfo.class), anyString(), eq(locale));
    }

    @Test
    void update_shouldThrowWhenUserDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RecordNotFoundException.class, () -> userService.update(id, new UserUpdateRequest()));
    }

    @Test
    void update_shouldApplyChangesAndPersistUser() {
        UUID id = UUID.randomUUID();
        User user = userWithPerson("update@example.com");
        user.setId(id);
        user.setNickname("old-nick");
        user.setUserStatus(UserStatus.INACTIVE);
        UserUpdateRequest request = TestEntityFactory.getUserUpdateRequest();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(currentUserService.getCurrentUsername()).thenReturn("editor");

        User updated = userService.update(id, request);

        assertSame(user, updated);
        assertEquals(request.getNickname(), updated.getNickname());
        assertEquals(UserStatus.ACTIVE, updated.getUserStatus());
        assertEquals("editor", updated.getUpdatedBy());
        assertNotNull(updated.getUpdatedAt());
        verify(userRepository).save(user);
    }

    @Test
    void delete_shouldMarkUserInactive() {
        UUID id = UUID.randomUUID();
        User user = userWithPerson("delete@example.com");
        user.setId(id);
        user.setUserStatus(UserStatus.ACTIVE);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(currentUserService.getCurrentUsername()).thenReturn("deleter");

        userService.delete(id);

        assertEquals(UserStatus.INACTIVE, user.getUserStatus());
        assertEquals("deleter", user.getUpdatedBy());
        assertNotNull(user.getUpdatedAt());
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_shouldActivateUserAndPersistEncodedPassword() {
        UUID id = UUID.randomUUID();
        User user = userWithPerson("password@example.com");
        user.setId(id);
        user.setUserStatus(UserStatus.RESET);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(currentUserService.getCurrentUsername()).thenReturn("password.admin");

        userService.resetPassword(id, "NewPassword123!");

        assertEquals(UserStatus.ACTIVE, user.getUserStatus());
        assertEquals("encoded-NewPassword123!", user.getPasswordHash());
        assertEquals("password.admin", user.getUpdatedBy());
        assertNotNull(user.getUpdatedAt());
        verify(userRepository).save(user);
    }

    @Test
    void queryMethods_shouldDelegateToRepositories() {
        UUID userId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        User user = userWithPerson("query@example.com");
        AppRoleUser appRoleUser = AppRoleUser.builder().id(UUID.randomUUID()).user(user).build();
        PageRequest pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userRepository.findAll(pageable)).thenReturn(page);
        when(userRepository.findByUsernameIgnoreCase(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.findByProviderAndExternalId(UserProvider.GOOGLE, "firebase-uid")).thenReturn(Optional.of(user));
        when(appRoleUserRepository.findByUserIdAndRoleApplicationId(userId, applicationId)).thenReturn(List.of(appRoleUser));

        assertEquals(Optional.of(user), userService.findById(userId));
        assertEquals(List.of(user), userService.findAll());
        assertEquals(page, userService.findAll(pageable));
        assertEquals(Optional.of(user), userService.findByUsername(user.getUsername()));
        assertEquals(Optional.of(user), userService.findByProviderAndExternalId(UserProvider.GOOGLE, "firebase-uid"));
        assertEquals(List.of(appRoleUser), userService.findAllAppRoleByApplicationId(userId, applicationId));
    }

    private void runAndTriggerAfterCommit(Runnable action) {
        TransactionSynchronizationManager.initSynchronization();
        try {
            action.run();
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private Person personWithId(String idNumber, boolean isActive) {
        Person person = TestEntityFactory.createPerson(idNumber, isActive);
        person.setId(UUID.randomUUID());
        return person;
    }

    private User userWithPerson(String username) {
        Person person = personWithId(TestEntityFactory.DEFAULT_ID_NUMBER, true);
        User user = TestEntityFactory.createUser(java.time.LocalDateTime.now(), TestEntityFactory.DEFAULT_USER);
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setPerson(person);
        return user;
    }

    private CreateUserCommand createCommand(UserProvider provider) {
        return CreateUserCommand.builder()
                .idNumber("ID-9988")
                .name("Alvaro")
                .lastname("Cascante")
                .firebaseUid("firebase-uid")
                .email("alvaro@example.com")
                .emailVerified(true)
                .nickname("alvarito")
                .provider(provider)
                .roleName("USER")
                .applicationCode("USR")
                .build();
    }

    private String lastEncodedPassword() {
        ArgumentCaptor<CharSequence> passwordCaptor = ArgumentCaptor.forClass(CharSequence.class);
        verify(passwordEncoder).encode(passwordCaptor.capture());
        return passwordCaptor.getValue().toString();
    }
}


