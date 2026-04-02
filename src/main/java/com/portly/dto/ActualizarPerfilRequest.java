package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO para actualizar los datos editables del perfil de usuario
// Endpoint: PUT /api/profile
@Data
public class ActualizarPerfilRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombres;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellidos;

    private String titularProfesional;
    private String sobreMi;
    private String fotoUrl;
    private String pais;
    private String ciudad;
}
