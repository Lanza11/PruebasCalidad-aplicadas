package com.prueba.tecnica.service.pipeline.rules;

import com.prueba.tecnica.common.pipeline.Filter;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.model.TipoSolicitud;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TypeFilter implements Filter<SolicitudPriorizada> {

    @Value("${priority.weight.incident:100.0}")
    private double incidentScore;

    @Value("${priority.weight.requirement:60.0}")
    private double requirementScore;

    @Value("${priority.weight.query:20.0}")
    private double queryScore;

    @Override
    public SolicitudPriorizada execute(SolicitudPriorizada input) {
        double points = 0;
        if (input.getSolicitud().getTipo() == TipoSolicitud.INCIDENTE) {
            points = incidentScore;
        } else if (input.getSolicitud().getTipo() == TipoSolicitud.REQUERIMIENTO) {
            points = requirementScore;
        } else if (input.getSolicitud().getTipo() == TipoSolicitud.CONSULTA) {
            points = queryScore;
        }

        input.setScore(input.getScore() + points);
        return input;
    }
}
