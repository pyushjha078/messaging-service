package com.messaging.service.service;

import com.messaging.service.api.dto.ConversationSummary;
import com.messaging.service.api.dto.PageResponse;
import com.messaging.service.api.dto.MessageResponse;
import com.messaging.service.domain.Conversation;
import com.messaging.service.domain.Message;
import com.messaging.service.domain.MyUser;
import com.messaging.service.repository.MessageRepository;
import com.messaging.service.repository.ConversationRepository;
import com.messaging.service.repository.UserRepository;
import com.messaging.service.security.ConversationAccessGuard;
import com.messaging.service.support.Cursor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
@Transactional(readOnly = true)
public class ConversationQueryService {

    private final ConversationRepository convRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final ConversationAccessGuard guard;

    public ConversationQueryService(ConversationRepository convRepo, MessageRepository messageRepo, UserRepository userRepo, ConversationAccessGuard guard) {
        this.convRepo = convRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
        this.guard = guard;
    }

    public PageResponse<MessageResponse> history(UUID convId, UUID userId,
                                                 String cursorStr, int limit) {
        // 1. Check access
        guard.requireParticipant(convId, userId);

        // 2. Clamp limit
        int safeLimit = Math.min(Math.max(limit, 1), 100);

        // 3. Decode cursor (null means start from the top)
        long cursorId = cursorStr == null ? Long.MAX_VALUE : Cursor.decode(cursorStr);

        // 4. Fetch one extra row to detect "has more"
        List<Message> rows = messageRepo
                .findByConversationIdAndIdLessThanOrderByIdDesc(
                        convId, cursorId, PageRequest.of(0, safeLimit + 1)
                );

        boolean hasMore = rows.size() > safeLimit;
        List<Message> page = hasMore ? rows.subList(0, safeLimit) : rows;

        // 5. Build next cursor from the last item's id
        String nextCursor = hasMore
                ? Cursor.encode(page.get(page.size() - 1).getId())
                : null;

        return new PageResponse<>(page.stream().map(this::toResponse).toList(), nextCursor);
    }

    public List<ConversationSummary> list(UUID userId) {
        List<Conversation> convs = convRepo.findAllByParticipant(userId);  // ordered by last message id DESC

        // Collect the "other" user id from each conversation
        Set<UUID> otherIds = convs.stream()
                .map(c -> c.getUserAId().equals(userId) ? c.getUserBId() : c.getUserAId())
                .collect(toSet());

        // Single IN query to load all the other users at once — avoids N+1
        Map<UUID, MyUser> usersById = userRepo.findAllById(otherIds)
                .stream().collect(toMap(MyUser::getId, u -> u));

        return convs.stream().map(c -> {
            UUID otherId = c.getUserAId().equals(userId) ? c.getUserBId() : c.getUserAId();
            MyUser other = usersById.get(otherId);
            Message lastMsg = messageRepo.findTopByConversationIdOrderByIdDesc(c.getId())
                    .orElse(null);
            return new ConversationSummary(c.getId(), new ConversationSummary.OtherUser(other.getId(), other.getUsername()), lastMsg == null ? null : new ConversationSummary.LastMessage(String.valueOf(lastMsg.getId()), lastMsg.getBody(), lastMsg.getSenderId(), lastMsg.getCreatedAt()), c.getCreatedAt());
        }).toList();
    }

    private MessageResponse toResponse(Message msg) {
        return new MessageResponse(String.valueOf(msg.getId()), msg.getConversationId(), msg.getSenderId(), msg.getBody(), msg.getCreatedAt());
    }
}
