package com.messaging.service.api.dto;

import java.time.Instant;
import java.util.UUID;

public record ConversationSummary(UUID conversationId,
                                  OtherUser otherUser,
                                  LastMessage lastMessage,
                                  Instant createdAt) {
    public record OtherUser(UUID id, String username) {
    }

    public record LastMessage(String id, String body,UUID senderId,Instant createdAt) {
    }
}
