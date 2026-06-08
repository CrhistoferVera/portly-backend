package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Payload para crear o actualizar un perfil profesional.
 */
@Data
public class PerfilProfesionalRequest {

    @NotBlank
    @Size(max = 100)
    private String etiqueta;

    @Size(max = 150)
    private String titularProfesional;

    private String acercaDeMi;

    @Size(max = 255)
    private String fotoUrl;
}
