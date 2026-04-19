package com.portly.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PublicProfesionalResponse {
    private UUID   idUsuario;
    private String nombre;
    private String apellido;
    private String email;
    private String titularProfesional;
    private String acercaDeMi;
    private String enlaceFoto;

    // Redes sociales (null si oculto)
    private String instagram;
    private String facebook;
    private String youtube;

    // Habilidades técnicas (null si oculto)
    private List<SkillDto> habilidadesTecnicas;

    // Habilidades blandas (null si oculto)
    private List<String> habilidadesBlandas;

    // Trayectoria profesional (null si oculto)
    private List<ExperienciaDto> trayectoria;

    // Formación académica (null si oculto)
    private List<FormacionDto> formacion;

    @Data @Builder
    public static class SkillDto {
        private String nombre;
        private String nivel;
    }

    @Data @Builder
    public static class ExperienciaDto {
        private String  empresa;
        private String  cargo;
        private String  fechaInicio;
        private String  fechaFin;
        private String  descripcion;
        private Boolean esEmpleoActual;
    }

    @Data @Builder
    public static class FormacionDto {
        private String  institucion;
        private String  carrera;
        private String  nivel;
        private String  fechaInicio;
        private String  fechaFinalizacion;
        private Boolean actualmenteEstudiando;
    }
}
