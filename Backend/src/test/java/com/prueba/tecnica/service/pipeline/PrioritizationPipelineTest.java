package com.prueba.tecnica.service.pipeline;

import com.prueba.tecnica.common.pipeline.Pipeline;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.model.TipoSolicitud;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

@SpringBootTest
class PrioritizationPipelineTest {

    @Autowired
    private Pipeline<SolicitudPriorizada> prioritizationPipeline;

    @Test
    void testIncidentScore() {
        Solicitud solicitud = Solicitud.builder()
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(1)
                .fechaCreacion(LocalDateTime.now())
                .usuario("TestUser")
                .build();

        SolicitudPriorizada result = prioritizationPipeline.process(new SolicitudPriorizada(solicitud, 0));

        // El puntaje base para INCIDENTE es 100.
        // Prioridad manual 1 * 10 = 10.
        // Antigüedad 0 horas = 0.
        // Resultado esperado: 110.
        Assertions.assertEquals(110.0, result.getScore());
    }

    @Test
    void testAntiquityCap() {
        // Creando solicitud hace 200 horas para verificar el límite de antigüedad y el
        // incremento por la misma
        Solicitud solicitud = Solicitud.builder()
                .tipo(TipoSolicitud.CONSULTA) // 10 puntos
                .prioridadManual(1) // 10 puntos
                .fechaCreacion(LocalDateTime.now().minusHours(200))
                .usuario("OldUser")
                .build();

        SolicitudPriorizada result = prioritizationPipeline.process(new SolicitudPriorizada(solicitud, 0));

        // Base CONSULTA: 20
        // Manual: 1 * 10 = 10
        // Antigüedad: 200 horas * 1.0 = 200, PERO se limita a 100 dado el cap
        // establecido
        // Total esperado: 20 + 10 + 100 = 130
        Assertions.assertEquals(130.0, result.getScore());
    }

    @Test
    void testManualPriority() {
        Solicitud solicitud = Solicitud.builder()
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(5)
                .fechaCreacion(LocalDateTime.now())
                .usuario("TestUser")
                .build();

        SolicitudPriorizada result = prioritizationPipeline.process(new SolicitudPriorizada(solicitud, 0));

        // El puntaje base para INCIDENTE es 100.
        // Prioridad manual 5 * 10 = 50.
        // Antigüedad 0 horas = 0.
        // Resultado esperado: 150.
        Assertions.assertEquals(150.0, result.getScore());
    }
}
