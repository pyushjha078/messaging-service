package com.messaging.service.api.exception;

public class SelfMessageException extends RuntimeException {
    public SelfMessageException(String message) {
        super("Self Message Exception");
    }
}
