package com.portly.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documento_proyecto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"usuario", "proyecto"})
public class DocumentoProyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento_proyecto", nullable = false)
    @EqualsAndHashCode.Include
    private Integer idDocumentoProyecto;

    @JsonBackReference("usuario-documentos")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false,
                foreignKey = @ForeignKey(name = "fk_documento_proyecto_usuario"))
    private Usuario usuario;

    /** Proyecto al que pertenece este documento (nullable hasta que el proyecto se guarda) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proyecto",
                foreignKey = @ForeignKey(name = "fk_documento_proyecto_proyecto"))
    private Proyecto proyecto;

    /** Nombre original del archivo subido por el usuario */
    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    /** Ruta local física o relativa del archivo */
    @Column(name = "ruta_local", nullable = false, length = 512)
    private String rutaLocal;

    /** Formato del archivo: pdf, doc, docx */
    @Column(name = "formato", nullable = false, length = 10)
    private String formato;

    /** Tamaño en bytes del archivo */
    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "fecha_subida", nullable = false)
    private LocalDateTime fechaSubida;
}
