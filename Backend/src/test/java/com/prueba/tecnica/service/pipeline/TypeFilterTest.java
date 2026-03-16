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
        ReflectionTestUtils.setField(typeFilter, "incidentScore",   100.0);
        ReflectionTestUtils.setField(typeFilter, "requirementScore", 60.0);
        ReflectionTestUtils.setField(typeFilter, "queryScore",       20.0);
    }


    // Un INCIDENTE debe recibir el puntaje más alto (100 puntos), reflejando
    // su prioridad máxima dentro del sistema de clasificación.

    @Test
    void testIncidente_AgregaPuntajeMaximoDeTipo() {
        SolicitudPriorizada entrada = solicitudCon(TipoSolicitud.INCIDENTE, 0.0);

        SolicitudPriorizada resultado = typeFilter.execute(entrada);

        assertEquals(100.0, resultado.getScore());
    }

    
    // Un REQUERIMIENTO debe recibir un puntaje intermedio (60 puntos),
    // por debajo de INCIDENTE pero por encima de CONSULTA.
    
    @Test
    void testRequerimiento_AgregaPuntajeIntermedio() {
        SolicitudPriorizada entrada = solicitudCon(TipoSolicitud.REQUERIMIENTO, 0.0);

        SolicitudPriorizada resultado = typeFilter.execute(entrada);

        assertEquals(60.0, resultado.getScore());
    }

    
    // Una CONSULTA recibe el puntaje más bajo segun las reglas de negocio (20 puntos).
    
    @Test
    void testConsulta_AgregaPuntajeMinimoDeTipo() {
        SolicitudPriorizada entrada = solicitudCon(TipoSolicitud.CONSULTA, 0.0);

        SolicitudPriorizada resultado = typeFilter.execute(entrada);

        assertEquals(20.0, resultado.getScore());
    }

    
    // El filtro debe acumular sobre el score existente, no sobreescribirlo.
    // Un INCIDENTE con score previo de 50 debe resultar en 150.
    
    @Test
    void testFiltroAcumula_SobreScorePrevio() {
        SolicitudPriorizada entrada = solicitudCon(TipoSolicitud.INCIDENTE, 50.0);

        SolicitudPriorizada resultado = typeFilter.execute(entrada);

        assertEquals(150.0, resultado.getScore(),
                "El filtro debe sumar los puntos de tipo al score ya acumulado por filtros anteriores");
    }

    // Si el tipo de solicitud es null, ninguna rama coincide: el filtro no debe
    // sumar ni restar puntos, dejando el score completamente intacto.

    @Test
    void testTipoNulo_ScoreNoModificado() {
        Solicitud solicitud = Solicitud.builder()
                .usuario("Test")
                .prioridadManual(1)
                .build(); // tipo = null
        SolicitudPriorizada entrada = new SolicitudPriorizada(solicitud, 35.0);

        SolicitudPriorizada resultado = typeFilter.execute(entrada);

        assertEquals(35.0, resultado.getScore(),
                "Un tipo null no corresponde a ningún caso conocido: el score no debe cambiar");
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
