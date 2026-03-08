package com.prueba.tecnica.controller;

import com.prueba.tecnica.dto.CreateSolicitudDTO;
import com.prueba.tecnica.mapper.SolicitudMapper;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Solicitud")
@RequiredArgsConstructor
public class SolicitudCommandController {

    private final SolicitudService solicitudService;
    private final SolicitudMapper solicitudMapper;

    @PostMapping("/Crear")
    public ResponseEntity<Solicitud> crearSolicitud(@Valid @RequestBody CreateSolicitudDTO dto) {
        Solicitud solicitud = solicitudMapper.toEntity(dto);
        return ResponseEntity.ok(solicitudService.crearSolicitud(solicitud));
    }
}
