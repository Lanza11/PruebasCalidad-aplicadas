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
        ReflectionTestUtils.setField(antiquityFilter, "antiquityCap",   100.0);
    }



    // Dado que segun la regla de negocio cada hora completa aporta 1 punto, de este modo tras 10 horas transcurridas el filtro debe agregar exactamente 10 puntos al score previo (en este caso 0.0), confirmando el cálculo correcto de la antigüedad en horas y su aplicación al score.

    @Test
    void testSolicitudDe10Horas_AgregaDiezPuntosDeAntiguedad() {
        SolicitudPriorizada entrada = solicitudConFecha(LocalDateTime.now().minusHours(10), 0.0);

        SolicitudPriorizada resultado = antiquityFilter.execute(entrada);

        assertEquals(10.0, resultado.getScore());
    }


    // De manera similar comprobamos trascurrridas 48 horas, el filtro debe agregar 48 puntos al score previo, confirmando que el cálculo de antigüedad se realiza correctamente y se acumula sobre el score existente.

    @Test
    void testSolicitudDe48Horas_AcumulaCorrectamenteSobreScorePrevio() {
        SolicitudPriorizada entrada = solicitudConFecha(LocalDateTime.now().minusHours(48), 60.0);

        SolicitudPriorizada resultado = antiquityFilter.execute(entrada);

        assertEquals(108.0, resultado.getScore(),
                "60 (puntos previos) + 48 (antigüedad que le agregamos) = 108 puntos");
    }

    
    // En este buscamos evaluar el funcionamiento correcto del cap de antiguedad, el cual tiene como objetivo limitar el crecimeiento infinito en las solicitudes, hasta un maximo de 100 puntos en este apartado.
    
    @Test
    void testSolicitudDe200Horas_ScoreLimitadoPorCap() {
        SolicitudPriorizada entrada = solicitudConFecha(LocalDateTime.now().minusHours(200), 0.0);

        SolicitudPriorizada resultado = antiquityFilter.execute(entrada);

        assertEquals(100.0, resultado.getScore(),
                "200 horas * 1 pt/h = 200 pts, pero el cap es 100: el score debe quedar limitado en 100");
    }


    // Si fechaCreacion es null el filtro no puede calcular antigüedad.
    // Debe dejar el score sin cambios y no lanzar NullPointerException.
    // dado que nisiquiera entraria al bloque de calculo
    
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

    
    // Una fecha en el futuro produce una diferencia de horas negativa (o cero
    // al truncar), por lo que el filtro no debe sumar ningún punto

    @Test
    void testFechaEnFuturo_ScoreNoModificado() {
        SolicitudPriorizada entrada = solicitudConFecha(LocalDateTime.now().plusHours(5), 50.0);

        SolicitudPriorizada resultado = antiquityFilter.execute(entrada);

        assertEquals(50.0, resultado.getScore(),
                "Una fecha futura produce antigüedad negativa: el score no debe cambiar");
    }

    // Una solicitud creada hace menos de una hora completa tiene 0 horas enteras
    // de antigüedad.  El filtro trabaja con horas completas (toHours()), así que
    // no debe agregar puntos.
    
    @Test
    void testSolicitudDeMenosDeUnaHora_ScoreNoModificado() {
        SolicitudPriorizada entrada = solicitudConFecha(LocalDateTime.now().minusMinutes(45), 20.0);

        SolicitudPriorizada resultado = antiquityFilter.execute(entrada);

        assertEquals(20.0, resultado.getScore(),
                "Menos de 1 hora completa = 0 horas enteras: el filtro no debe sumar puntos");
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
