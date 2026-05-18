package com.portly.dto;

import com.portly.domain.entity.TemplateSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Respuesta que el front espera al hacer GET /api/plantillas.
 * Los nombres de campo coinciden con la interfaz Template del frontend.
 */
@Getter
@AllArgsConstructor
@Builder
public class PlantillaResponse {

    private String id;
    private String nombre;
    private String descripcion;
    private List<String> tags;
    private String previewImageUrl;
    private String previewUrl;
    private PlantillaStatsResponse stats;
    private TemplateSchema schema;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class PlantillaStatsResponse {
        private int secciones;
        private String impacto;
        private String tiempoConfiguracion;
    }
}
