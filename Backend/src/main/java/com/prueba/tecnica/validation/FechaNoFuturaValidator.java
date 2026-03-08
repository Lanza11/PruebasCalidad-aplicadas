package com.prueba.tecnica.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class FechaNoFuturaValidator implements ConstraintValidator<FechaNoFutura, LocalDateTime> {

    @Override
    public boolean isValid(LocalDateTime fecha, ConstraintValidatorContext context) {
        // Null values are considered valid (use @NotNull for null checks)
        if (fecha == null) {
            return true;
        }

        return !fecha.isAfter(LocalDateTime.now());
    }
}
