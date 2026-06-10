package com.messaging.service.integration;

import com.messaging.service.api.dto.MessageResponse;
import com.messaging.service.api.dto.SendMessageRequest;
import com.messaging.service.api.exception.ServiceErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SendMessageIntegrationTest")
public class SendMessageIntegrationTest extends BaseIntegrationTest {

    @Test
    void sendPersistsTheMessageAndCreatesAConversation() {
        // Given
        UserWithToken alice = registerAndLogin("alice", "password123");
        UserWithToken bob = registerAndLogin("bob", "password123");

        // When
        SendMessageRequest request = new SendMessageRequest(bob.user().getId(), "Hello Bob!");
        ResponseEntity<MessageResponse> response = send(alice.token(), request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().body()).isEqualTo("Hello Bob!");
        assertThat(response.getBody().conversationId()).isNotNull();
        assertThat(messageRepository.count()).isEqualTo(1);
        assertThat(conversationRepository.count()).isEqualTo(1);
    }

    @Test
    void sendToSelfIsRejected() {
        UserWithToken alice = registerAndLogin("alice_self", "password123");

        ResponseEntity<ServiceErrorResponse> response = sendError(
                alice.token(),
                new SendMessageRequest(alice.user().getId(), "can not self send")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("SELF_MESSAGE");
    }

    @Test
    void sendToMissingRecipientIsRejected() {
        UserWithToken alice = registerAndLogin("alice_missing", "password123");

        ResponseEntity<ServiceErrorResponse> response = sendError(
                alice.token(),
                new SendMessageRequest(java.util.UUID.randomUUID(), "hello unknown")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("RECIPIENT_NOT_FOUND");
    }

    private ResponseEntity<ServiceErrorResponse> sendError(String token, SendMessageRequest request) {
        return post(
                token,
                "/messages",
                request,
                new ParameterizedTypeReference<ServiceErrorResponse>() {
                }
        );
    }
}
