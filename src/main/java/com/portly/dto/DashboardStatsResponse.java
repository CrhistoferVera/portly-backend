package com.portly.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private long usuariosRegistradosSemana;
    private long portafoliosPublicosSemana;
    private long denunciasPendientes;
    private long cuentasSuspendidas;
    private List<PlantillaStats> plantillasMasUsadas;
    private List<ProfesionStats> profesionesMasRegistradas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlantillaStats {
        private String nombre;
        private long cantidad;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfesionStats {
        private String nombre;
        private long cantidad;
    }
}
