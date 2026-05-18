package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proyecto_enlace")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"proyecto"})
public class ProyectoEnlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_enlace", nullable = false)
    @EqualsAndHashCode.Include
    private Long idEnlace;

    @JsonBackReference("proyecto-enlaces")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto", nullable = false,
                foreignKey = @ForeignKey(name = "fk_proyecto_enlace_proyecto"))
    private Proyecto proyecto;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "url", nullable = false, length = 512)
    private String url;
}
