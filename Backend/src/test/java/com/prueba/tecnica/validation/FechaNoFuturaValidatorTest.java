package com.prueba.tecnica.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class FechaNoFuturaValidatorTest {
    private final FechaNoFuturaValidator validator = new FechaNoFuturaValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @Test
    void debeRetornarTrueCuandoFechaEsNula() {
        // Cubre: if (fecha == null) → true
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void debeRetornarTrueCuandoFechaEsAnteriorAHoy() {
        // Cubre: !fecha.isAfter(now) → true
        LocalDateTime fechaPasada = LocalDateTime.now().minusDays(1);
        assertTrue(validator.isValid(fechaPasada, context));
    }

    @Test
    void debeRetornarFalseCuandoFechaEsFutura() {
        // Cubre: !fecha.isAfter(now) → false
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(1);
        assertFalse(validator.isValid(fechaFutura, context));
    }

}
