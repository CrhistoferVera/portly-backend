package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "enlace_profesional")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnlaceProfesional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enlace", nullable = false)
    private Integer idEnlace;

    @JsonBackReference("usuario-enlaces")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_enlace_usuario"))
    private Usuario usuario;

    @Column(name = "plataforma", nullable = false, length = 50)
    private String plataforma;

    @Column(name = "url_perfil", nullable = false, length = 255)
    private String urlPerfil;

    @Column(name = "visible", nullable = false)
    private Boolean visible;
}
