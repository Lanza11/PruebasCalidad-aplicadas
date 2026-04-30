package com.prueba.tecnica.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ErrorResponseTest {
    @Test
    void debeConstruirErrorResponseConConstructorParcial() {
        // Cubre el constructor de 4 parámetros
        ErrorResponse response = new ErrorResponse(
                400, "Bad Request", "Mensaje de error", "/api/test");

        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("Mensaje de error", response.getMessage());
        assertEquals("/api/test", response.getPath());
        assertNotNull(response.getTimestamp()); // se asigna automáticamente
        assertNull(response.getValidationErrors()); // no se pasa en este constructor
    }

    @Test
    void debeConstruirErrorResponseConBuilder() {
        // Cubre el @Builder
        ErrorResponse response = ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Recurso no encontrado")
                .path("/api/solicitudes/99")
                .build();

        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
    }

}
