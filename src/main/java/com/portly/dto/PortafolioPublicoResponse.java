package com.portly.dto;

import com.portly.domain.entity.TemplateSchema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PortafolioPublicoResponse {
    private String id;
    private String nombre;
    private String visibilidad;
    private String templateNombre;
    private TemplateSchema templateSchema;
    
    private UsuarioPublico usuario;
    private List<SkillPublica> skills;
    private List<SoftSkillPublica> softSkills;
    private List<ExperienciaPublica> experiencias;
    private List<ProyectoPublico> proyectos;
    private List<FormacionPublica> formaciones;

    @Getter @Builder public static class UsuarioPublico {
        private String nombre;
        private String apellido;
        private String profesion;
        private String descripcion;
        private String avatarUrl;
        private String email;
        private String telefono;
        private String pais;
        private String instagram;
        private String facebook;
        private String youtube;
        private String linkedin;
        private String github;
    }
    
    @Getter @Builder public static class SkillPublica {
        private String id;
        private String name;
        private String level;
    }

    @Getter @Builder public static class SoftSkillPublica {
        private Long id;
        private String nombreHabilidad;
    }

    @Getter @Builder public static class ExperienciaPublica {
        private Long id;
        private String nombreEmpresa;
        private String cargo;
        private String modalidadTrabajo;
        private String fechaInicio;
        private String fechaFin;
        private boolean actualmenteTrabajando;
        private String descripcion;
        private java.util.List<String> funcionesPrincipales;
        private java.util.List<String> logros;
        private String correoJefe;
        private String numeroJefe;
        private String cargoJefe;
    }

    @Getter @Builder public static class ProyectoPublico {
        private Long id;
        private String nombre;
        private String descripcionCorta;
        private String descripcionDetallada;
        private List<String> tecnologias;
        private String urlDemo;
        private String iconoUrl;
    }

    @Getter @Builder public static class FormacionPublica {
        private Long idFormacionAcademica;
        private String institucion;
        private String carrera;
        private String fechaInicio;
        private String fechaFinalizacion;
        private boolean actualmenteEstudiando;
        private String nivel;
        private String descripcion;
    }
}
