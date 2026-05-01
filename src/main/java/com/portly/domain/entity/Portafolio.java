package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa un portafolio creado por un usuario.
 * Asocia un usuario con una plantilla seleccionada y almacena
 * la configuración específica del portafolio (nombre, visibilidad, URL pública).
 */
@Entity
@Table(name = "portafolio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario", "plantilla"})
public class Portafolio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_portafolio", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private UUID idPortafolio;

    @JsonBackReference("usuario-portafolios")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_portafolio_usuario"))
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plantilla", nullable = false,
                foreignKey = @ForeignKey(name = "fk_portafolio_plantilla"))
    private Plantilla plantilla;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "visibilidad", nullable = false, length = 10)
    private String visibilidad;

    @Column(name = "url_publica", nullable = false, unique = true, length = 500)
    private String urlPublica;

    @Column(name = "imagen_vista_previa", length = 500)
    private String imagenVistaPrevia;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
