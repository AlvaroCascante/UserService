package com.quetoquenana.userservice.exception;

import com.quetoquenana.userservice.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.MissingRequestHeaderException;

import java.lang.reflect.Method;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControllerExceptionAdviceTest {

    private ControllerExceptionAdvice advice;
    private MessageSource messageSource;

    @BeforeEach
    void setUp() {
        advice = new ControllerExceptionAdvice();
        messageSource = mock(MessageSource.class);
        ReflectionTestUtils.setField(advice, "messageSource", messageSource);
    }

    @Test
    void handleExpiredTokenException_shouldReturnUnauthorizedAndSpecificErrorCode() {
        when(messageSource.getMessage(eq(ExpiredTokenException.MESSAGE_KEY), nullable(Object[].class), eq(Locale.ENGLISH)))
                .thenReturn("Token expired.");

        ResponseEntity<ApiResponse> response = advice.handleExpiredTokenException(new ExpiredTokenException(), Locale.ENGLISH);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Token expired.", response.getBody().getMessage());
        assertEquals(ExpiredTokenException.ERROR_CODE, response.getBody().getErrorCode());
    }

    @Test
    void handleMissingRequestHeader_shouldReturnBadRequest() {
        MissingRequestHeaderException exception = new MissingRequestHeaderException(
                "X-Application-Name",
                methodParameter("sampleHeader", String.class)
        );

        ResponseEntity<ApiResponse> response = advice.handleMissingRequestHeader(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getErrorCode());
        assertEquals(
                "BAD_REQUEST -- Required request header 'X-Application-Name' for method parameter type String is not present",
                response.getBody().getMessage()
        );
    }

    private MethodParameter methodParameter(String methodName, Class<?>... parameterTypes) {
        try {
            Method method = ControllerExceptionAdviceTest.class.getDeclaredMethod(methodName, parameterTypes);
            return new MethodParameter(method, 0);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to create MethodParameter for test", e);
        }
    }

    @SuppressWarnings("unused")
    private void sampleHeader(String value) {
        // helper method for MethodParameter creation in tests
    }
}

