package com.chatforyou.io.controller;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MissingRequestHeaderException;
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
        response.put("code", "400");
        response.put("message", ex.getMessage());
        log.error("UnauthorizedException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequestException(BadRequestException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("code", "400");
        response.put("message", ex.getMessage());
        log.error("UnauthorizedException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("code", "500");
        response.put("message", "Unknown Server Error");
        log.error("RuntimeException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static class UnauthorizedException extends AuthenticationException {
        public UnauthorizedException(String message) {
            super(message);
        }

        public UnauthorizedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(UnauthorizedException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "401");
        response.put("message", ex.getMessage());
        log.error("Unauthorized: {}", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("code", "500");
        response.put("message", "Unknown Server Error");
        log.error("IllegalArgumentException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static class JwtSignatureException extends BadRequestException {
        public JwtSignatureException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @ExceptionHandler(JwtSignatureException.class)
    public ResponseEntity<Map<String, String>> handleSignatureException(JwtSignatureException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("code", "401");
        response.put("message", ex.getMessage());
        log.error("SignatureException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }


    public static class JwtExpiredException extends BadRequestException {
        public JwtExpiredException(String message, Throwable cause) {
            super(message, cause);
        }
        public JwtExpiredException(Throwable cause) {
            super(cause);
        }
    }

    @ExceptionHandler(JwtExpiredException.class)
    public ResponseEntity<Map<String, String>> handleExpiredJwtException(JwtExpiredException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("code", "401");
        response.put("message", "The provided token has expired.");
        log.error("ExpiredJwtException: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    public static class JwtBearerException extends BadRequestException {
        public JwtBearerException(String message, Throwable cause) {
            super(message, cause);
        }
        public JwtBearerException() {
            super();
        }
    }

    @ExceptionHandler(JwtBearerException.class)
    public ResponseEntity<Map<String, String>> handleJwtBearerException(JwtBearerException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("code", "401");
        response.put("message", "Bearer token is required.");
        log.error("ExpiredJwtException :: Bearer Token Error");
        log.error("ExpiredJwtException : {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, String>> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("code", "401");
        response.put("message", "Bearer token is required.");
        log.error("Required Request header 'Authorization' :: Bearer Token Error");
        log.error("Authorization Error : {}", ex.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

}