# Messaging Service

A 1-1 messaging REST API built with Spring Boot, H2, Spring Security, and JWT authentication.

## Overview

This service lets users:

- Register and log in
- Send direct messages to another user
- Read conversation history with cursor-based pagination
- Inspect API docs through Swagger UI

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Web MVC
- Spring Security
- Spring Data JPA
- H2 in-memory database
- JWT authentication

## Getting Started

### Run the app

```bash
./gradlew bootRun
```

The app starts on `http://localhost:8080` by default.

### Useful local URLs

- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`
- H2 console: `http://localhost:8080/h2-console`

## Project Docs

- [Help Guide](HELP.md)
- [Decision Log](docs/DECISIONS.md)
- [Integration Test Report](reports/integration-test-success-report.md)

## API Endpoints

### Auth

- `POST /auth/register`
- `POST /auth/login`

### Messages

- `POST /messages`

### Conversations

- `GET /conversations/{id}/messages?limit=30&cursor=...`

### Health

- `GET /actuator/health`

## Request Flow

1. Register a user with `POST /auth/register`
2. Log in with `POST /auth/login`
3. Copy the returned JWT access token
4. Send authenticated requests with:

```http
Authorization: Bearer <token>
```

## Running Tests

```bash
./gradlew test
```

Integration tests use `WebTestClient` against the live random-port server.

### Latest successful test run

- Command: `./gradlew test --tests 'com.messaging.service.integration.*'`
- Result: `BUILD SUCCESSFUL`
- Coverage: integration tests under `src/test/java/com/messaging/service/integration`

### Test report

- Report file: [reports/integration-test-success-report.md](reports/integration-test-success-report.md)

## Architecture

- **Domain Layer:** JPA entities and domain models for users, conversations, and messages
- **Repository Layer:** Spring Data repositories for persistence access
- **Security Layer:** JWT authentication, authorization, and conversation access checks
- **Service Layer:** Business rules for messaging, authentication, and conversation history
- **API Layer:** REST controllers and request/response DTOs
- **Error Handling:** Centralized exception mapping through `GlobalExceptionHandler`

## Key Design Decisions

See [Decision Log](docs/DECISIONS.md) for the rationale behind the main implementation choices.
