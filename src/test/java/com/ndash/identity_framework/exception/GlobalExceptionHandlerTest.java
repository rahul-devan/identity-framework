package com.ndash.identity_framework.exception;

import com.ndash.identity_framework.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleValidation_returnsBadRequestWithFieldMessages() {
        // given
        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "loginRequest");
        bindingResult.addError(new FieldError("loginRequest", "username", "must not be blank"));
        final MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // when
        final ResponseEntity<ApiResponse<Object>> response = handler.handleValidation(ex);

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getError().contains("username"));
    }

    @Test
    void handleDuplicateResource_returnsConflict() {
        // given
        final DuplicateResourceException ex = new DuplicateResourceException("duplicate email");

        // when
        final ResponseEntity<ApiResponse<Object>> response = handler.handleDuplicateResource(ex);

        // then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("duplicate email", response.getBody().getError());
    }
}
