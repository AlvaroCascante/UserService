package com.quetoquenana.personservice.exception;

import lombok.Getter;

@Getter
public class RecordNotFoundException extends RuntimeException {
    public static final String DEFAULT_MESSAGE_KEY = "error.record.not.found";
    private final String messageKey;
    private final Object[] messageArgs;

    public RecordNotFoundException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
    }

    public RecordNotFoundException() {
        super();
        this.messageKey = DEFAULT_MESSAGE_KEY;
        this.messageArgs = null;
    }
}
