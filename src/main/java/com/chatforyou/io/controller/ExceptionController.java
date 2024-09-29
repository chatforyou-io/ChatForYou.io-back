package com.chatforyou.io.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "can not find info");
        response.put("message", ex.getMessage());
        log.error("UnauthorizedException: {} :: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestException(BadRequestException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Bad Request");
        response.put("message", ex.getMessage());
        log.error("UnauthorizedException: {} :: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Internal Server Error");
        response.put("message", ex.getMessage());
        log.error("RuntimeException: {} :: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpClientErrorException.Unauthorized.class)
    public ResponseEntity<Map<String, String>> handleAuthException(HttpClientErrorException.Unauthorized ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Unauthorized");
        response.put("message", ex.getMessage());
        log.error("Unauthorized: {} :: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Unauthorized");
        response.put("message", ex.getMessage());
        log.error("IllegalArgumentException: {} :: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}