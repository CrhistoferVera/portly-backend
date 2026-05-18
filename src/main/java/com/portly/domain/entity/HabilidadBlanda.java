package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "habilidad_blanda")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabilidadBlanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habilidad_blanda", nullable = false)
    private Integer id;

    @JsonBackReference("usuario-habilidades-blandas")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_habilidad_blanda_usuario"))
    private Usuario usuario;

    @Column(name = "nombre_habilidad", nullable = false, length = 100)
    private String nombreHabilidad;
}
