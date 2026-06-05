package com.portly.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO que devuelve el backend al frontend para un perfil profesional.
 */
@Data
@Builder
public class PerfilProfesionalResponse {

    private String id;
    private String etiqueta;
    private String titularProfesional;
    private String acercaDeMi;
    private String fotoUrl;
    private String fechaCreacion;
}
