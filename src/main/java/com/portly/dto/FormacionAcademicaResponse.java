package com.portly.dto;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

// DTO de respuesta para una formación académica
@Data
@Builder
public class FormacionAcademicaResponse {

    private Long      idFormacionAcademica;
    private String    institucion;
    private String    carrera;
    private LocalDate fechaEgreso;
    private Boolean   actualmenteEstudiando;
    private String    descripcion;
    private String    nivel;
}
