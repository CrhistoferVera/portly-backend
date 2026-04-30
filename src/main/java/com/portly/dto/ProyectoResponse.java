package com.portly.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProyectoResponse {

    private Long idProyecto;
    private String titulo;
    private String resumen;
    private String descripcionRepositorio;
    private String enlaceIcono;

    private String estadoPublicacion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean esActual;
    private Boolean importadoDesdeGithub;
    private String idRepositorioGithub;
    private LocalDateTime fechaSincronizacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;



    /** Tecnologías del catálogo */
    private List<HabilidadDto> tecnologias;

    /** Evidencias de galería vinculadas */
    private List<EvidenciaProyectoResponse> evidencias;

    @Data @Builder
    public static class HabilidadDto {
        private Long idHabilidad;
        private String nombre;
        private String categoria;
        private String enlaceIcono;
    }
}
