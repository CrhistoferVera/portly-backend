package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "redes_sociales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RedesSociales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_red_social", nullable = false)
    private Integer idRedSocial;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "enlace", nullable = false, length = 255)
    private String enlace;

    @JsonBackReference("perfil-redes")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_perfil_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_redes_sociales_perfil_usuario"))
    private PerfilUsuario perfilUsuario;
}
