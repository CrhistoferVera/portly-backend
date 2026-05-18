package com.portly.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentoProyectoResponse {
    private Integer idDocumentoProyecto;
    private String nombreOriginal;
    private String urlDescarga; // URL to download the file
    private String formato;
    private Long tamanoBytes;
    private LocalDateTime fechaSubida;
}
