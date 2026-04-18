package com.portly.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO que devuelve el backend al frontend para experiencias laborales.
 * Coincide exactamente con la interfaz Experience del frontend.
 */
@Data
@Builder
public class ExperienceResponse {

    private Integer id;
    private String nombreEmpresa;
    private String cargo;
    private String fechaInicio;
    private String fechaFin;
    private Boolean actualmenteTrabajando;
    private String descripcion;
    private List<String> funcionesPrincipales;
    private List<String> logros;
    private ReferenciaProfesionalDto referenciaProfesional;

    @Data
    @Builder
    public static class ReferenciaProfesionalDto {
        private String correoJefe;
        private String numeroJefe;
        private String cargoJefe;
    }
}
