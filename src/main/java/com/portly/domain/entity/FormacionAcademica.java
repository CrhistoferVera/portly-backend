package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "formacion_academica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormacionAcademica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_formacion_academica", nullable = false)
    private Long idFormacionAcademica;

    @JsonBackReference("usuario-formaciones")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_formacion_academica_usuario"))
    private Usuario usuario;

    @Column(name = "institucion", nullable = false, length = 120)
    private String institucion;

    @Column(name = "carrera", nullable = false, length = 120)
    private String carrera;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_finalizacion")
    private LocalDate fechaFinalizacion;

    @Column(name = "actualmente_estudiando", nullable = false)
    private Boolean actualmenteEstudiando;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "nivel", length = 50)
    private String nivel;
}
