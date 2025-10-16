package com.quetoquenana.personservice.exception;

import lombok.Getter;

@Getter
public class ImmutableFieldModificationException extends RuntimeException {
    public static final String DEFAULT_MESSAGE_KEY = "error.immutable.field.modification";
    private final String messageKey;
    private final Object[] messageArgs;

    public ImmutableFieldModificationException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
    }

    public ImmutableFieldModificationException() {
        super();
        this.messageKey = DEFAULT_MESSAGE_KEY;
        this.messageArgs = null;
    }

}
