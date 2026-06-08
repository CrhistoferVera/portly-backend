package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Perfil profesional alternativo de un usuario.
 * Permite que un mismo usuario presente distintas identidades profesionales
 * (titular, descripción y foto) y asignar una a cada portafolio.
 * Si un portafolio no tiene un perfil profesional asignado, se usa el
 * PerfilUsuario global como fallback.
 */
@Entity
@Table(name = "perfil_profesional")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario"})
public class PerfilProfesional {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_perfil_profesional", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private UUID idPerfilProfesional;

    @JsonBackReference("usuario-perfiles-profesionales")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_perfil_profesional_usuario"))
    private Usuario usuario;

    @Column(name = "etiqueta", nullable = false, length = 100)
    private String etiqueta;

    @Column(name = "titular_profesional", length = 150)
    private String titularProfesional;

    @Column(name = "acerca_de_mi", columnDefinition = "TEXT")
    private String acercaDeMi;

    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}
