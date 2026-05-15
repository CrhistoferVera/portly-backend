package com.portly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExplorePortfolioPropietario {
    private String nombre;
    private String apellido;
    private String profesion;
    private String avatarUrl;
}
