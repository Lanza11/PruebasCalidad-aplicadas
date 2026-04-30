package com.prueba.tecnica.service.pipeline;

import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.model.TipoSolicitud;
import com.prueba.tecnica.service.pipeline.rules.TypeFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class TypeFilterTest {
    private TypeFilter typeFilter;

    @BeforeEach
    void setUp() {
        typeFilter = new TypeFilter();
        ReflectionTestUtils.setField(typeFilter, "incidentScore", 100.0);
        ReflectionTestUtils.setField(typeFilter, "requirementScore", 60.0);
        ReflectionTestUtils.setField(typeFilter, "queryScore", 20.0);
    }

    @Test
    void testIncidente_AgregaPuntajeMaximoDeTipo() {
        SolicitudPriorizada entrada = solicitudCon(TipoSolicitud.INCIDENTE, 0.0);
        SolicitudPriorizada resultado = typeFilter.execute(entrada);
        assertEquals(100.0, resultado.getScore());
    }

    @Test
    void testRequerimiento_AgregaPuntajeIntermedio() {
        SolicitudPriorizada entrada = solicitudCon(TipoSolicitud.REQUERIMIENTO, 0.0);
        SolicitudPriorizada resultado = typeFilter.execute(entrada);
        assertEquals(60.0, resultado.getScore());
    }

    @Test
    void testConsulta_AgregaPuntajeMinimoDeTipo() {
        SolicitudPriorizada entrada = solicitudCon(TipoSolicitud.CONSULTA, 0.0);
        SolicitudPriorizada resultado = typeFilter.execute(entrada);
        assertEquals(20.0, resultado.getScore());
    }

    @Test
    void testFiltroAcumula_SobreScorePrevio() {
        SolicitudPriorizada entrada = solicitudCon(TipoSolicitud.INCIDENTE, 50.0);
        SolicitudPriorizada resultado = typeFilter.execute(entrada);
        assertEquals(150.0, resultado.getScore(),
                "El filtro debe sumar los puntos de tipo al score ya acumulado por filtros anteriores");
    }

    @Test
    void testTipoNulo_ScoreNoModificado() {
        Solicitud solicitud = Solicitud.builder()
                .usuario("Test")
                .prioridadManual(1)
                .build();
        SolicitudPriorizada entrada = new SolicitudPriorizada(solicitud, 35.0);
        SolicitudPriorizada resultado = typeFilter.execute(entrada);
        assertEquals(35.0, resultado.getScore(),
                "Un tipo null no corresponde a ningún caso conocido: el score no debe cambiar");
    }

    @Test
    void debeAsignarPuntajePorIncidente() {
        TypeFilter filter = new TypeFilter();
        ReflectionTestUtils.setField(filter, "incidentScore", 100.0);
        ReflectionTestUtils.setField(filter, "requirementScore", 60.0);
        ReflectionTestUtils.setField(filter, "queryScore", 20.0);

        Solicitud solicitud = new Solicitud();
        solicitud.setTipo(TipoSolicitud.INCIDENTE);
        SolicitudPriorizada input = new SolicitudPriorizada(solicitud, 0);

        assertEquals(100.0, filter.execute(input).getScore());
    }

    @Test
    void debeAsignarPuntajePorRequerimiento() {
        TypeFilter filter = new TypeFilter();
        ReflectionTestUtils.setField(filter, "incidentScore", 100.0);
        ReflectionTestUtils.setField(filter, "requirementScore", 60.0);
        ReflectionTestUtils.setField(filter, "queryScore", 20.0);

        Solicitud solicitud = new Solicitud();
        solicitud.setTipo(TipoSolicitud.REQUERIMIENTO);
        SolicitudPriorizada input = new SolicitudPriorizada(solicitud, 0);

        assertEquals(60.0, filter.execute(input).getScore());
    }

    @Test
    void debeAsignarPuntajePorConsulta() {
        TypeFilter filter = new TypeFilter();
        ReflectionTestUtils.setField(filter, "incidentScore", 100.0);
        ReflectionTestUtils.setField(filter, "requirementScore", 60.0);
        ReflectionTestUtils.setField(filter, "queryScore", 20.0);

        Solicitud solicitud = new Solicitud();
        solicitud.setTipo(TipoSolicitud.CONSULTA);
        SolicitudPriorizada input = new SolicitudPriorizada(solicitud, 0);

        assertEquals(20.0, filter.execute(input).getScore());
    }

    private SolicitudPriorizada solicitudCon(TipoSolicitud tipo, double scoreInicial) {
        Solicitud solicitud = Solicitud.builder()
                .tipo(tipo)
                .usuario("Test")
                .prioridadManual(1)
                .build();
        return new SolicitudPriorizada(solicitud, scoreInicial);
    }

}
