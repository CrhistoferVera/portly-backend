package com.portly.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que coincide exactamente con la interfaz Project del frontend.
 * Se usa para recibir POST/PUT en /api/profile/proyectos.
 */
@Data
public class FrontProjectRequest {

    private Long id;

    private String nombre;

    private String descripcionCorta;

    private String descripcionDetallada;

    private String fechaInicio;

    private String fechaFin;

    private Boolean esActual = false;

    /** Lista de nombres de tecnologías (ej: ["React", "Java"]) */
    private List<String> tecnologias = new ArrayList<>();

    /** "publico" o "privado" */
    private String visibilidad;

    private String urlDemo;

    /** URLs de repositorios */
    private List<String> repositorios = new ArrayList<>();

    private String iconoUrl;

    /** Evidencias ya subidas (con id) */
    private List<EvidenceDto> evidencias = new ArrayList<>();

    @Data
    public static class EvidenceDto {
        private Integer id;
        private String nombre;
        private String url;
        private String tipo;
        private Long pesoBytes;
    }
}
