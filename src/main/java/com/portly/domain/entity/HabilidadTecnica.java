package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "habilidad_tecnica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabilidadTecnica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habilidad", nullable = false)
    private Integer idHabilidad;

    @JsonBackReference("usuario-habilidades")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_habilidad_usuario"))
    private Usuario usuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "nivel", nullable = false, length = 50)
    private String nivel;
}
