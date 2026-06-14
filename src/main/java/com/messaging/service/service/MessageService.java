package com.messaging.service.service;

import com.messaging.service.api.dto.MessageResponse;
import com.messaging.service.api.dto.SendMessageRequest;
import com.messaging.service.api.exception.RecipientNotFoundException;
import com.messaging.service.api.exception.SelfMessageException;
import com.messaging.service.domain.Conversation;
import com.messaging.service.domain.Message;
import com.messaging.service.domain.ParticipantKey;
import com.messaging.service.repository.MessageRepository;
import com.messaging.service.repository.ConversationRepository;
import com.messaging.service.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service @Transactional
public class MessageService {

    private final UserRepository userRepo;
    private final MessageRepository messageRepo;
    private final ConversationRepository convRepo;

    public MessageService(UserRepository userRepo, MessageRepository messageRepo, ConversationRepository convRepo) {
        this.userRepo = userRepo;
        this.messageRepo = messageRepo;
        this.convRepo = convRepo;
    }

    public MessageResponse send(UUID senderId, SendMessageRequest req) {
        // 1. Can't send to yourself
        if (senderId.equals(req.recipientId()))
            throw new SelfMessageException("You can't send a message to yourself");

        // 2. Recipient must exist
        userRepo.findById(req.recipientId())
                .orElseThrow(() -> new RecipientNotFoundException(req.recipientId().toString()));

        // 3. Get or create the conversation — race-safe
        String key = ParticipantKey.canonical(senderId, req.recipientId());
        Conversation conv = getOrCreateConversation(key, senderId, req.recipientId());

        // 4. Save the message
        Message msg = messageRepo.save(Message.builder()
                .conversationId(conv.getId())
                .senderId(senderId)
                .body(req.body())
                .build());

        return toResponse(msg);
    }

    private Conversation getOrCreateConversation(String key, UUID a, UUID b) {
        return convRepo.findByParticipantKey(key).orElseGet(() -> {
            try {
                return convRepo.saveAndFlush(Conversation.builder()
                        .participantKey(key)
                        .userAId(a.compareTo(b) < 0 ? a : b)
                        .userBId(a.compareTo(b) < 0 ? b : a)
                        .build());
            } catch (DataIntegrityViolationException race) {
                // Another thread just created it — fetch and return
                return convRepo.findByParticipantKey(key).orElseThrow();
            }
        });
    }

    private MessageResponse toResponse(Message msg){
        return new MessageResponse(String.valueOf(msg.getId()),msg.getConversationId(),msg.getSenderId(),msg.getBody(),msg.getCreatedAt());
    }
}
