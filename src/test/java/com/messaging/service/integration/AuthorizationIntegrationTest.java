package com.messaging.service.integration;

import com.messaging.service.api.dto.MessageResponse;
import com.messaging.service.api.dto.SendMessageRequest;
import com.messaging.service.api.exception.ServiceErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthorizationIntegrationTest")
public class AuthorizationIntegrationTest extends BaseIntegrationTest {

    @Test
    void aNonParticipantCannotReadAConversation() {
        // Given Alice and Bob share a conversation
        UserWithToken alice = registerAndLogin("alice_a");
        UserWithToken bob = registerAndLogin("bob_a");
        UserWithToken carol = registerAndLogin("carol_a");

        send(alice.token(), new SendMessageRequest(bob.user().getId(), "private message"));
        UUID convId = getConversationId(alice.token(), bob.user().getId());

        // When Carol tries to read the conversation
        ResponseEntity<ServiceErrorResponse> response = getHistoryWithError(carol.token(), convId);

        // Then Carol gets 404, not 403, and no message content is leaked
        assertThat(response.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(response.getBody().message()).contains("not found");
    }

    @Test
    void aNonParticipantGetsTheSame404AsANonExistentConversation() {
        // Given
        UserWithToken carol = registerAndLogin("carol_b");

        // Create an existing conversation for another user, so carol is a non-participant
        UserWithToken alice = registerAndLogin("alice_c");
        UserWithToken bob = registerAndLogin("bob_c");
        send(alice.token(), new SendMessageRequest(bob.user().getId(), "some message"));
        UUID existingConvId = getConversationId(alice.token(), bob.user().getId());

        // When
        ResponseEntity<ServiceErrorResponse> resReal = getHistoryWithError(carol.token(), existingConvId);
        ResponseEntity<ServiceErrorResponse> resFake = getHistoryWithError(carol.token(), UUID.randomUUID());

        // Then Both return 404 with the same error shape
        assertThat(resReal.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(resFake.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(resReal.getBody().code()).isEqualTo(resFake.getBody().code());
    }

    private UUID getConversationId(String token, UUID recipientId) {
        // Send a message to ensure a conversation exists and retrieve its ID
        ResponseEntity<MessageResponse> response = send(token, new SendMessageRequest(recipientId, "dummy"));
        return response.getBody().conversationId();
    }

    private ResponseEntity<ServiceErrorResponse> getHistoryWithError(String token, UUID convId) {
        return get(
                token,
                "/conversations/" + convId + "/messages",
                new ParameterizedTypeReference<ServiceErrorResponse>() {}
        );
    }
}
