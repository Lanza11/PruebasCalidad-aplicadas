package com.prueba.tecnica.service.pipeline.rules;

import com.prueba.tecnica.common.pipeline.Filter;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class AntiquityFilter implements Filter<SolicitudPriorizada> {

    @Value("${priority.weight.antiquity:1.0}")
    private double antiquityWeight; // Puntos por hora de antigüedad

    @Value("${priority.cap.antiquity:100.0}")
    private double antiquityCap;

    @Override
    public SolicitudPriorizada execute(SolicitudPriorizada input) {
        double points = Optional.ofNullable(input.getSolicitud().getFechaCreacion())
                .map(this::calculateHours)
                .filter(hours -> hours > 0)
                .map(this::toCappedPoints)
                .orElse(0.0);

        input.setScore(input.getScore() + points);
        return input;
    }

    private long calculateHours(LocalDateTime created) {
        return Duration.between(created, LocalDateTime.now()).toHours();
    }

    private double toCappedPoints(long hours) {
        double points = hours * antiquityWeight;
        return Math.min(points, antiquityCap);
    }
}
