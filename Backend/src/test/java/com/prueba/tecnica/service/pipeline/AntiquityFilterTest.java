package com.prueba.tecnica.service.pipeline;

import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.model.TipoSolicitud;
import com.prueba.tecnica.service.pipeline.rules.AntiquityFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AntiquityFilterTest {
    private AntiquityFilter antiquityFilter;

    @BeforeEach
    void setUp() {
        antiquityFilter = new AntiquityFilter();
        ReflectionTestUtils.setField(antiquityFilter, "antiquityWeight", 1.0);
        ReflectionTestUtils.setField(antiquityFilter, "antiquityCap", 100.0);
    }

    @Test
    void testSolicitudDe10Horas_AgregaDiezPuntosDeAntiguedad() {
        SolicitudPriorizada entrada = solicitudConFecha(LocalDateTime.now().minusHours(10), 0.0);
        SolicitudPriorizada resultado = antiquityFilter.execute(entrada);
        assertEquals(10.0, resultado.getScore());
    }

    @Test
    void testSolicitudDe48Horas_AcumulaCorrectamenteSobreScorePrevio() {
        SolicitudPriorizada entrada = solicitudConFecha(LocalDateTime.now().minusHours(48), 60.0);
        SolicitudPriorizada resultado = antiquityFilter.execute(entrada);
        assertEquals(108.0, resultado.getScore(),
                "60 (puntos previos) + 48 (antigüedad) = 108 puntos");
    }

    @Test
    void testSolicitudDe200Horas_ScoreLimitadoPorCap() {
        SolicitudPriorizada entrada = solicitudConFecha(LocalDateTime.now().minusHours(200), 0.0);
        SolicitudPriorizada resultado = antiquityFilter.execute(entrada);
        assertEquals(100.0, resultado.getScore(),
                "200 horas * 1 pt/h = 200 pts, pero el cap es 100");
    }

    @Test
    void testFechaCreacionNula_ScoreNoModificado() {
        Solicitud solicitud = Solicitud.builder()
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(3)
                .fechaCreacion(null)
                .usuario("Test")
                .build();
        SolicitudPriorizada entrada = new SolicitudPriorizada(solicitud, 50.0);
        SolicitudPriorizada resultado = antiquityFilter.execute(entrada);
        assertEquals(50.0, resultado.getScore(),
                "fechaCreacion null no debe modificar el score ni lanzar excepción");
    }

    @Test
    void testFechaEnFuturo_ScoreNoModificado() {
        SolicitudPriorizada entrada = solicitudConFecha(LocalDateTime.now().plusHours(5), 50.0);
        SolicitudPriorizada resultado = antiquityFilter.execute(entrada);
        assertEquals(50.0, resultado.getScore(),
                "Una fecha futura produce antigüedad negativa: el score no debe cambiar");
    }

    @Test
    void testSolicitudDeMenosDeUnaHora_ScoreNoModificado() {
        SolicitudPriorizada entrada = solicitudConFecha(LocalDateTime.now().minusMinutes(45), 20.0);
        SolicitudPriorizada resultado = antiquityFilter.execute(entrada);
        assertEquals(20.0, resultado.getScore(),
                "Menos de 1 hora completa = 0 horas enteras: el filtro no debe sumar puntos");
    }

    @Test
    void debeRetornarInputCuandoFechaCreacionEsNula() {
        AntiquityFilter filter = new AntiquityFilter();

        Solicitud solicitud = new Solicitud();
        solicitud.setFechaCreacion(null);
        SolicitudPriorizada input = new SolicitudPriorizada(solicitud, 0);

        assertEquals(0, filter.execute(input).getScore());
    }

    @Test
    void debeRetornarInputCuandoHorasSonCero() {
        AntiquityFilter filter = new AntiquityFilter();

        Solicitud solicitud = new Solicitud();
        solicitud.setFechaCreacion(LocalDateTime.now());
        SolicitudPriorizada input = new SolicitudPriorizada(solicitud, 0);

        assertEquals(0, filter.execute(input).getScore());
    }

    @Test
    void debeAplicarCapCuandoPuntosExcedenElMaximo() {
        AntiquityFilter filter = new AntiquityFilter();
        ReflectionTestUtils.setField(filter, "antiquityWeight", 1.0);
        ReflectionTestUtils.setField(filter, "antiquityCap", 100.0);

        Solicitud solicitud = new Solicitud();
        solicitud.setFechaCreacion(LocalDateTime.now().minusHours(200));
        SolicitudPriorizada input = new SolicitudPriorizada(solicitud, 0);

        assertEquals(100.0, filter.execute(input).getScore());
    }

    private SolicitudPriorizada solicitudConFecha(LocalDateTime fecha, double scoreInicial) {
        Solicitud solicitud = Solicitud.builder()
                .tipo(TipoSolicitud.CONSULTA)
                .prioridadManual(1)
                .fechaCreacion(fecha)
                .usuario("Test")
                .build();
        return new SolicitudPriorizada(solicitud, scoreInicial);
    }

}
