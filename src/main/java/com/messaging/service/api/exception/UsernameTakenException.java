package com.messaging.service.api.exception;

public class UsernameTakenException extends RuntimeException {
    public UsernameTakenException(String message) {
        super("Username is already taken "+ message);
    }
}
