package com.quetoquenana.userservice.service;

import com.google.firebase.auth.FirebaseToken;
import com.quetoquenana.userservice.command.CreateUserCommand;
import com.quetoquenana.userservice.dto.PersonCreateRequest;
import com.quetoquenana.userservice.dto.UserCreateFromFirebaseRequest;
import com.quetoquenana.userservice.dto.UserCreateFromFirebaseResponse;
import com.quetoquenana.userservice.model.AppRole;
import com.quetoquenana.userservice.model.AppRoleUser;
import com.quetoquenana.userservice.model.Application;
import com.quetoquenana.userservice.model.Person;
import com.quetoquenana.userservice.model.User;
import com.quetoquenana.userservice.model.UserProvider;
import com.quetoquenana.userservice.service.impl.AuthUserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;
import java.util.UUID;

import static com.quetoquenana.userservice.util.Constants.Headers.AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthUserServiceImplTest {

    private static final String FIREBASE_ID_TOKEN = "firebase-id-token";
    private static final String APP_CODE = "USR";
    private static final String APP_NAME = "User Service";
    private static final String USERNAME = "firebase-user@example.com";
    private static final String FIREBASE_UID = "firebase-uid";

    private ApplicationService applicationService;
    private FirebaseTokenVerifier firebaseTokenVerifier;
    private AuthUserServiceImpl authUserService;

    @BeforeEach
    void setUp() {
        applicationService = mock(ApplicationService.class);
        firebaseTokenVerifier = mock(FirebaseTokenVerifier.class);
        authUserService = new AuthUserServiceImpl(applicationService, firebaseTokenVerifier);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void getFirebaseSession_shouldVerifyTokenAndReturnMappedResponse() {
        setBearerToken();

        FirebaseToken decoded = mock(FirebaseToken.class);
        when(decoded.getUid()).thenReturn(FIREBASE_UID);
        when(firebaseTokenVerifier.verify(FIREBASE_ID_TOKEN)).thenReturn(decoded);

        AppRoleUser appRoleUser = buildAppRoleUser("alvarito");
        when(applicationService.getUser(APP_CODE, FIREBASE_UID)).thenReturn(appRoleUser);

        UserCreateFromFirebaseResponse response = authUserService.getFirebaseSession(APP_CODE);

        assertEquals(appRoleUser.getUser().getId().toString(), response.userId());
        assertEquals("1-2345-6789", response.idNumber());
        assertEquals("Alvaro", response.name());
        assertEquals("Cascante", response.lastname());
        assertEquals(USERNAME, response.username());
        assertEquals("alvarito", response.nickname());
        assertEquals(APP_NAME, response.applicationName());
        assertEquals(APP_CODE, response.applicationCode());

        verify(firebaseTokenVerifier).verify(FIREBASE_ID_TOKEN);
        verify(applicationService).getUser(APP_CODE, FIREBASE_UID);
    }

    @Test
    void createFromFirebase_shouldBuildCommandFromFirebaseTokenAndRequest() {
        setBearerToken();

        FirebaseToken decoded = mock(FirebaseToken.class);
        when(decoded.getUid()).thenReturn(FIREBASE_UID);
        when(decoded.getEmail()).thenReturn(USERNAME);
        when(decoded.isEmailVerified()).thenReturn(true);
        when(decoded.getName()).thenReturn("Firebase Name");
        when(decoded.getClaims()).thenReturn(Map.of(
                "firebase", Map.of("sign_in_provider", "password")
        ));

        when(firebaseTokenVerifier.verify(FIREBASE_ID_TOKEN)).thenReturn(decoded);

        AppRoleUser appRoleUser = buildAppRoleUser("Firebase Name");

        ArgumentCaptor<CreateUserCommand> captor = ArgumentCaptor.forClass(CreateUserCommand.class);
        when(applicationService.addUser(captor.capture())).thenReturn(appRoleUser);

        UserCreateFromFirebaseRequest request = new UserCreateFromFirebaseRequest(
                new PersonCreateRequest("1-2345-6789", "Alvaro", "Cascante"),
                null
        );

        UserCreateFromFirebaseResponse response = authUserService.createFromFirebase(request, APP_CODE);

        CreateUserCommand command = captor.getValue();
        assertEquals("1-2345-6789", command.getIdNumber());
        assertEquals("Alvaro", command.getName());
        assertEquals("Cascante", command.getLastname());
        assertEquals(FIREBASE_UID, command.getFirebaseUid());
        assertEquals(USERNAME, command.getEmail());
        assertTrue(command.isEmailVerified());
        assertEquals("Firebase Name", command.getNickname());
        assertEquals(UserProvider.PASSWORD, command.getProvider());
        assertEquals("USER", command.getRoleName());
        assertEquals(APP_CODE, command.getApplicationCode());

        assertEquals(USERNAME, response.username());
        assertEquals(APP_CODE, response.applicationCode());

        verify(firebaseTokenVerifier).verify(FIREBASE_ID_TOKEN);
        verify(applicationService).addUser(any(CreateUserCommand.class));
    }

    @Test
    void getFirebaseSession_shouldThrowWhenAuthorizationHeaderMissing() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authUserService.getFirebaseSession(APP_CODE)
        );

        assertEquals("Missing Authorization header", ex.getMessage());
        verifyNoInteractions(firebaseTokenVerifier, applicationService);
    }

    private void setBearerToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION, "Bearer " + FIREBASE_ID_TOKEN);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private AppRoleUser buildAppRoleUser(String nickname) {
        Application application = Application.builder()
                .id(UUID.randomUUID())
                .name(APP_NAME)
                .code(APP_CODE)
                .build();

        AppRole role = AppRole.builder()
                .id(UUID.randomUUID())
                .application(application)
                .roleName("USER")
                .build();

        Person person = Person.builder()
                .id(UUID.randomUUID())
                .idNumber("1-2345-6789")
                .name("Alvaro")
                .lastname("Cascante")
                .isActive(true)
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .person(person)
                .username(USERNAME)
                .externalId(FIREBASE_UID)
                .nickname(nickname)
                .build();

        return AppRoleUser.builder()
                .id(UUID.randomUUID())
                .user(user)
                .role(role)
                .build();
    }
}


