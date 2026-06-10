package com.messaging.service.api.exception;

public class RecipientNotFoundException extends RuntimeException {
    public RecipientNotFoundException(String message) {
        super("Recepient not found");
    }
}
