package com.prueba.tecnica.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SolicitudTest {
    @Test
    void debeAsignarFechaCreacionCuandoEsNula() {
        // Cubre el if (fechaCreacion == null)
        Solicitud solicitud = new Solicitud();
        solicitud.prePersist();

        assertNotNull(solicitud.getFechaCreacion());
    }

    @Test
    void debeManteberFechaCreacionCuandoYaExiste() {
        // Cubre el else implícito — fecha no se sobreescribe
        LocalDateTime fechaOriginal = LocalDateTime.of(2024, 1, 15, 10, 0);
        Solicitud solicitud = new Solicitud();
        solicitud.setFechaCreacion(fechaOriginal);

        solicitud.prePersist();

        assertEquals(fechaOriginal, solicitud.getFechaCreacion());
    }

}
