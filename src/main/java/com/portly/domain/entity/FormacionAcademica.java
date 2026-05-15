package com.portly.domain.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "fecha_egreso")
    private LocalDate fechaEgreso;

    @Column(name = "actualmente_estudiando", nullable = false)
    private Boolean actualmenteEstudiando;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "nivel", length = 50)
    private String nivel;
}
