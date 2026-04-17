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
        columnNames = {"id_usuario"}
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario"})
public class PerfilUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil_usuario", nullable = false)
    @EqualsAndHashCode.Include
    private Integer idPerfilUsuario;

    @JsonBackReference("usuario-perfil")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true,
                foreignKey = @ForeignKey(name = "fk_perfil_usuario_usuario"))
    private Usuario usuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "titular_profesional", length = 150)
    private String titularProfesional;

    @Column(name = "acerca_de_mi", columnDefinition = "TEXT")
    private String acercaDeMi;

    @Column(name = "enlace_foto", length = 255)
    private String enlaceFoto;

    @Column(name = "pais", length = 100)
    private String pais;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;
}
