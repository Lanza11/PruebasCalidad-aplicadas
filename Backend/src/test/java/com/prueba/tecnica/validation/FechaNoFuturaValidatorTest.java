package com.prueba.tecnica.validation;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FechaNoFuturaValidatorTest {

    private final FechaNoFuturaValidator validator = new FechaNoFuturaValidator();

    @Test
    void isValid_CuandoFechaEsNull_RetornaTrue() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void isValid_CuandoFechaEsPasada_RetornaTrue() {
        LocalDateTime fechaPasada = LocalDateTime.now().minusMinutes(1);

        assertTrue(validator.isValid(fechaPasada, null));
    }

    @Test
    void isValid_CuandoFechaEsFutura_RetornaFalse() {
        LocalDateTime fechaFutura = LocalDateTime.now().plusMinutes(1);

        assertFalse(validator.isValid(fechaFutura, null));
    }
}
