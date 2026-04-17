package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proyecto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario", "repositorios", "tecnologias", "evidencias"})
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proyecto", nullable = false)
    @EqualsAndHashCode.Include
    private Long idProyecto;

    @JsonBackReference("usuario-proyectos")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_proyecto_usuario"))
    private Usuario usuario;

    // ─── Campos del ER ───────────────────────────────────────────────────────

    /** ID del repositorio en GitHub (solo cuando importado_desde_github = true) */
    @Column(name = "id_repositorio_github", length = 50)
    private String idRepositorioGithub;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "resumen", length = 500)
    private String resumen;

    @Column(name = "descripcion_repositorio", columnDefinition = "TEXT")
    private String descripcionRepositorio;

    /** URL principal del repositorio (puede ser null si se usan proyecto_repositorio) */
    @Column(name = "enlace_repositorio", length = 512)
    private String enlaceRepositorio;

    @Column(name = "enlace_proyecto_despleado", length = 512)
    private String enlaceProyectoDeplegado;

    /** PUBLICO | PRIVADO */
    @Column(name = "estado_publicacion", length = 20)
    private String estadoPublicacion;

    @Column(name = "importado_desde_github", nullable = false)
    @Builder.Default
    private Boolean importadoDesdeGithub = false;

    @Column(name = "fecha_sincronizacion")
    private LocalDateTime fechaSincronizacion;

    // ─── Columnas extra (no en el ER original, Hibernate las crea) ───────────

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "es_actual", nullable = false)
    @Builder.Default
    private Boolean esActual = false;

    /** URL del ícono del proyecto en Cloudinary */
    @Column(name = "enlace_icono", length = 512)
    private String enlaceIcono;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // ─── Relaciones ───────────────────────────────────────────────────────────

    /** URLs adicionales de repositorios (tabla proyecto_repositorio) */
    @OneToMany(mappedBy = "proyecto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProyectoRepositorio> repositorios = new ArrayList<>();

    /** Tecnologías del proyecto (tabla tecnologia_proyecto → habilidad_catalogo) */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tecnologia_proyecto",
        joinColumns        = @JoinColumn(name = "id_proyecto"),
        inverseJoinColumns = @JoinColumn(name = "id_habilidad")
    )
    @Builder.Default
    private List<HabilidadCatalogo> tecnologias = new ArrayList<>();

    /** Evidencias de galería vinculadas a este proyecto */
    @OneToMany(mappedBy = "proyecto", fetch = FetchType.LAZY)
    @Builder.Default
    private List<EvidenciaProyecto> evidencias = new ArrayList<>();
}
