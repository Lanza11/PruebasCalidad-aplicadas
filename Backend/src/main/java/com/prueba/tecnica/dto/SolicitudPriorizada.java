package com.prueba.tecnica.dto;

import com.prueba.tecnica.model.Solicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudPriorizada {
    private Solicitud solicitud;
    private double score;
}
