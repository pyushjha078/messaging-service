package com.messaging.service.api;

import com.messaging.service.api.dto.MessageResponse;
import com.messaging.service.api.dto.SendMessageRequest;
import com.messaging.service.security.AuthenticatedUser;
import com.messaging.service.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
     ResponseEntity<MessageResponse> send(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody SendMessageRequest req
    ) {
        MessageResponse res = messageService.send(user.id(), req);
        URI location = URI.create("/conversations/" + res.conversationId()
                + "/messages/" + res.id());
        return ResponseEntity.created(location).body(res);
    }
}
