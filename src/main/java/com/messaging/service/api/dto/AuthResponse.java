package com.messaging.service.api.dto;

import java.time.Instant;

public record AuthResponse(String accessToken, Instant expiresAt) {}