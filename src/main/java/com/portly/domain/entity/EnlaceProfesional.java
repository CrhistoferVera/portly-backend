package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enlace_perfil")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnlaceProfesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enlace_profesional", nullable = false)
    private Integer idEnlaceProfesional;

    @JsonBackReference("usuario-enlaces")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_enlace_perfil_usuario"))
    private Usuario usuario;

    @Column(name = "plataforma_profesional", nullable = false, length = 50)
    private String plataformaProfesional;

    @Column(name = "direccion_enlace", nullable = false, length = 255)
    private String direccionEnlace;

    @Column(name = "es_visible", nullable = false)
    private Boolean esVisible;
}
