package com.dhyey.chatapp_withjpa.exceptions;

public class UnauthorizedMessageException extends RuntimeException {

    public UnauthorizedMessageException(String message) {
        super(message);
    }

    public UnauthorizedMessageException(String message, Throwable cause) {
        super(message, cause);
    }
}
