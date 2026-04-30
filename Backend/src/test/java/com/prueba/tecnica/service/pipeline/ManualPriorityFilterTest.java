package com.prueba.tecnica.service.pipeline;

import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.model.TipoSolicitud;
import com.prueba.tecnica.service.pipeline.rules.ManualPriorityFilter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class ManualPriorityFilterTest {
    private ManualPriorityFilter manualPriorityFilter;

    @BeforeEach
    void setUp() {
        manualPriorityFilter = new ManualPriorityFilter();
        ReflectionTestUtils.setField(manualPriorityFilter, "manualWeight", 10.0);
    }

    @Test
    void testPrioridad3_AgregaTreintaPuntos() {
        SolicitudPriorizada entrada = solicitudConPrioridad(3, 0.0);
        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);
        assertEquals(30.0, resultado.getScore());
    }

    @Test
    void testPrioridad5_AgregaCincuentaPuntosAlScorePrevio() {
        SolicitudPriorizada entrada = solicitudConPrioridad(5, 100.0);
        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);
        assertEquals(150.0, resultado.getScore(),
                "Prioridad 5 * peso(10) = 50 puntos deben sumarse al score previo de 100");
    }

    @Test
    void testPrioridad1_AgregaDiezPuntos() {
        SolicitudPriorizada entrada = solicitudConPrioridad(1, 0.0);
        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);
        assertEquals(10.0, resultado.getScore());
    }

    @Test
    void testPrioridadNula_ScoreNoModificado() {
        Solicitud solicitud = Solicitud.builder()
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(null)
                .usuario("Test")
                .build();
        SolicitudPriorizada entrada = new SolicitudPriorizada(solicitud, 100.0);
        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);
        assertEquals(100.0, resultado.getScore(),
                "Una prioridad null no debe modificar el score ni lanzar excepción");
    }

    @Test
    void testPesoPersonalizado_CalculaConElPesoCorrecto() {
        ReflectionTestUtils.setField(manualPriorityFilter, "manualWeight", 5.0);
        SolicitudPriorizada entrada = solicitudConPrioridad(4, 0.0);
        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);
        assertEquals(20.0, resultado.getScore(),
                "Prioridad 4 * peso 5 debe dar 20 puntos");
    }

    @Test
    void testPesoExcesivo_CalculaSinRestriccionesInternas() {
        ReflectionTestUtils.setField(manualPriorityFilter, "manualWeight", 20.0);
        SolicitudPriorizada entrada = solicitudConPrioridad(5, 0.0);
        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);
        assertEquals(100.0, resultado.getScore(),
                "Prioridad 5 * peso 20 debe dar 100 puntos");
    }

    // Probamos con una prioridad manual superior al rango esperado (por ejemplo, 10), deberia ajustarse y 
    // calcular es con valores dentro del rango, no aplicar tal cual la prioridad manual de 10 * 10 para un score 
    // de 1000, sino que se ajustaria a prioridad 5 * peso 10 para un score de 50
    // Bug solucionado
    
    @Test
    void debeRetornarInputCuandoPrioridadEsNula() {
        ManualPriorityFilter filter = new ManualPriorityFilter();

        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);
        assertEquals(50.0, resultado.getScore(),
                "Prioridad 10 ajustada a 5 * peso 10 debe dar 50 puntos");
    }

        Solicitud solicitud = new Solicitud();
        solicitud.setPrioridadManual(10);
        SolicitudPriorizada input = new SolicitudPriorizada(solicitud, 0);

        assertEquals(100.0, filter.execute(input).getScore());
    }

    @Test
    void debeCalcularConPrioridadNegativa() {
        // El código original NO tiene clampeo: -3 * 10.0 = -30.0
        ManualPriorityFilter filter = new ManualPriorityFilter();
        ReflectionTestUtils.setField(filter, "manualWeight", 10.0);

        Solicitud solicitud = new Solicitud();
        solicitud.setPrioridadManual(-3);
        SolicitudPriorizada input = new SolicitudPriorizada(solicitud, 0);

        assertEquals(-30.0, filter.execute(input).getScore());
    }

    private SolicitudPriorizada solicitudConPrioridad(Integer prioridad, double scoreInicial) {
        Solicitud solicitud = Solicitud.builder()
                .tipo(TipoSolicitud.CONSULTA)
                .prioridadManual(prioridad)
                .usuario("Test")
                .build();
        return new SolicitudPriorizada(solicitud, scoreInicial);
    }

}
