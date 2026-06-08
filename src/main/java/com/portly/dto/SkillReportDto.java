package com.portly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillReportDto {
    private String nombreHabilidad;
    private String tipo;
    private Long cantidadUsuarios;
}
