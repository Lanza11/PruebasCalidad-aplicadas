package com.prueba.tecnica.service.pipeline.rules;

import com.prueba.tecnica.common.pipeline.Filter;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ManualPriorityFilter implements Filter<SolicitudPriorizada> {

    @Value("${priority.weight.manual:10.0}") //Definimos que peso queremos darle a la prioridad manual segun se vea necesario
    private double manualWeight;

    @Override
    public SolicitudPriorizada execute(SolicitudPriorizada input) {
        Integer priority = input.getSolicitud().getPrioridadManual();
        if (priority != null) {
            priority = Math.max(1, Math.min(5, priority)); // Aseguramos que la prioridad esté en el rango 1-5
            input.setScore(input.getScore() + (priority * manualWeight));
        }
        return input;
    }
}
