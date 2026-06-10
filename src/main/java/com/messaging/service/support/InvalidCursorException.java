package com.messaging.service.support;

public class InvalidCursorException extends RuntimeException{
    public InvalidCursorException(String message) {
        super(message);
    }
}
