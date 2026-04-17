package com.portly.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EvidenciaProyectoResponse {

    private Integer idEvidenciaProyecto;

    /** Nombre original del archivo (tal como lo subió el usuario) */
    private String nombreOriginal;

    /** URL pública de la imagen en Cloudinary */
    private String enlaceEvidencia;

    /** URL de miniatura generada por Cloudinary */
    private String enlaceMiniatura;

    /** Formato del archivo: PNG, JPG o GIF */
    private String formato;

    /** Tamaño del archivo en bytes */
    private Long tamanoBytes;

    private LocalDateTime fechaSubida;
}
