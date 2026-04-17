package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "proyecto_repositorio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"proyecto"})
public class ProyectoRepositorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_repositorio", nullable = false)
    @EqualsAndHashCode.Include
    private Long idRepositorio;

    @JsonBackReference("proyecto-repositorios")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto", nullable = false,
                foreignKey = @ForeignKey(name = "fk_proyecto_repositorio_proyecto"))
    private Proyecto proyecto;

    @Column(name = "url", nullable = false, length = 512)
    private String url;
}
