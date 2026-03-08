package com.prueba.tecnica.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prueba.tecnica.dto.CreateSolicitudDTO;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.mapper.SolicitudMapper;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.model.TipoSolicitud;
import com.prueba.tecnica.service.SolicitudService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = { SolicitudCommandController.class, SolicitudQueryController.class })
class SolicitudControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SolicitudService solicitudService;

    @MockBean
    private SolicitudMapper solicitudMapper;

    @Test
    void testCrearSolicitud_Success() throws Exception {
        // Given
        CreateSolicitudDTO dto = CreateSolicitudDTO.builder()
                .usuario("Juan Perez")
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(5)
                .build();

        Solicitud solicitudCreada = Solicitud.builder()
                .id(1L)
                .usuario("Juan Perez")
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(5)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(solicitudMapper.toEntity(any(CreateSolicitudDTO.class))).thenReturn(solicitudCreada);
        when(solicitudService.crearSolicitud(any(Solicitud.class))).thenReturn(solicitudCreada);

        // When & Then
        mockMvc.perform(post("/Solicitud/Crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.usuario").value("Juan Perez"))
                .andExpect(jsonPath("$.tipo").value("INCIDENTE"))
                .andExpect(jsonPath("$.prioridadManual").value(5));
    }

    @Test
    void testCrearSolicitud_ValidationError_UsuarioVacio() throws Exception {
        // Given
        CreateSolicitudDTO dto = CreateSolicitudDTO.builder()
                .usuario("") // usuario vacío - debe fallar
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(5)
                .build();

        // When & Then
        mockMvc.perform(post("/Solicitud/Crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void testCrearSolicitud_ValidationError_PrioridadFueraDeRango() throws Exception {
        // Given
        CreateSolicitudDTO dto = CreateSolicitudDTO.builder()
                .usuario("Juan Perez")
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(10) // fuera de rango 1-5
                .build();

        // When & Then
        mockMvc.perform(post("/Solicitud/Crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.prioridadManual").exists());
    }

    @Test
    void testListarTodas() throws Exception {
        // Given
        Solicitud sol1 = Solicitud.builder()
                .id(1L)
                .usuario("Usuario1")
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(5)
                .fechaCreacion(LocalDateTime.now())
                .build();

        Solicitud sol2 = Solicitud.builder()
                .id(2L)
                .usuario("Usuario2")
                .tipo(TipoSolicitud.CONSULTA)
                .prioridadManual(2)
                .fechaCreacion(LocalDateTime.now())
                .build();

        when(solicitudService.listarTodas()).thenReturn(Arrays.asList(sol1, sol2));

        // When & Then
        mockMvc.perform(get("/Listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].usuario").value("Usuario1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].usuario").value("Usuario2"));
    }

    @Test
    void testListarPriorizadas() throws Exception {
        // Given
        Solicitud sol1 = Solicitud.builder()
                .id(1L)
                .usuario("Usuario1")
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(5)
                .fechaCreacion(LocalDateTime.now())
                .build();

        SolicitudPriorizada priorizada1 = SolicitudPriorizada.builder()
                .solicitud(sol1)
                .score(160.0)
                .build();

        Solicitud sol2 = Solicitud.builder()
                .id(2L)
                .usuario("Usuario2")
                .tipo(TipoSolicitud.CONSULTA)
                .prioridadManual(1)
                .fechaCreacion(LocalDateTime.now())
                .build();

        SolicitudPriorizada priorizada2 = SolicitudPriorizada.builder()
                .solicitud(sol2)
                .score(30.0)
                .build();

        List<SolicitudPriorizada> priorizadas = Arrays.asList(priorizada1, priorizada2);
        when(solicitudService.listarPriorizadas()).thenReturn(priorizadas);

        // When & Then
        mockMvc.perform(get("/Listar/Priorizado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].score").value(160.0))
                .andExpect(jsonPath("$[0].solicitud.id").value(1))
                .andExpect(jsonPath("$[1].score").value(30.0))
                .andExpect(jsonPath("$[1].solicitud.id").value(2));
    }

    @Test
    void testListarTodas_ListaVacia() throws Exception {
        // Given
        when(solicitudService.listarTodas()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/Listar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
