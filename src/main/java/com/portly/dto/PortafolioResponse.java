package com.portly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

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
    private ItemVisibilidadDto itemVisibilidad;

    @Getter
    @AllArgsConstructor
    @Builder
    public static class ItemVisibilidadDto {
        private Boolean showProjects;
        private Map<String, Boolean> techSkillItems;
        private Map<String, Boolean> softSkillItems;
        private Map<String, Boolean> experienceItems;
        private Map<String, Boolean> educationItems;
        private Map<String, Boolean> projectItems;
    }
}
