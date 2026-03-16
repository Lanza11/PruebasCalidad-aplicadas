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



    // Prioridad manual 3 con peso 10 debe aportar exactamente 30 puntos.

    @Test
    void testPrioridad3_AgregaTreintaPuntos() {
        SolicitudPriorizada entrada = solicitudConPrioridad(3, 0.0);

        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);

        assertEquals(30.0, resultado.getScore());
    }

    // Prioridad manual máxima (5) con peso 10 debe aportar 50 puntos. Se verifica además que se acumule 
    // correctamente sobre un score previo (en este caso 100.0), confirmando la suma correcta de puntos de 
    // prioridad manual al score existente.

    @Test
    void testPrioridad5_AgregaCincuentaPuntosAlScorePrevio() {
        SolicitudPriorizada entrada = solicitudConPrioridad(5, 100.0); // score previo de tipo INCIDENTE

        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);

        assertEquals(150.0, resultado.getScore(),
                "Prioridad 5 * peso(10) = 50 puntos deben sumarse al score previo de 100");
    }

    
     // Prioridad mínima (1) debe aportar únicamente 10 puntos, confirmando el rango
     // completo del filtro (1–5).

    @Test
    void testPrioridad1_AgregaDiezPuntos() {
        SolicitudPriorizada entrada = solicitudConPrioridad(1, 0.0);

        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);

        assertEquals(10.0, resultado.getScore());
    }


    // Si prioridadManual es null el filtro debe dejarlo pasar sin modificar el
    // score, evitando un NullPointerException y preservando los puntos ya calculados.

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

     // Con un peso personalizado diferente del valor por defecto se verifica que el
     // filtro respeta el parámetro de configuración.  Peso 5 + prioridad 4 = 20 pts.

    @Test
    void testPesoPersonalizado_CalculaConElPesoCorrecto() {
        ReflectionTestUtils.setField(manualPriorityFilter, "manualWeight", 5.0);
        SolicitudPriorizada entrada = solicitudConPrioridad(4, 0.0);

        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);

        assertEquals(20.0, resultado.getScore(),
                "Prioridad 4 * peso 5 debe dar 20 puntos independientemente del peso por defecto");
    }

    // Probando con un peso superior al rango esperado (por ejemplo, 20) se verifica que el filtro sigue 
    // aplicando la fórmula correctamente, resultando en un aporte de 100 puntos para prioridad 5, 
    // confirmando que el filtro no tiene restricciones internas sobre el valor del peso y calcula según lo configurado.

    @Test
    void testPesoExcesivo_CalculaSinRestriccionesInternas() {
        ReflectionTestUtils.setField(manualPriorityFilter, "manualWeight", 20.0);
        SolicitudPriorizada entrada = solicitudConPrioridad(5, 0.0);

        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);
        assertEquals(100.0, resultado.getScore(),
                "Prioridad 5 * peso 20 debe dar 100 puntos, confirmando que el filtro calcula según el peso configurado sin restricciones internas");
    }

    // Probamos con una prioridad manual superior al rango esperado (por ejemplo, 10), deberia ajustarse y 
    // calcular es con valores dentro del rango, no aplicar tal cual la prioridad manual de 10 * 10 para un score 
    // de 1000, sino que se ajustaria a prioridad 5 * peso 10 para un score de 50
    
    @Test
    void testPrioridadExcesiva_CalculaSinRestriccionesInternas() {
        SolicitudPriorizada entrada = solicitudConPrioridad(10, 0.0);

        SolicitudPriorizada resultado = manualPriorityFilter.execute(entrada);
        assertEquals(50.0, resultado.getScore(),
                "Prioridad 10 ajustada a 5 * peso 10 debe dar 50 puntos");
    } //este falla porque? porque en el diseño inicial estaba confiando en que el controlador haria la validacion 
    // de que la prioridad manual no exceda el rango permitido, pero el filtro como tal carece de dicha validacion
    // y deberia implementarse a futuro un ajsute al mismo


    private SolicitudPriorizada solicitudConPrioridad(Integer prioridad, double scoreInicial) {
        Solicitud solicitud = Solicitud.builder()
                .tipo(TipoSolicitud.CONSULTA)
                .prioridadManual(prioridad)
                .usuario("Test")
                .build();
        return new SolicitudPriorizada(solicitud, scoreInicial);
    }
}
