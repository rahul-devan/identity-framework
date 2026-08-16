package com.ndash.identity_framework.exception;

import com.ndash.identity_framework.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateResource(DuplicateResourceException ex) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return errorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoSuchElement(NoSuchElementException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        String message = ex.getMessage() != null && !ex.getMessage().isBlank()
                ? ex.getMessage()
                : "Resource not found";
        return errorResponse(HttpStatus.NOT_FOUND, message);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse<Object>> handleResponseStatus(ResponseStatusException ex) {
        log.warn("Request rejected: {}", ex.getReason());
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return errorResponse(status, ex.getReason() != null ? ex.getReason() : status.getReasonPhrase());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        String message = resolveDataIntegrityMessage(ex);
        return errorResponse(HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex) {
        log.warn("API error: {}", ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining("; "));
        if (message.isBlank()) {
            message = "Validation failed";
        }
        log.warn("Validation error: {}", message);
        return errorResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnreadableMessage(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return errorResponse(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponse<Object>> handleTransactionSystem(TransactionSystemException ex) {
        return resolveAndRespond(ex);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        return resolveAndRespond(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        log.error("Unhandled exception", ex);
        return errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage() != null ? ex.getMessage() : "Internal server error"
        );
    }

    private ResponseEntity<ApiResponse<Object>> resolveAndRespond(Throwable ex) {
        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);

        if (root instanceof DuplicateResourceException duplicate) {
            return handleDuplicateResource(duplicate);
        }
        if (root instanceof ResourceNotFoundException notFound) {
            return handleNotFound(notFound);
        }
        if (root instanceof BadRequestException badRequest) {
            return handleBadRequest(badRequest);
        }
        if (root instanceof ApiException apiException) {
            return handleApiException(apiException);
        }
        if (root instanceof DataIntegrityViolationException dataIntegrity) {
            return handleDataIntegrity(dataIntegrity);
        }

        log.warn("Request failed: {}", root.getMessage(), root);
        return errorResponse(HttpStatus.BAD_REQUEST, root.getMessage());
    }

    private ResponseEntity<ApiResponse<Object>> errorResponse(HttpStatus status, String message) {
        String safeMessage = message != null && !message.isBlank() ? message : status.getReasonPhrase();
        return ResponseEntity
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(safeMessage, status.value()));
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException ex) {
        String rootMessage = NestedExceptionUtils.getMostSpecificCause(ex).getMessage();
        if (rootMessage == null) {
            return "A record with the same unique value already exists.";
        }

        String lower = rootMessage.toLowerCase();
        if (lower.contains("email")) {
            return "A user with this email address already exists.";
        }
        if (lower.contains("phone")) {
            return "A user with this phone number already exists.";
        }
        if (lower.contains("ssn")) {
            return "A user with this SSN already exists.";
        }

        return "A record with the same unique value already exists.";
    }
}
