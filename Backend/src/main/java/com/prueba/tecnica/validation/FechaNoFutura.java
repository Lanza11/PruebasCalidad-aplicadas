package com.prueba.tecnica.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FechaNoFuturaValidator.class)
@Documented
public @interface FechaNoFutura {
    String message() default "La fecha no puede ser en el futuro";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
