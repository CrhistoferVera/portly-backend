package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO para actualizar los datos editables del perfil de usuario
// Endpoint: PUT /api/profile
@Data
public class ActualizarPerfilRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    private String titularProfesional;
    private String acercaDeMi;
    private String enlaceFoto;
    private String pais;
    private String ciudad;
}
