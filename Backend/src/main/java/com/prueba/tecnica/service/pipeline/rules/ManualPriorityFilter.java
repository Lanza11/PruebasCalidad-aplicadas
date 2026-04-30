package com.prueba.tecnica.service.pipeline.rules;

import com.prueba.tecnica.common.pipeline.Filter;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ManualPriorityFilter implements Filter<SolicitudPriorizada> {

    @Value("${priority.weight.manual:10.0}") //Definimos que peso queremos darle a la prioridad manual segun se vea necesario
    private double manualWeight;

    @Override
    public SolicitudPriorizada execute(SolicitudPriorizada input) {
        double points = Optional.ofNullable(input.getSolicitud().getPrioridadManual())
                .map(this::capManPriority)
                .map(priority -> priority * manualWeight)
                .orElse(0.0);

        input.setScore(input.getScore() + points);
        return input;
    }

    private int capManPriority(int priority) {
        return Math.max(1, Math.min(5, priority));
    }
}
