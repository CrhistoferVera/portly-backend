package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "experiencia_laboral")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExperienciaLaboral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_experiencia_laboral", nullable = false)
    private Integer idExperienciaLaboral;

    @JsonBackReference("usuario-experiencias")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_exp_laboral_usuario"))
    private Usuario usuario;

    @Column(name = "empresa", nullable = false, length = 150)
    private String empresa;

    @Column(name = "cargo", nullable = false, length = 100)
    private String cargo;

    @Column(name = "modalidad_trabajo", length = 20)
    private String modalidadTrabajo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "es_empleo_actual", nullable = false)
    private Boolean esEmpleoActual;
}
