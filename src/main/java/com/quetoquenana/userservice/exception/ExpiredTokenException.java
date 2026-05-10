package com.quetoquenana.userservice.exception;

public class ExpiredTokenException extends AuthenticationException {

    public static final String MESSAGE_KEY = "error.authentication.token.expired";
    public static final int ERROR_CODE = 40101;

    public ExpiredTokenException() {
        super(MESSAGE_KEY, (Object[]) null);
    }

    public ExpiredTokenException(Throwable cause) {
        super(MESSAGE_KEY, (Object[]) null);
        initCause(cause);
    }
}

