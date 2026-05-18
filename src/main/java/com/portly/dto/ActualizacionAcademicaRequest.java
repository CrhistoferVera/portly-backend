package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ActualizacionAcademicaRequest {

    @NotBlank(message = "La institución es obligatoria")
    @Size(min = 1, max = 120, message = "La institución debe tener entre 1 y 120 caracteres")
    private String institucion;

    @NotBlank(message = "Debes seleccionar un tipo")
    @Size(max = 50, message = "El tipo no puede superar los 50 caracteres")
    private String tipo;

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 1, max = 120, message = "El título debe tener entre 1 y 120 caracteres")
    private String titulo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFinalizacion;

    @NotNull(message = "Indica si aún no lo finalizaste")
    private Boolean aunNoLoFinalice;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;
}
