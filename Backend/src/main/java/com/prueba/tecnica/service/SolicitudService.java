package com.prueba.tecnica.service;

import com.prueba.tecnica.common.pipeline.Pipeline;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.model.Solicitud;
import com.prueba.tecnica.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final Pipeline<SolicitudPriorizada> prioritizationPipeline;

    public Solicitud crearSolicitud(Solicitud solicitud) {
        return solicitudRepository.save(solicitud);
    }

    public List<Solicitud> listarTodas() {
        return solicitudRepository.findAll();
    }

    public List<SolicitudPriorizada> listarPriorizadas() {
        List<Solicitud> todas = solicitudRepository.findAll();

        return todas.stream()
                .map(solicitud -> new SolicitudPriorizada(solicitud, 0))
                .map(prioritizationPipeline::process)
                .sorted(Comparator.comparingDouble(SolicitudPriorizada::getScore).reversed()) // Higher score first
                .collect(Collectors.toList());
    }
}
