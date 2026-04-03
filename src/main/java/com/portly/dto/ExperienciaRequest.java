package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

// DTO para agregar o editar una experiencia laboral
// Endpoints: POST /api/profile/experiencia  |  PUT /api/profile/experiencia/{id}
@Data
public class ExperienciaRequest {

    @NotBlank(message = "La empresa es obligatoria")
    private String empresa;

    @NotBlank(message = "El cargo es obligatorio")
    private String cargo;

    private String modalidad;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaIni;

    private LocalDate fechaFin;

    private String descripcion;

    @NotNull(message = "Indica si es empleo actual")
    private Boolean esEmpleoActual;
}
