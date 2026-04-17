package com.portly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "habilidad_catalogo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabilidadCatalogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_habilidad", nullable = false)
    private Long idHabilidad;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "categoria", length = 50)
    private String categoria;

    @Column(name = "activo", nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "enlace_icono", length = 255)
    private String enlaceIcono;
}
