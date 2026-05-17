package com.portly.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request genérico para registrar eventos de tracking desde el portafolio público.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackEventRequest {

    /** ID del portafolio donde ocurre el evento */
    private String portfolioId;

    /** ID de la visita (para actualizar duración) */
    private Long visitId;

    /** Duración en segundos (para visit-duration) */
    private Integer durationSeconds;

    /** ID del proyecto clickeado (para project-click) */
    private Long projectId;

    /** Tipo de sección: EXPERIENCIA o RED_SOCIAL (para section-click) */
    private String sectionType;

    /** ID de referencia del item clickeado */
    private String referenceId;

    /** Nombre legible del item clickeado */
    private String referenceName;

    /** Hash del visitante (generado en el frontend) */
    private String visitorId;
}
