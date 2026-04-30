package com.prueba.tecnica.mapper;

import com.prueba.tecnica.dto.CreateSolicitudDTO;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.model.TipoSolicitud;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SolicitudMapperTest {
    private final SolicitudMapper mapper = new SolicitudMapper();

    @Test
    void debeMapearDTOaEntidadCorrectamente() {
        LocalDateTime fecha = LocalDateTime.now();

        CreateSolicitudDTO dto = new CreateSolicitudDTO();
        dto.setUsuario("Juan");
        dto.setTipo(TipoSolicitud.INCIDENTE);
        dto.setPrioridadManual(3);
        dto.setFechaCreacion(fecha);

        Solicitud result = mapper.toEntity(dto);

        assertEquals("Juan", result.getUsuario());
        assertEquals(TipoSolicitud.INCIDENTE, result.getTipo());
        assertEquals(3, result.getPrioridadManual());
        assertEquals(fecha, result.getFechaCreacion());
    }

}
