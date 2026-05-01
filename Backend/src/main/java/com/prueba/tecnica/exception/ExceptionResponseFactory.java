package com.prueba.tecnica.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public final class ExceptionResponseFactory {

    private ExceptionResponseFactory() {
        // utility
    }

    public static String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    public static String resolveJsonErrorMessage(HttpMessageNotReadableException ex) {
        if (ex.getMessage() != null && ex.getMessage().contains("TipoSolicitud")) {
            return "Tipo de solicitud inválido. Valores permitidos: INCIDENTE, REQUERIMIENTO, CONSULTA";
        }
        return "Error al procesar el JSON. Verifica que el formato sea correcto.";
    }

    public static Map<String, String> buildValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            errors.put(fieldName, error.getDefaultMessage());
        });
        return errors;
    }

    public static Map<String, String> buildConstraintErrors(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations()
                .forEach(violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return errors;
    }

    public static ErrorResponse buildValidationResponse(MethodArgumentNotValidException ex, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Error")
                .message("Los datos enviados no son válidos")
                .path(getPath(request))
                .validationErrors(buildValidationErrors(ex))
                .build();
    }

    public static ErrorResponse buildConstraintResponse(ConstraintViolationException ex, WebRequest request) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Constraint Violation")
                .message("Violación de restricciones de validación")
                .path(getPath(request))
                .validationErrors(buildConstraintErrors(ex))
                .build();
    }

    public static ErrorResponse buildJsonResponse(HttpMessageNotReadableException ex, WebRequest request) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                resolveJsonErrorMessage(ex),
                getPath(request));
    }

    public static ErrorResponse buildGlobalResponse(WebRequest request) {
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ha ocurrido un error interno. Por favor, intenta nuevamente.",
                getPath(request));
    }
}
