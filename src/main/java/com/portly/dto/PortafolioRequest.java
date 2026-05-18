package com.portly.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Request para crear un nuevo portafolio.
 * Coincide con el CreatePortfolioDto del frontend.
 */
@Getter
@NoArgsConstructor
public class PortafolioRequest {

    @NotBlank(message = "El ID de la plantilla es obligatorio")
    private String templateId;

    @NotBlank(message = "El nombre del portafolio es obligatorio")
    @Size(max = 200, message = "El nombre no puede superar los 200 caracteres")
    private String nombre;

    @NotBlank(message = "La visibilidad es obligatoria")
    private String visibilidad;
}
