package com.messaging.service.domain;

import java.util.UUID;

public final class ParticipantKey {

    public static String canonical(UUID a, UUID b) {
        if (a.equals(b)) {
            throw new IllegalArgumentException("Cannot create key for same user");
        }
        // Always put the smaller UUID first → (A,B) and (B,A) produce the same key
        return a.compareTo(b) < 0
                ? a + ":" + b
                : b + ":" + a;
    }
}