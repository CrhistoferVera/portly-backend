package com.portly.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * POJO que representa el esquema de configuración completo de una plantilla.
 * Contiene las secciones, esquema de colores y familia tipográfica.
 * Se almacena como JSON en la columna esquema_configuracion de la tabla plantilla.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateSchema {

    private List<TemplateSection> sections;
    private Object colorScheme;
    private String fontFamily;
}
