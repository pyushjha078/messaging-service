package com.messaging.service.security;

public class ConversationAccessException extends RuntimeException {
    public ConversationAccessException() {
        super("Conversation not found");
    }
}
