package com.quetoquenana.userservice.controller;

import com.quetoquenana.userservice.dto.ApiResponse;
import com.quetoquenana.userservice.dto.ChangePasswordRequest;
import com.quetoquenana.userservice.dto.CompleteRegistrationResponse;
import com.quetoquenana.userservice.dto.PersonCreateRequest;
import com.quetoquenana.userservice.dto.ResetUserRequest;
import com.quetoquenana.userservice.dto.TokenResponse;
import com.quetoquenana.userservice.dto.UserCreateFromFirebaseRequest;
import com.quetoquenana.userservice.dto.UserCreateFromFirebaseResponse;
import com.quetoquenana.userservice.service.AuthUserService;
import com.quetoquenana.userservice.service.SecurityService;
import com.quetoquenana.userservice.service.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Mock
    private SecurityService securityService;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthUserService authUserService;

    @InjectMocks
    private AuthController authController;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    void login_shouldReturnTokensAndDelegateToServices() {
        Authentication authentication = mock(Authentication.class);
        TokenResponse expected = new TokenResponse("access-token", "refresh-token", 3600L);

        when(authentication.getName()).thenReturn("alvaro@example.com");
        when(tokenService.createTokens(authentication, "USR")).thenReturn(expected);

        ResponseEntity<TokenResponse> response = authController.login(authentication, "USR");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());
        verify(securityService).login(authentication);
        verify(tokenService).createTokens(authentication, "USR");
    }

    @Test
    void reset_shouldReturnNoContentAndDelegateToSecurityService() {
        Authentication authentication = mock(Authentication.class);
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("NewPassword123!");

        when(authentication.getName()).thenReturn("alvaro@example.com");

        ResponseEntity<Void> response = authController.reset(authentication, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(securityService).resetUser(authentication, request);
    }

    @Test
    void refresh_shouldReturnTokensFromTokenService() {
        Authentication authentication = mock(Authentication.class);
        TokenResponse expected = new TokenResponse("access-token", "next-refresh-token", 3600L);

        when(tokenService.refresh(authentication, "USR")).thenReturn(expected);

        ResponseEntity<TokenResponse> response = authController.refresh(authentication, "USR");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());
        verify(tokenService).refresh(authentication, "USR");
    }

    @Test
    void forgotPassword_shouldReturnNoContentAndDelegateToSecurityService() {
        ResetUserRequest request = new ResetUserRequest();
        request.setUsername("alvaro@example.com");

        ResponseEntity<Void> response = authController.forgotPassword(request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(securityService).forgotPassword("alvaro@example.com");
    }

    @Test
    void completeRegistrationFromFirebase_shouldReturnCreatedWrappedResponse() {
        UserCreateFromFirebaseRequest request = new UserCreateFromFirebaseRequest(
                new PersonCreateRequest("1-2345-6789", "Alvaro", "Cascante"),
                "alvarito"
        );
        UserCreateFromFirebaseResponse user = new UserCreateFromFirebaseResponse(
                "12345679",
                "1-2345-6789",
                "Alvaro",
                "Cascante",
                "alvaro@example.com",
                "alvarito",
                "User Service",
                "USR"
        );
        TokenResponse tokens = new TokenResponse("access-token", "refresh-token", 3600L);

        when(authUserService.createFromFirebase(request, "USR")).thenReturn(user);
        when(tokenService.createTokensForUser("alvaro@example.com", "USR")).thenReturn(tokens);

        ResponseEntity<ApiResponse> response = authController.completeRegistrationFromFirebase(request, "USR");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Success", apiResponse.getMessage());
        assertEquals(0, apiResponse.getErrorCode());
        assertInstanceOf(Map.class, apiResponse.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) apiResponse.getData();
        Object registrationObject = data.get("registration");
        assertNotNull(registrationObject);
        assertInstanceOf(CompleteRegistrationResponse.class, registrationObject);

        CompleteRegistrationResponse registration = (CompleteRegistrationResponse) registrationObject;
        assertSame(tokens, registration.getTokenResponse());
        assertSame(user, registration.getUser());

        verify(authUserService).createFromFirebase(request, "USR");
        verify(tokenService).createTokensForUser(user.username(), "USR");
    }

    @Test
    void checkForFirebaseSession_shouldReturnCreatedWrappedResponse() {
        UserCreateFromFirebaseResponse user = new UserCreateFromFirebaseResponse(
                "12345679",
                "1-2345-6789",
                "Alvaro",
                "Cascante",
                "alvaro@example.com",
                "alvarito",
                "User Service",
                "USR"
        );
        TokenResponse tokens = new TokenResponse("access-token", "refresh-token", 3600L);

        when(authUserService.getFirebaseSession("USR")).thenReturn(user);
        when(tokenService.createTokensForUser("alvaro@example.com", "USR")).thenReturn(tokens);

        ResponseEntity<ApiResponse> response = authController.checkForFirebaseSession("USR");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponse apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Success", apiResponse.getMessage());
        assertEquals(0, apiResponse.getErrorCode());
        assertInstanceOf(Map.class, apiResponse.getData());

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) apiResponse.getData();
        Object registrationObject = data.get("registration");
        assertNotNull(registrationObject);
        assertInstanceOf(CompleteRegistrationResponse.class, registrationObject);

        CompleteRegistrationResponse registration = (CompleteRegistrationResponse) registrationObject;
        assertSame(tokens, registration.getTokenResponse());
        assertSame(user, registration.getUser());

        verify(authUserService).getFirebaseSession("USR");
        verify(tokenService).createTokensForUser(user.username(), "USR");
    }
}

