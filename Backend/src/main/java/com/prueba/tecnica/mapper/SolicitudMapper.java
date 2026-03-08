package com.prueba.tecnica.mapper;

import com.prueba.tecnica.dto.CreateSolicitudDTO;
import com.prueba.tecnica.model.Solicitud;
import org.springframework.stereotype.Component;

@Component
public class SolicitudMapper {

    public Solicitud toEntity(CreateSolicitudDTO dto) {
        return Solicitud.builder()
                .usuario(dto.getUsuario())
                .tipo(dto.getTipo())
                .prioridadManual(dto.getPrioridadManual())
                .fechaCreacion(dto.getFechaCreacion())
                .build();
    }
}
