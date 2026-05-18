package com.portly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registra cada visita pública a un portafolio.
 * Permite calcular vistas totales, visitantes únicos y tiempo de visualización.
 */
@Entity
@Table(name = "visita_portafolio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisitaPortafolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_visita", nullable = false)
    private Long idVisita;

    @Column(name = "id_portafolio", nullable = false)
    private UUID idPortafolio;

    @Column(name = "visitor_id", length = 64)
    private String visitorId;

    @Column(name = "fecha_visita", nullable = false)
    private LocalDateTime fechaVisita;

    @Column(name = "duracion_segundos")
    @Builder.Default
    private Integer duracionSegundos = 0;
}
