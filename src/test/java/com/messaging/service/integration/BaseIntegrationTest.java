package com.messaging.service.integration;

import com.messaging.service.api.dto.AuthResponse;
import com.messaging.service.api.dto.MessageResponse;
import com.messaging.service.api.dto.LoginRequest;
import com.messaging.service.api.dto.RegisterRequest;
import com.messaging.service.api.dto.SendMessageRequest;
import com.messaging.service.domain.MyUser;
import com.messaging.service.repository.ConversationRepository;
import com.messaging.service.repository.MessageRepository;
import com.messaging.service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseIntegrationTest {
    @LocalServerPort protected int port;
    protected WebTestClient http;
    @Autowired protected UserRepository userRepository;
    @Autowired protected ConversationRepository conversationRepository;
    @Autowired protected MessageRepository messageRepository;

    @BeforeEach
    void setUpWebClient() {
        http = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        messageRepository.deleteAll();
        conversationRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected record UserWithToken(MyUser user, String token){ }

    protected UserWithToken registerAndLogin(){
        return registerAndLogin("user" + System.currentTimeMillis(), "password");
    }

    protected UserWithToken registerAndLogin(String username){
        return registerAndLogin(username, "password");
    }

    protected UserWithToken registerAndLogin(String username, String password){
        RegisterRequest registerRequest = new RegisterRequest(username, password);
        http.post()
                .uri("/auth/register")
                .bodyValue(registerRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody().isEmpty();

        LoginRequest loginRequest = new LoginRequest(username, password);
        AuthResponse authResponse = http.post()
                .uri("/auth/login")
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        MyUser user = userRepository.findByUsername(username).orElseThrow();
        return new UserWithToken(user, authResponse.accessToken());
    }

    protected ResponseEntity<MessageResponse> send(String token, SendMessageRequest request) {
        return exchange(
                http.post()
                        .uri("/messages")
                        .header("Authorization", "Bearer " + token)
                        .bodyValue(request),
                MessageResponse.class
        );
    }

    protected <T> ResponseEntity<T> post(String token, String uri, Object body, ParameterizedTypeReference<T> responseType) {
        return exchange(
                http.post()
                        .uri(uri)
                        .header("Authorization", "Bearer " + token)
                        .bodyValue(body),
                responseType
        );
    }

    protected <T> ResponseEntity<T> post(String uri, Object body, ParameterizedTypeReference<T> responseType) {
        return exchange(
                http.post()
                        .uri(uri)
                        .bodyValue(body),
                responseType
        );
    }

    protected <T> ResponseEntity<T> get(String token, String uri, ParameterizedTypeReference<T> responseType) {
        return exchange(
                http.get()
                        .uri(uri)
                        .header("Authorization", "Bearer " + token),
                responseType
        );
    }

    protected <T> ResponseEntity<T> get(String token, Function<UriBuilder, URI> uriFunction, ParameterizedTypeReference<T> responseType) {
        return exchange(
                http.get()
                        .uri(uriFunction)
                        .header("Authorization", "Bearer " + token),
                responseType
        );
    }

    private <T> ResponseEntity<T> exchange(WebTestClient.RequestHeadersSpec<?> request, Class<T> bodyType) {
        EntityExchangeResult<T> result = request.exchange()
                .expectBody(bodyType)
                .returnResult();
        return new ResponseEntity<>(result.getResponseBody(), result.getResponseHeaders(), result.getStatus());
    }

    private <T> ResponseEntity<T> exchange(WebTestClient.RequestHeadersSpec<?> request, ParameterizedTypeReference<T> bodyType) {
        EntityExchangeResult<T> result = request.exchange()
                .expectBody(bodyType)
                .returnResult();
        return new ResponseEntity<>(result.getResponseBody(), result.getResponseHeaders(), result.getStatus());
    }

}
