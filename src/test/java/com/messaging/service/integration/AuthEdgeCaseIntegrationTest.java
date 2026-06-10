package com.messaging.service.integration;

import com.messaging.service.api.dto.AuthResponse;
import com.messaging.service.api.dto.LoginRequest;
import com.messaging.service.api.dto.RegisterRequest;
import com.messaging.service.api.exception.ServiceErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthEdgeCaseIntegrationTest")
public class AuthEdgeCaseIntegrationTest extends BaseIntegrationTest {

    @Test
    void registerRejectsInvalidPayloads() {
        ResponseEntity<ServiceErrorResponse> response = post(
                "/auth/register",
                new RegisterRequest("ab", "short"),
                new ParameterizedTypeReference<ServiceErrorResponse>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).contains("size must be between");
    }

    @Test
    void loginRejectsBadCredentials() {
        registerAndLogin("auth_user", "password123");

        ResponseEntity<ServiceErrorResponse> response = post(
                "/auth/login",
                new LoginRequest("auth_user", "wrong-password"),
                new ParameterizedTypeReference<ServiceErrorResponse>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("BAD_CREDENTIALS");
    }
}
