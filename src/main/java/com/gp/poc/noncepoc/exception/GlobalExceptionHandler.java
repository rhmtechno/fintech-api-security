package com.gp.poc.noncepoc.exception;

import com.gp.poc.noncepoc.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ------------------------------------
    // Security / Nonce / Signature errors
    // ------------------------------------
    @ExceptionHandler(SecurityViolationException.class)
    public ResponseEntity<ErrorResponse> handleSecurityViolation(
            SecurityViolationException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(
                        ErrorResponse.builder()
                                .code(ex.getCode())
                                .message(ex.getMessage())
                                .timestamp(Instant.now().toString())
                                .path(request.getRequestURI())
                                .build()
                );
    }

    // ------------------------------------
    // Fallback (unexpected errors)
    // ------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        return ResponseEntity
                .status(500)
                .body(
                        ErrorResponse.builder()
                                .code("INTERNAL_SERVER_ERROR")
                                .message("Something went wrong")
                                .timestamp(Instant.now().toString())
                                .path(request.getRequestURI())
                                .build()
                );
    }
}