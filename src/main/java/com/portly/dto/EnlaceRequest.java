package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO para agregar un enlace profesional (red social, portafolio, etc.)
// Endpoints: POST /api/profile/enlace
@Data
public class EnlaceRequest {

    @NotBlank(message = "La plataforma es obligatoria")
    private String plataforma; // ej: "linkedin", "github", "twitter", "portfolio"

    @NotBlank(message = "La URL del perfil es obligatoria")
    private String urlPerfil;

    private Boolean visible = true; // visible por defecto
}
