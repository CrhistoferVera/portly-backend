package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

// DTO para agregar o editar una formación académica
// Endpoints: POST /api/profile/formacion  |  PUT /api/profile/formacion/{id}
@Data
public class FormacionAcademicaRequest {

    @NotBlank(message = "La institución es obligatoria")
    @Size(min = 3, max = 120, message = "La institución debe tener entre 3 y 120 caracteres")
    private String institucion;

    @NotBlank(message = "La carrera/título es obligatorio")
    @Size(min = 3, max = 120, message = "La carrera debe tener entre 3 y 120 caracteres")
    private String carrera;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFinalizacion;

    @NotNull(message = "Indica si actualmente estás estudiando")
    private Boolean actualmenteEstudiando;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String descripcion;

    private String nivel;
}
