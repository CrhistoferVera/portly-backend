package com.portly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registra cada clic en un proyecto dentro de un portafolio público.
 */
@Entity
@Table(name = "click_proyecto_portafolio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClickProyectoPortafolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_click", nullable = false)
    private Long idClick;

    @Column(name = "id_portafolio", nullable = false)
    private UUID idPortafolio;

    @Column(name = "id_proyecto", nullable = false)
    private Long idProyecto;

    @Column(name = "fecha_click", nullable = false)
    private LocalDateTime fechaClick;
}
