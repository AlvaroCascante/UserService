package com.quetoquenana.userservice.exception;

import lombok.Getter;

@Getter
public class AuthenticationException extends RuntimeException {
    public static final String DEFAULT_MESSAGE_KEY = "error.authentication";
    private final String messageKey;
    private final Object[] messageArgs;

    public AuthenticationException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
    }

    public AuthenticationException() {
        super();
        this.messageKey = DEFAULT_MESSAGE_KEY;
        this.messageArgs = null;
    }

}