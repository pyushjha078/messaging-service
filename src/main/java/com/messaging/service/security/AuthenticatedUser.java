package com.messaging.service.security;

import java.util.UUID;

public record AuthenticatedUser(UUID id, String userName) {
}
