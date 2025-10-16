package com.quetoquenana.personservice.exception;

import lombok.Getter;

@Getter
public class InactiveRecordException extends RuntimeException {
    public static final String DEFAULT_MESSAGE_KEY = "error.inactive.record";
    private final String messageKey;
    private final Object[] messageArgs;

    public InactiveRecordException(String messageKey, Object... messageArgs) {
        super(messageKey);
        this.messageKey = messageKey;
        this.messageArgs = messageArgs;
    }

    public InactiveRecordException() {
        super();
        this.messageKey = DEFAULT_MESSAGE_KEY;
        this.messageArgs = null;
    }

}
