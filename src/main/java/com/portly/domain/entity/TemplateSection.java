package com.portly.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO que representa una sección dentro del esquema de una plantilla.
 * Se serializa/deserializa como parte del JSON de esquema_configuracion.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateSection {

    private String type;
    private String title;
    private boolean visible;
    private int order;
}
