package com.prueba.tecnica.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private String getPath(WebRequest request) {
        return ExceptionResponseFactory.getPath(request);
    }

    private String resolveJsonErrorMessage(HttpMessageNotReadableException ex) {
        return ExceptionResponseFactory.resolveJsonErrorMessage(ex);
    }

    private Map<String, String> buildValidationErrors(MethodArgumentNotValidException ex) {
        return ExceptionResponseFactory.buildValidationErrors(ex);
    }

    private Map<String, String> buildConstraintErrors(ConstraintViolationException ex) {
        return ExceptionResponseFactory.buildConstraintErrors(ex);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        return new ResponseEntity<>(ExceptionResponseFactory.buildValidationResponse(ex, request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        return new ResponseEntity<>(ExceptionResponseFactory.buildConstraintResponse(ex, request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        return new ResponseEntity<>(ExceptionResponseFactory.buildJsonResponse(ex, request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        return new ResponseEntity<>(ExceptionResponseFactory.buildGlobalResponse(request), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
