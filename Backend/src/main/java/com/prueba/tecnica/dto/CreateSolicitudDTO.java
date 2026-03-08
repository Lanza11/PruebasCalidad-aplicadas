package com.prueba.tecnica.dto;

import com.prueba.tecnica.model.TipoSolicitud;
import com.prueba.tecnica.validation.FechaNoFutura;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSolicitudDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String usuario;

    @NotNull(message = "El tipo de solicitud es obligatorio")
    private TipoSolicitud tipo;

    @NotNull(message = "La prioridad manual es obligatoria")
    @Min(value = 1, message = "La prioridad debe ser al menos 1")
    @Max(value = 5, message = "La prioridad no puede ser mayor a 5")
    private Integer prioridadManual;

    @FechaNoFutura(message = "La fecha de creación no puede ser en el futuro")
    private LocalDateTime fechaCreacion;
}
