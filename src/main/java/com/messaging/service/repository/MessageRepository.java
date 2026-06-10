package com.messaging.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.messaging.service.domain.Message;
import org.springframework.data.domain.Pageable;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Core cursor pagination query
    List<Message> findByConversationIdAndIdLessThanOrderByIdDesc(
            UUID conversationId, Long cursor, Pageable pageable
    );

    // Fetch the most recent message — used for conversation list preview
    Optional<Message> findTopByConversationIdOrderByIdDesc(UUID conversationId);
}