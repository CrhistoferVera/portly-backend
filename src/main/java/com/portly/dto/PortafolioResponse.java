package com.portly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Respuesta al front al listar o crear portafolios.
 * Coincide con la interfaz Portfolio del frontend.
 */
@Getter
@AllArgsConstructor
@Builder
public class PortafolioResponse {

    private String id;
    private String nombre;
    private String visibilidad;
    private String templateId;
    private String publicUrl;
    private String previewImageUrl;
    private String createdAt;
}
