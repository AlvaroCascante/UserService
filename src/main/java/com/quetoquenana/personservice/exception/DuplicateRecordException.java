package com.quetoquenana.personservice.exception;

import lombok.Getter;

@Getter
public class DuplicateRecordException extends RuntimeException {
    public static final String DEFAULT_MESSAGE_KEY = "error.duplicate.record";
    private final String messageKey;
    private final Object[] messageArgs;

    public DuplicateRecordException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
    }

    public DuplicateRecordException() {
        super();
        this.messageKey = DEFAULT_MESSAGE_KEY;
        this.messageArgs = null;
    }

}