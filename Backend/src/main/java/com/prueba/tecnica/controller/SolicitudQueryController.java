package com.prueba.tecnica.controller;

import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/Listar")
@RequiredArgsConstructor
public class SolicitudQueryController {

    private final SolicitudService solicitudService;

    @GetMapping
    public ResponseEntity<List<Solicitud>> listarTodas() {
        return ResponseEntity.ok(solicitudService.listarTodas());
    }

    @GetMapping("/Priorizado")
    public ResponseEntity<List<SolicitudPriorizada>> listarPriorizadas() {
        return ResponseEntity.ok(solicitudService.listarPriorizadas());
    }
}
