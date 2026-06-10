package com.messaging.service.api.dto;

import java.util.UUID;
import java.time.Instant;

public record MessageResponse(
        String id,           // String, not Long — avoids JS precision loss on large numbers
        UUID conversationId,
        UUID senderId,
        String body,
        Instant createdAt
) {}