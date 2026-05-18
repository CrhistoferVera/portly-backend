package com.portly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registra clics en secciones específicas de un portafolio público
 * (experiencias laborales o redes sociales).
 */
@Entity
@Table(name = "click_seccion_portafolio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickSeccionPortafolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_click_seccion", nullable = false)
    private Long idClickSeccion;

    @Column(name = "id_portafolio", nullable = false)
    private UUID idPortafolio;

    /** EXPERIENCIA o RED_SOCIAL */
    @Column(name = "tipo_seccion", nullable = false, length = 30)
    private String tipoSeccion;

    /** ID de la experiencia o nombre de la red social */
    @Column(name = "id_referencia", length = 100)
    private String idReferencia;

    /** Nombre legible (ej: "Empresa XYZ" o "LinkedIn") */
    @Column(name = "nombre_referencia", length = 200)
    private String nombreReferencia;

    @Column(name = "fecha_click", nullable = false)
    private LocalDateTime fechaClick;
}
