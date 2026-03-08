package com.prueba.tecnica.service;

import com.prueba.tecnica.common.pipeline.Pipeline;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.model.TipoSolicitud;
import com.prueba.tecnica.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudServiceTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private Pipeline<SolicitudPriorizada> prioritizationPipeline;

    @InjectMocks
    private SolicitudService solicitudService;

    private Solicitud solicitudTest;

    @BeforeEach
    void setUp() {
        solicitudTest = Solicitud.builder()
                .id(1L)
                .usuario("Juan Perez")
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(5)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }

    @Test
    void testCrearSolicitud() {
        // Given
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudTest);

        // When
        Solicitud resultado = solicitudService.crearSolicitud(solicitudTest);

        // Then
        assertNotNull(resultado);
        assertEquals("Juan Perez", resultado.getUsuario());
        assertEquals(TipoSolicitud.INCIDENTE, resultado.getTipo());
        assertEquals(5, resultado.getPrioridadManual());
        verify(solicitudRepository, times(1)).save(solicitudTest);
    }

    @Test
    void testListarTodas() {
        // Given
        Solicitud solicitud2 = Solicitud.builder()
                .id(2L)
                .usuario("Maria Lopez")
                .tipo(TipoSolicitud.CONSULTA)
                .prioridadManual(2)
                .fechaCreacion(LocalDateTime.now())
                .build();

        List<Solicitud> solicitudes = Arrays.asList(solicitudTest, solicitud2);
        when(solicitudRepository.findAll()).thenReturn(solicitudes);

        // When
        List<Solicitud> resultado = solicitudService.listarTodas();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Juan Perez", resultado.get(0).getUsuario());
        assertEquals("Maria Lopez", resultado.get(1).getUsuario());
        verify(solicitudRepository, times(1)).findAll();
    }

    @Test
    void testListarPriorizadas() {
        // Given
        Solicitud incidente = Solicitud.builder()
                .id(1L)
                .usuario("Usuario1")
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(5)
                .fechaCreacion(LocalDateTime.now().minusHours(2))
                .build();

        Solicitud consulta = Solicitud.builder()
                .id(2L)
                .usuario("Usuario2")
                .tipo(TipoSolicitud.CONSULTA)
                .prioridadManual(1)
                .fechaCreacion(LocalDateTime.now())
                .build();

        List<Solicitud> solicitudes = Arrays.asList(incidente, consulta);
        when(solicitudRepository.findAll()).thenReturn(solicitudes);

        // Simular el pipeline que asigna scores
        when(prioritizationPipeline.process(any(SolicitudPriorizada.class)))
                .thenAnswer(invocation -> {
                    SolicitudPriorizada input = invocation.getArgument(0);
                    // Simular scores (incidente debería tener mayor score)
                    if (input.getSolicitud().getTipo() == TipoSolicitud.INCIDENTE) {
                        input.setScore(160.0); // 100 (tipo) + 50 (manual) + 10 (antigüedad estimada)
                    } else {
                        input.setScore(30.0); // 20 (tipo) + 10 (manual) + 0 (antigüedad)
                    }
                    return input;
                });

        // When
        List<SolicitudPriorizada> resultado = solicitudService.listarPriorizadas();

        // Then
        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        // Verificar que están ordenadas por score descendente
        assertTrue(resultado.get(0).getScore() > resultado.get(1).getScore());
        assertEquals(TipoSolicitud.INCIDENTE, resultado.get(0).getSolicitud().getTipo());
        assertEquals(TipoSolicitud.CONSULTA, resultado.get(1).getSolicitud().getTipo());

        verify(solicitudRepository, times(1)).findAll();
        verify(prioritizationPipeline, times(2)).process(any(SolicitudPriorizada.class));
    }

    @Test
    void testListarPriorizadas_ListaVacia() {
        // Given
        when(solicitudRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<SolicitudPriorizada> resultado = solicitudService.listarPriorizadas();

        // Then
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(solicitudRepository, times(1)).findAll();
        verify(prioritizationPipeline, never()).process(any());
    }
}
