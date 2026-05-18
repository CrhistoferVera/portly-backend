package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "actualizacion_academica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizacionAcademica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_actualizacion_academica", nullable = false)
    private Long idActualizacionAcademica;

    @JsonBackReference("usuario-actualizaciones")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_actualizacion_academica_usuario"))
    private Usuario usuario;

    @Column(name = "institucion", nullable = false, length = 120)
    private String institucion;

    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;

    @Column(name = "titulo", nullable = false, length = 120)
    private String titulo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_finalizacion")
    private LocalDate fechaFinalizacion;

    @Column(name = "aun_no_lo_finalice", nullable = false)
    private Boolean aunNoLoFinalice;

    @Column(name = "descripcion", length = 500)
    private String descripcion;
}
