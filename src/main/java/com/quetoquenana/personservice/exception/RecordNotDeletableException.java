package com.quetoquenana.personservice.exception;

import lombok.Getter;

@Getter
public class RecordNotDeletableException extends RuntimeException {
    public static final String DEFAULT_MESSAGE_KEY = "error.record.not.deletable";
    private final String messageKey;
    private final Object[] messageArgs;

    public RecordNotDeletableException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
    }

    public RecordNotDeletableException() {
        super();
        this.messageKey = DEFAULT_MESSAGE_KEY;
        this.messageArgs = null;
    }

}
