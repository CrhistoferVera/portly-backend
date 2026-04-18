package com.portly.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO que recibe el frontend para crear/actualizar experiencias laborales.
 * Coincide exactamente con la interfaz Experience del frontend.
 */
@Data
public class ExperienceRequest {

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
    public static class ReferenciaProfesionalDto {
        private String correoJefe;
        private String numeroJefe;
        private String cargoJefe;
    }
}
