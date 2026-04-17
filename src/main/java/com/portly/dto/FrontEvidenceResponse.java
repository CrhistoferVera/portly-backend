package com.portly.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO que coincide con la interfaz ProjectEvidence del frontend.
 */
@Data
@Builder
public class FrontEvidenceResponse {

    private Integer id;

    private String nombre;

    private String url;

    private String tipo;

    private Long pesoBytes;
}
