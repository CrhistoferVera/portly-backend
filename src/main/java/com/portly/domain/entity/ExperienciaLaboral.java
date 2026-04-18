package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.portly.util.StringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

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

    @Convert(converter = StringListConverter.class)
    @Column(name = "funciones_principales", columnDefinition = "TEXT")
    private List<String> funcionesPrincipales;

    @Convert(converter = StringListConverter.class)
    @Column(name = "logros", columnDefinition = "TEXT")
    private List<String> logros;

    @Column(name = "correo_jefe", length = 255)
    private String correoJefe;

    @Column(name = "numero_jefe", length = 20)
    private String numeroJefe;

    @Column(name = "cargo_jefe", length = 50)
    private String cargoJefe;
}

