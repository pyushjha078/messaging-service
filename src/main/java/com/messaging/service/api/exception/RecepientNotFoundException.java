package com.messaging.service.api.exception;

public class RecepientNotFoundException extends RuntimeException {
    public RecepientNotFoundException(String message) {
        super("Recepient not found");
    }
}
