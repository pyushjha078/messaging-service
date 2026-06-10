package com.messaging.service.security;

import org.springframework.stereotype.Component;

import java.util.UUID;

import com.messaging.service.domain.Conversation;
import com.messaging.service.repository.ConversationRepository;

@Component
public class ConversationAccessGuard {

    private final ConversationRepository convRepo;

    public ConversationAccessGuard(ConversationRepository convRepo) {
        this.convRepo = convRepo;
    }

    public Conversation requireParticipant(UUID convId, UUID userId) {
        Conversation conv = convRepo.findById(convId)
                .orElseThrow(ConversationAccessException::new);
        if (!userId.equals(conv.getUserAId()) && !userId.equals(conv.getUserBId()))
            throw new ConversationAccessException();
        return conv;
    }
}
