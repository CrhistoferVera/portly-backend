package com.portly.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ActualizacionAcademicaResponse {

    private Long      idActualizacionAcademica;
    private String    institucion;
    private String    tipo;
    private String    titulo;
    private LocalDate fechaInicio;
    private LocalDate fechaFinalizacion;
    private Boolean   aunNoLoFinalice;
    private String    descripcion;
}
