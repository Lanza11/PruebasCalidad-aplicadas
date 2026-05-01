package com.prueba.tecnica.mapper;

import com.prueba.tecnica.dto.CreateSolicitudDTO;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.model.TipoSolicitud;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SolicitudMapperTest {

    private final SolicitudMapper mapper = new SolicitudMapper();

    @Test
    void toEntity_MapeaTodosLosCampos() {
        LocalDateTime fecha = LocalDateTime.now().minusDays(1);
        CreateSolicitudDTO dto = CreateSolicitudDTO.builder()
                .usuario("Pepito")
                .tipo(TipoSolicitud.INCIDENTE)
                .prioridadManual(4)
                .fechaCreacion(fecha)
                .build();

        Solicitud entidad = mapper.toEntity(dto);

        assertEquals("Pepito", entidad.getUsuario());
        assertEquals(TipoSolicitud.INCIDENTE, entidad.getTipo());
        assertEquals(4, entidad.getPrioridadManual());
        assertEquals(fecha, entidad.getFechaCreacion());
    }

    @Test
    void toEntity_CuandoFechaEsNull_MapeaNull() {
        CreateSolicitudDTO dto = CreateSolicitudDTO.builder()
                .usuario("Ana")
                .tipo(TipoSolicitud.CONSULTA)
                .prioridadManual(2)
                .fechaCreacion(null)
                .build();

        Solicitud entidad = mapper.toEntity(dto);

        assertEquals("Ana", entidad.getUsuario());
        assertNull(entidad.getFechaCreacion());
    }
}
