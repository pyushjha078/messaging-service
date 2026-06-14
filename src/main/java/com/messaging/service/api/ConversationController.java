package com.messaging.service.api;

import com.messaging.service.api.dto.MessageResponse;
import com.messaging.service.api.dto.PageResponse;
import com.messaging.service.api.dto.ConversationSummary;
import com.messaging.service.security.AuthenticatedUser;
import com.messaging.service.service.ConversationQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/conversations")
public class ConversationController{

    private final ConversationQueryService queryService;

    public ConversationController(ConversationQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    ResponseEntity<List<ConversationSummary>> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return ResponseEntity.ok(queryService.list(user.id()));
    }

    @GetMapping("/{id}/messages")
    ResponseEntity<PageResponse<MessageResponse>> history(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(required = false) String cursor
    ) {
        return ResponseEntity.ok(queryService.history(id, user.id(), cursor, limit));
    }
}
