package com.portly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateReportDto {
    private String nombrePlantilla;
    private String estadoPlantilla;
    private Long cantidadUsuarios;
}
