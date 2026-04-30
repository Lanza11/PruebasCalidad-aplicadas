package com.prueba.tecnica.exception;

import com.prueba.tecnica.controller.SolicitudCommandController;
import com.prueba.tecnica.mapper.SolicitudMapper;
import com.prueba.tecnica.service.SolicitudService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SolicitudCommandController.class)
public class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SolicitudService solicitudService;

    @MockBean
    private SolicitudMapper solicitudMapper;

    @Test
    void debeManejarJsonInvalido() throws Exception {
        // Cubre: if contains("TipoSolicitud") → FALSE
        mockMvc.perform(post("/Solicitud/Crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{json_invalido}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void debeManejarTipoSolicitudInvalido() throws Exception {
        // Cubre: if contains("TipoSolicitud") → TRUE
        mockMvc.perform(post("/Solicitud/Crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"tipo\": \"INVALIDO\", \"usuario\": \"test\", \"prioridadManual\": 1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Tipo de solicitud inválido. Valores permitidos: INCIDENTE, REQUERIMIENTO, CONSULTA"));
    }

    @Test
    void debeManejarValidacionDeCampos() throws Exception {
        // Cubre: handleValidationExceptions — campos requeridos vacíos
        mockMvc.perform(post("/Solicitud/Crear")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

}
