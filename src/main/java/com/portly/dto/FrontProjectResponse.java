package com.portly.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO de respuesta que coincide exactamente con la interfaz Project del frontend.
 */
@Data
@Builder
public class FrontProjectResponse {

    private Long id;

    private String nombre;

    private String descripcionCorta;

    private String descripcionDetallada;

    private String fechaInicio;

    private String fechaFin;

    private Boolean esActual;

    private List<String> tecnologias;

    private String visibilidad;

    private String urlDemo;

    private List<String> repositorios;

    private String iconoUrl;

    private List<FrontEvidenceResponse> evidencias;
}
