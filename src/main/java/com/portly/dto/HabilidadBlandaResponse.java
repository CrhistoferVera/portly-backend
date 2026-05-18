package com.portly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class HabilidadBlandaResponse {

    private Integer id;
    private String nombreHabilidad;
}
