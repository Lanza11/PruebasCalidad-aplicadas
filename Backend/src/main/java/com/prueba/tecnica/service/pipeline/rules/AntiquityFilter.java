package com.prueba.tecnica.service.pipeline.rules;

import com.prueba.tecnica.common.pipeline.Filter;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class AntiquityFilter implements Filter<SolicitudPriorizada> {

    @Value("${priority.weight.antiquity:1.0}")
    private double antiquityWeight; // Puntos por hora de antigüedad

    @Value("${priority.cap.antiquity:100.0}")
    private double antiquityCap;

    @Override
    public SolicitudPriorizada execute(SolicitudPriorizada input) {
        LocalDateTime created = input.getSolicitud().getFechaCreacion();
        if (created != null) {
            long hours = Duration.between(created, LocalDateTime.now()).toHours();
            if (hours > 0) {
                double points = hours * antiquityWeight;
                // aplicamos el cap para evitar crecimiento infinito
                double finalPoints = Math.min(points, antiquityCap);
                input.setScore(input.getScore() + finalPoints);
            }
        }
        return input;
    }
}
