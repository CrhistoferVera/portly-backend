package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "perfil_usuario",
    uniqueConstraints = @UniqueConstraint(
        name = "perfil_usuario_usuario_unique",
        columnNames = {"usuario_id"}
    )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil", nullable = false)
    private Integer idPerfil;

    @JsonBackReference("usuario-perfil")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_perfil_usuario_usuario"))
    private Usuario usuario;

    @Column(name = "nombres", nullable = false, length = 100)
    private String nombres;

    @Column(name = "apellidos", nullable = false, length = 100)
    private String apellidos;

    @Column(name = "titular_profesional", length = 150)
    private String titularProfesional;

    @Column(name = "sobre_mi", columnDefinition = "TEXT")
    private String sobreMi;

    @Column(name = "foto_url", length = 255)
    private String fotoUrl;

    @Column(name = "pais", length = 100)
    private String pais;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "cv_automatico", nullable = false)
    private Boolean cvAutomatico;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;
}
