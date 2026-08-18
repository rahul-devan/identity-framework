package com.ndash.identity_framework.exception;

import com.ndash.identity_framework.dto.ApiResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ApiResponse<Object>> handleError(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusCode != null ? Integer.parseInt(statusCode.toString()) : 500;

        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        String message = resolveMessage(exception, request, status);

        HttpStatus httpStatus = HttpStatus.resolve(status);
        if (httpStatus == null) {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return ResponseEntity
                .status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ApiResponse.error(message, httpStatus.value()));
    }

    private String resolveMessage(Throwable exception, HttpServletRequest request, int status) {
        if (exception != null) {
            Throwable root = NestedExceptionUtils.getMostSpecificCause(exception);
            if (root.getMessage() != null && !root.getMessage().isBlank()) {
                return root.getMessage();
            }
        }

        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (errorMessage != null && !errorMessage.toString().isBlank()) {
            return errorMessage.toString();
        }

        HttpStatus httpStatus = HttpStatus.resolve(status);
        return httpStatus != null ? httpStatus.getReasonPhrase() : "Internal server error";
    }
}
