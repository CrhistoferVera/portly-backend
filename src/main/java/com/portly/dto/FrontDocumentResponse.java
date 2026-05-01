package com.portly.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FrontDocumentResponse {
    private Integer id;
    private String nombre;
    private String urlDescarga;
    private String tipo;
    private Long pesoBytes;
}
