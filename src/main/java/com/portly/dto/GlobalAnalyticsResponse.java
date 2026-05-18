package com.portly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalAnalyticsResponse {

    private long totalVistas;
    private long totalClicsProyectos;
    private long visitantesUnicos;
    private long duracionTotalSegundos;

    private List<PortfolioChartSeries> chartSeries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioChartSeries {
        private String portfolioId;
        private String portfolioName;
        private String color;
        private List<PortfolioAnalyticsResponse.ChartPoint> data;
    }
}
