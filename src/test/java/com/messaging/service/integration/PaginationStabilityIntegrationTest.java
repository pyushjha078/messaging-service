package com.messaging.service.integration;

import com.messaging.service.api.dto.MessageResponse;
import com.messaging.service.api.dto.PageResponse;
import com.messaging.service.api.dto.SendMessageRequest;
import com.messaging.service.api.exception.ServiceErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PaginationStabilityIntegrationTest")
public class PaginationStabilityIntegrationTest extends BaseIntegrationTest {

    @Test
    void paginationIsStableWhenNewMessagesArriveMidWalk() {
        // Given Alice and Bob have 50 messages
        UserWithToken alice = registerAndLogin("alice_p");
        UserWithToken bob = registerAndLogin("bob_p");

        UUID convId = sendInitialMessages(alice, bob, 50);

        // When Alice fetches page 1
        ResponseEntity<PageResponse<MessageResponse>> page1 = getHistory(alice.token(), convId, 20, null);
        List<String> page1Ids = page1.getBody().items().stream().map(MessageResponse::id).toList();
        long highestIdBeforeNewMessages = Long.parseLong(page1Ids.get(0));

        // And Bob sends 10 new messages while Alice is reading
        IntStream.range(0, 10).forEach(i -> send(bob.token(), new SendMessageRequest(alice.user().getId(), "new-" + i)));

        // And Alice fetches page 2 using the cursor from page 1
        ResponseEntity<PageResponse<MessageResponse>> page2 = getHistory(alice.token(), convId, 20, page1.getBody().nextCursor());
        List<String> page2Ids = page2.getBody().items().stream().map(MessageResponse::id).toList();

        // Then No duplicates between page 1 and page 2
        assertThat(page1Ids).doesNotContainAnyElementsOf(page2Ids);
        assertThat(page1Ids.size() + page2Ids.size()).isEqualTo(40);

        // And New messages don't appear in the existing pages
        page1Ids.forEach(id -> assertThat(Long.parseLong(id)).isLessThanOrEqualTo(highestIdBeforeNewMessages));
        page2Ids.forEach(id -> assertThat(Long.parseLong(id)).isLessThanOrEqualTo(highestIdBeforeNewMessages));

        // And All ids are strictly descending across both pages
        List<Long> allIds = page1Ids.stream().map(Long::parseLong).collect(Collectors.toList());
        allIds.addAll(page2Ids.stream().map(Long::parseLong).toList());
        assertThat(allIds).isSortedAccordingTo(Comparator.reverseOrder());
    }

    @Test
    void invalidCursorIsRejected() {
        UserWithToken alice = registerAndLogin("alice_cursor", "password123");
        UserWithToken bob = registerAndLogin("bob_cursor", "password123");
        UUID convId = sendInitialMessages(alice, bob, 2);

        ResponseEntity<ServiceErrorResponse> response = getHistoryError(
                alice.token(),
                convId,
                "not-a-valid-cursor"
        );

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_CURSOR");
    }

    private UUID sendInitialMessages(UserWithToken sender, UserWithToken recipient, int count) {
        UUID conversationId = null;
        for (int i = 0; i < count; i++) {
            ResponseEntity<MessageResponse> response = send(sender.token(), new SendMessageRequest(recipient.user().getId(), "msg-" + i));
            if (conversationId == null) {
                conversationId = response.getBody().conversationId();
            }
        }
        return conversationId;
    }

    private ResponseEntity<PageResponse<MessageResponse>> getHistory(String token, UUID convId, int limit, String cursor) {
        return get(
                token,
                uriBuilder -> uriBuilder.path("/conversations/{convId}/messages")
                        .queryParam("limit", limit)
                        .queryParamIfPresent("cursor", Optional.ofNullable(cursor))
                        .build(convId),
                new ParameterizedTypeReference<PageResponse<MessageResponse>>() {}
        );
    }

    private ResponseEntity<ServiceErrorResponse> getHistoryError(String token, UUID convId, String cursor) {
        return get(
                token,
                uriBuilder -> uriBuilder.path("/conversations/{convId}/messages")
                        .queryParam("cursor", cursor)
                        .build(convId),
                new ParameterizedTypeReference<ServiceErrorResponse>() {}
        );
    }

}
