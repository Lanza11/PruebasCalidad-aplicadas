package com.prueba.tecnica.service;

import com.prueba.tecnica.common.pipeline.Pipeline;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.model.TipoSolicitud;
import com.prueba.tecnica.repository.SolicitudRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;

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

    @Captor
    private ArgumentCaptor<Solicitud> solicitudCaptor;

    private Solicitud solicitudTest;

    // Estas pruebas complejas me recueran al leguaje gherkin, given, when, then

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


     // Utilizaremos ArgumentCamptor para estas pruebas complejas, revisamos que el objeto que el servicio 
     // le entrega al repositorio en el save() tiene exactamente los campos esperados, confirmando que el 
     // servicio procesa correctamente la solicitud antes de guardarla. 

     @Test
     void testCrearSolicitud_CaptorVerificaCamposExactosEnviadosAlRepositorio() {

        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudTest);

        solicitudService.crearSolicitud(solicitudTest);

        // capturamos lo que el servicio le entregó al repo y revisamos cada campo
        verify(solicitudRepository).save(solicitudCaptor.capture());
        Solicitud capturado = solicitudCaptor.getValue();

        assertAll("todos los campos deben llegar intactos al repositorio",
                () -> assertEquals(1L,capturado.getId()),
                () -> assertEquals("Juan Perez", capturado.getUsuario()),
                () -> assertEquals(TipoSolicitud.INCIDENTE, capturado.getTipo()),
                () -> assertEquals(5, capturado.getPrioridadManual()),
                () -> assertNotNull(capturado.getFechaCreacion())
        );
    }

    
    // Prueba para comprobar que al crear una solicitud sin fechaCreacion, el servicio asigna 
    // automáticamente la fecha actual antes de guardar. Utilizamos ArgumentCaptor para verificar que 
    // el objeto enviado al repositorio tiene una fechaCreacion no nula, ademas de no ser una fecha 
    // antes o  previa a la llamada

    @Test
    void testCrearSolicitud_SolicitudSinFecha_ServicioAsignaFechaAntesDeGuardar() {
        Solicitud sinFecha = Solicitud.builder()
                .usuario("Carlos Ruiz")
                .tipo(TipoSolicitud.CONSULTA)
                .prioridadManual(2)
                .build();

        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(sinFecha);

        LocalDateTime antesLlamada = LocalDateTime.now();

        solicitudService.crearSolicitud(sinFecha);

        LocalDateTime despuesLlamada = LocalDateTime.now();

        verify(solicitudRepository).save(solicitudCaptor.capture());
        Solicitud capturado = solicitudCaptor.getValue();

        assertNotNull(capturado.getFechaCreacion(),
                "El servicio debe asignar fechaCreacion antes de llamar al repositorio");
        assertFalse(capturado.getFechaCreacion().isBefore(antesLlamada),
                "La fecha asignada no puede ser anterior al inicio de la llamada");
        assertFalse(capturado.getFechaCreacion().isAfter(despuesLlamada),
                "La fecha asignada no puede ser posterior al fin de la llamada");
    }

    
    // Garantiza que el servicio NO sobreescribe una fechaCreacion que el cliente ya proporcionó: si la fecha 
    // ya viene establecida, el captor debe mostrar exactamente la misma fecha en el objeto que llegó al repositorio.
    
    @Test
    void testCrearSolicitud_SolicitudConFechaExistente_FechaNoSobreescrita() {
        LocalDateTime fechaHistorica = LocalDateTime.of(2025, 6, 15, 9, 30);
        Solicitud conFecha = Solicitud.builder()
                .usuario("Ana Torres")
                .tipo(TipoSolicitud.REQUERIMIENTO)
                .prioridadManual(3)
                .fechaCreacion(fechaHistorica)
                .build();

        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(conFecha);

        solicitudService.crearSolicitud(conFecha);

        verify(solicitudRepository).save(solicitudCaptor.capture());
        assertEquals(fechaHistorica, solicitudCaptor.getValue().getFechaCreacion(),
                "El servicio no debe modificar la fechaCreacion si ya estaba establecida");
    }

    
    // Simula un error dentro del pipeline de priorización y verifica que el servicio propaga la excepción 
    // sin suprimirla ni envolverla en otra. También comprueba que el repositorio fue consultado y que el pipeline 
    // fue invocado exactamente una vez antes de fallar.

    @Test
    void testListarPriorizadas_ErrorEnPipeline_ExcepcionPropagadaCorrectamente() {
        Solicitud solicitud = Solicitud.builder()
                .id(1L)
                .usuario("Usuario Test")
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(3)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(solicitudRepository.findAll()).thenReturn(Arrays.asList(solicitud));
        when(prioritizationPipeline.process(any(SolicitudPriorizada.class)))
                .thenThrow(new RuntimeException("Error crítico en el pipeline de priorización"));

        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> solicitudService.listarPriorizadas());

        assertEquals("Error crítico en el pipeline de priorización", excepcion.getMessage());

        verify(solicitudRepository, times(1)).findAll();
        verify(prioritizationPipeline, times(1)).process(any(SolicitudPriorizada.class));
    }


    // Esta prueba busca simular un escenario donde el pipeline de priorización falla parcialmente: 
    // el primer elemento se procesa correctamente, pero el segundo lanza una excepción. 
    // Verificamos que la excepción se propaga y que el pipeline fue invocado para ambos elementos, 
    // confirmando que el error ocurre en el segundo procesamiento y no antes.

    @Test
    void testListarPriorizadas_ErrorParcialEnPipeline_NingunResultadoParcialRetornado() {
        Solicitud s1 = Solicitud.builder()
                .id(1L).usuario("U1").tipo(TipoSolicitud.CONSULTA)
                .prioridadManual(1).fechaCreacion(LocalDateTime.now())
                .build();
        Solicitud s2 = Solicitud.builder()
                .id(2L).usuario("U2").tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(5).fechaCreacion(LocalDateTime.now())
                .build();

        when(solicitudRepository.findAll()).thenReturn(Arrays.asList(s1, s2));

        when(prioritizationPipeline.process(any(SolicitudPriorizada.class)))
                .thenReturn(new SolicitudPriorizada(s1, 30.0))
                .thenThrow(new IllegalStateException("Estado corrupto en el pipeline al procesar segundo elemento"));

        IllegalStateException excepcion = assertThrows(IllegalStateException.class,
                () -> solicitudService.listarPriorizadas());

        assertEquals("Estado corrupto en el pipeline al procesar segundo elemento", excepcion.getMessage());

        // El repo se consultó una sola vez; el pipeline se invocó dos veces (1 ok + 1 fallo)
        verify(solicitudRepository, times(1)).findAll();
        verify(prioritizationPipeline, times(2)).process(any(SolicitudPriorizada.class));
    }
}
