package com.messaging.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.messaging.service.domain.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByParticipantKey(String key);

    @Query("""
        SELECT c FROM Conversation c
        WHERE c.userAId = :uid OR c.userBId = :uid
        ORDER BY (
            SELECT MAX(m.id) FROM Message m WHERE m.conversationId = c.id
        ) DESC NULLS LAST
        """)
    List<Conversation> findAllByParticipant(@Param("uid") UUID uid);
}