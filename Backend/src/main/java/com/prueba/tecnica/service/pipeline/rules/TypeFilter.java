package com.prueba.tecnica.service.pipeline.rules;

import com.prueba.tecnica.common.pipeline.Filter;
import com.prueba.tecnica.dto.SolicitudPriorizada;
import com.prueba.tecnica.model.TipoSolicitud;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.EnumMap;

@Component
public class TypeFilter implements Filter<SolicitudPriorizada> {

    @Value("${priority.weight.incident:100.0}")
    private double incidentScore;

    @Value("${priority.weight.requirement:60.0}")
    private double requirementScore;

    @Value("${priority.weight.query:20.0}")
    private double queryScore;


    //Mejora Mantenibliidad: Al centralizar la asignación de puntajes en un EnumMap
    // Mas facil de leer y entender que los 3 elif que teniamos antes
    @Override
    public SolicitudPriorizada execute(SolicitudPriorizada input) {
        EnumMap<TipoSolicitud, Double> scoreByType = new EnumMap<>(TipoSolicitud.class);
        scoreByType.put(TipoSolicitud.INCIDENTE, incidentScore);
        scoreByType.put(TipoSolicitud.REQUERIMIENTO, requirementScore);
        scoreByType.put(TipoSolicitud.CONSULTA, queryScore);

        double points = scoreByType.getOrDefault(input.getSolicitud().getTipo(), 0.0);

        input.setScore(input.getScore() + points);
        return input;
    }
}
