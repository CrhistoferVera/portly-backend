package com.portly.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Respuesta completa de analíticas para un portafolio individual.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioAnalyticsResponse {

    private long totalVistas;
    private long visitantesUnicos;
    private long duracionTotalSegundos;

    /** Datos del gráfico de línea (etiqueta + valor) */
    private List<ChartPoint> chartData;

    /** Ranking de proyectos por clics */
    private List<RankingItem> proyectosRanking;

    /** Ranking de experiencias por clics */
    private List<RankingItem> experienciasRanking;

    /** Ranking de redes sociales por clics */
    private List<RankingItem> redesSocialesRanking;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartPoint {
        private String label;
        private Long value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingItem {
        private String id;
        private String nombre;
        private long clicks;
    }
}
