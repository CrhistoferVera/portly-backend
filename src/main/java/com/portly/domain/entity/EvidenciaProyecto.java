package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evidencia_proyecto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario", "proyecto"})
public class EvidenciaProyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia_proyecto", nullable = false)
    @EqualsAndHashCode.Include
    private Integer idEvidenciaProyecto;

    @JsonBackReference("usuario-evidencias")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_evidencia_proyecto_usuario"))
    private Usuario usuario;

    /** Proyecto al que pertenece esta evidencia (nullable hasta que el proyecto se guarda) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto",
                foreignKey = @ForeignKey(name = "fk_evidencia_proyecto_proyecto"))
    private Proyecto proyecto;

    /** Nombre original del archivo subido por el usuario */
    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    /** URL pública generada por Cloudinary */
    @Column(name = "enlace_evidencia", nullable = false, length = 512)
    private String enlaceEvidencia;

    /** Formato del archivo: PNG, JPG o GIF */
    @Column(name = "formato", nullable = false, length = 10)
    private String formato;

    /** Tamaño en bytes del archivo */
    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    /** URL de miniatura generada por Cloudinary (versión reducida) */
    @Column(name = "enlace_miniatura", length = 512)
    private String enlaceMiniatura;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;
}
