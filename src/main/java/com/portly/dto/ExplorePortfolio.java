package com.portly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplorePortfolio {
    private String id;
    private String nombre;
    private String templateNombre;
    private String previewImageUrl;
    private ExplorePortfolioPropietario propietario;
    private LocalDateTime createdAt;
}
