package com.messaging.service.api;

import com.messaging.service.api.exception.RecipientNotFoundException;
import com.messaging.service.api.exception.SelfMessageException;
import com.messaging.service.api.exception.ServiceErrorResponse;
import com.messaging.service.api.exception.UsernameTakenException;
import com.messaging.service.security.ConversationAccessException;
import com.messaging.service.support.InvalidCursorException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Validation failures
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ServiceErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        List<String> errors = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.badRequest().body(new ServiceErrorResponse("VALIDATION_ERROR",String.join(";",errors)));
    }

    // Each custom exception maps to an HTTP status
    @ExceptionHandler(UsernameTakenException.class)
    ResponseEntity<ServiceErrorResponse> handleUserNameTaken(UsernameTakenException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ServiceErrorResponse("USERNAME_TAKEN", exception.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ServiceErrorResponse> handleBadCredentials(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ServiceErrorResponse("BAD_CREDENTIALS", exception.getMessage()));
    }

    @ExceptionHandler(RecipientNotFoundException.class)
    ResponseEntity<ServiceErrorResponse> handleRecipientNotFound(RecipientNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ServiceErrorResponse("RECIPIENT_NOT_FOUND", exception.getMessage()));
    }
    @ExceptionHandler(SelfMessageException.class)
    ResponseEntity<ServiceErrorResponse> handleSelfMessage(SelfMessageException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ServiceErrorResponse("SELF_MESSAGE", exception.getMessage()));
    }
    @ExceptionHandler(ConversationAccessException.class)
    ResponseEntity<ServiceErrorResponse> handleConversationAccess(ConversationAccessException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ServiceErrorResponse("CONVERSATION_ACCESS", exception.getMessage()));
    }
    @ExceptionHandler(InvalidCursorException.class)
    ResponseEntity<ServiceErrorResponse> handleInvalidCursor(InvalidCursorException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ServiceErrorResponse("INVALID_CURSOR", exception.getMessage()));
    }

    // Safety net
    @ExceptionHandler(Exception.class)
    ResponseEntity<ServiceErrorResponse> handleException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ServiceErrorResponse("INTERNAL_SERVER_ERROR", exception.getMessage()));
    }
}
