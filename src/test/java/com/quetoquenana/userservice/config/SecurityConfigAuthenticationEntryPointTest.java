package com.quetoquenana.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quetoquenana.userservice.exception.ExpiredTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityConfigAuthenticationEntryPointTest {

    private AuthenticationEntryPoint authenticationEntryPoint;

    @BeforeEach
    void setUp() {
        SecurityConfig securityConfig = new SecurityConfig(mock(CorsConfigProperties.class), mock(RsaKeyProperties.class));
        MessageSource messageSource = mock(MessageSource.class);
        when(messageSource.getMessage(eq(ExpiredTokenException.MESSAGE_KEY), eq(null), eq(Locale.ENGLISH)))
                .thenReturn("Token expired.");
        authenticationEntryPoint = securityConfig.authenticationEntryPoint(new ObjectMapper(), messageSource);
    }

    @Test
    void authenticationEntryPoint_shouldReturnExpiredTokenPayload() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addPreferredLocale(Locale.ENGLISH);
        MockHttpServletResponse response = new MockHttpServletResponse();

        JwtValidationException expired = new JwtValidationException(
                "Token expired",
                List.of(new OAuth2Error("invalid_token", "Jwt expired at 2026-05-10T12:00:00Z", null))
        );

        authenticationEntryPoint.commence(request, response, new BadCredentialsException("Bad credentials", expired));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Token expired."));
        assertTrue(response.getContentAsString().contains(String.valueOf(ExpiredTokenException.ERROR_CODE)));
    }
}

