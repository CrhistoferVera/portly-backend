package com.portly.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

// DTO de respuesta para una formación académica
@Data
@Builder
public class FormacionAcademicaResponse {

    private Long      idFormacionAcademica;
    private String    institucion;
    private String    carrera;
    private LocalDate fechaInicio;
    private LocalDate fechaFinalizacion;
    private Boolean   actualmenteEstudiando;
    private String    descripcion;
    private String    nivel;
}
