package com.portly.service;

import com.portly.domain.entity.ClickProyectoPortafolio;
import com.portly.domain.entity.ClickSeccionPortafolio;
import com.portly.domain.entity.VisitaPortafolio;
import com.portly.domain.repository.ClickProyectoPortafolioRepository;
import com.portly.domain.repository.ClickSeccionPortafolioRepository;
import com.portly.domain.repository.PortafolioRepository;
import com.portly.domain.repository.VisitaPortafolioRepository;
import com.portly.dto.PortfolioAnalyticsResponse;
import com.portly.dto.TrackEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final VisitaPortafolioRepository visitaRepo;
    private final ClickProyectoPortafolioRepository clickProyectoRepo;
    private final ClickSeccionPortafolioRepository clickSeccionRepo;
    private final PortafolioRepository portafolioRepo;

    // ─── Tracking (público) ──────────────────────────────────────────────────

    /** Registra una nueva visita y devuelve el ID para actualizaciones de duración. */
    @Transactional
    public Long trackVisit(TrackEventRequest request) {
        UUID portfolioId = UUID.fromString(request.getPortfolioId());
        VisitaPortafolio visita = VisitaPortafolio.builder()
                .idPortafolio(portfolioId)
                .visitorId(request.getVisitorId())
                .fechaVisita(LocalDateTime.now())
                .duracionSegundos(0)
                .build();
        visitaRepo.save(visita);
        log.debug("Visita registrada: portfolio={}, visitor={}", portfolioId, request.getVisitorId());
        return visita.getIdVisita();
    }

    /** Actualiza la duración de una visita existente. */
    @Transactional
    public void updateVisitDuration(TrackEventRequest request) {
        if (request.getVisitId() == null) return;
        visitaRepo.findById(request.getVisitId()).ifPresent(visita -> {
            visita.setDuracionSegundos(request.getDurationSeconds() != null
                    ? request.getDurationSeconds() : 0);
            visitaRepo.save(visita);
        });
    }

    /** Registra un clic en un proyecto. */
    @Transactional
    public void trackProjectClick(TrackEventRequest request) {
        UUID portfolioId = UUID.fromString(request.getPortfolioId());
        ClickProyectoPortafolio click = ClickProyectoPortafolio.builder()
                .idPortafolio(portfolioId)
                .idProyecto(request.getProjectId())
                .fechaClick(LocalDateTime.now())
                .build();
        clickProyectoRepo.save(click);
    }

    /** Registra un clic en una sección (experiencia o red social). */
    @Transactional
    public void trackSectionClick(TrackEventRequest request) {
        UUID portfolioId = UUID.fromString(request.getPortfolioId());
        ClickSeccionPortafolio click = ClickSeccionPortafolio.builder()
                .idPortafolio(portfolioId)
                .tipoSeccion(request.getSectionType())
                .idReferencia(request.getReferenceId())
                .nombreReferencia(request.getReferenceName())
                .fechaClick(LocalDateTime.now())
                .build();
        clickSeccionRepo.save(click);
    }

    // ─── Consulta de analíticas (autenticado) ───────────────────────────────

    /** Obtiene las analíticas de un portafolio para un periodo dado. */
    @Transactional(readOnly = true)
    public PortfolioAnalyticsResponse getPortfolioAnalytics(UUID userId, UUID portfolioId, String period) {
        // Verificar que el portafolio pertenezca al usuario
        var portafolio = portafolioRepo.findById(portfolioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portafolio no encontrado"));
        if (!portafolio.getUsuario().getIdUsuario().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver estas analíticas");
        }

        // Calcular rango de fechas según el periodo
        LocalDateTime hasta = LocalDateTime.now();
        LocalDateTime desde = calcularDesde(period, hasta);

        // KPIs
        long totalVistas = visitaRepo.countByIdPortafolioAndFechaVisitaBetween(portfolioId, desde, hasta);
        long visitantesUnicos = visitaRepo.countDistinctVisitorsByPortafolioAndFecha(portfolioId, desde, hasta);
        long duracionTotal = visitaRepo.sumDuracionByPortafolioAndFecha(portfolioId, desde, hasta);

        // Datos del gráfico
        List<PortfolioAnalyticsResponse.ChartPoint> chartData = buildChartData(portfolioId, desde, hasta, period);

        // Rankings
        List<PortfolioAnalyticsResponse.RankingItem> proyectos = clickProyectoRepo
                .countByProyecto(portfolioId, desde, hasta)
                .stream()
                .map(row -> PortfolioAnalyticsResponse.RankingItem.builder()
                        .id(row[0] != null ? row[0].toString() : "")
                        .nombre(row[1] != null ? row[1].toString() : "")
                        .clicks(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());

        // Rankings de secciones
        List<PortfolioAnalyticsResponse.RankingItem> experiencias = clickSeccionRepo
                .countBySeccion(portfolioId, "EXPERIENCIA", desde, hasta)
                .stream()
                .map(row -> PortfolioAnalyticsResponse.RankingItem.builder()
                        .id(row[0] != null ? row[0].toString() : "")
                        .nombre(row[1] != null ? row[1].toString() : "")
                        .clicks(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());

        List<PortfolioAnalyticsResponse.RankingItem> redes = clickSeccionRepo
                .countBySeccion(portfolioId, "RED_SOCIAL", desde, hasta)
                .stream()
                .map(row -> PortfolioAnalyticsResponse.RankingItem.builder()
                        .id(row[0] != null ? row[0].toString() : "")
                        .nombre(row[1] != null ? row[1].toString() : "")
                        .clicks(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());

        return PortfolioAnalyticsResponse.builder()
                .totalVistas(totalVistas)
                .visitantesUnicos(visitantesUnicos)
                .duracionTotalSegundos(duracionTotal)
                .chartData(chartData)
                .proyectosRanking(proyectos)
                .experienciasRanking(experiencias)
                .redesSocialesRanking(redes)
                .build();
    }

    /** Obtiene las analíticas globales (todos los portafolios del usuario). */
    @Transactional(readOnly = true)
    public com.portly.dto.GlobalAnalyticsResponse getGlobalAnalytics(UUID userId, String period) {
        LocalDateTime hasta = LocalDateTime.now();
        LocalDateTime desde = calcularDesde(period, hasta);

        List<com.portly.domain.entity.Portafolio> portafolios = portafolioRepo.findByUsuario_IdUsuarioOrderByFechaCreacionDesc(userId);

        long totalVistas = 0;
        long visitantesUnicos = 0;
        long duracionTotal = 0;
        long clicsTotales = 0;

        List<com.portly.dto.GlobalAnalyticsResponse.PortfolioChartSeries> seriesList = new ArrayList<>();

        String[] colores = {"#7c6bec", "#06b6d4", "#10b981", "#f59e0b", "#ef4444"};
        int colorIdx = 0;

        for (com.portly.domain.entity.Portafolio p : portafolios) {
            UUID pId = p.getIdPortafolio();
            totalVistas += visitaRepo.countByIdPortafolioAndFechaVisitaBetween(pId, desde, hasta);
            visitantesUnicos += visitaRepo.countDistinctVisitorsByPortafolioAndFecha(pId, desde, hasta);
            duracionTotal += visitaRepo.sumDuracionByPortafolioAndFecha(pId, desde, hasta);
            
            // contar clics en proyectos de este portafolio
            clicsTotales += clickProyectoRepo.countByProyecto(pId, desde, hasta).stream()
                    .mapToLong(row -> ((Number) row[2]).longValue()).sum();

            List<PortfolioAnalyticsResponse.ChartPoint> chartData = buildChartData(pId, desde, hasta, period);
            
            String color = colores[colorIdx % colores.length];
            colorIdx++;

            seriesList.add(com.portly.dto.GlobalAnalyticsResponse.PortfolioChartSeries.builder()
                    .portfolioId(pId.toString())
                    .portfolioName(p.getNombre() != null && !p.getNombre().isEmpty() ? p.getNombre() : "Portafolio Sin Nombre")
                    .color(color)
                    .data(chartData)
                    .build());
        }

        return com.portly.dto.GlobalAnalyticsResponse.builder()
                .totalVistas(totalVistas)
                .totalClicsProyectos(clicsTotales)
                .visitantesUnicos(visitantesUnicos)
                .duracionTotalSegundos(duracionTotal)
                .chartSeries(seriesList)
                .build();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private LocalDateTime calcularDesde(String period, LocalDateTime hasta) {
        return switch (period != null ? period.toLowerCase() : "all") {
            case "24h" -> hasta.minusHours(24);
            case "7d" -> hasta.minusDays(7);
            case "30d" -> hasta.minusDays(30);
            default -> LocalDateTime.of(2020, 1, 1, 0, 0); // "all" — desde el inicio
        };
    }

    private List<PortfolioAnalyticsResponse.ChartPoint> buildChartData(
            UUID portfolioId, LocalDateTime desde, LocalDateTime hasta, String period) {

        if ("24h".equalsIgnoreCase(period)) {
            // Agrupar por hora del día
            List<Object[]> raw = visitaRepo.countByHour(portfolioId, desde, hasta);
            List<PortfolioAnalyticsResponse.ChartPoint> points = new ArrayList<>();
            for (int h = 0; h < 24; h++) {
                final int hour = h;
                long count = raw.stream()
                        .filter(r -> ((Number) r[0]).intValue() == hour)
                        .findFirst()
                        .map(r -> ((Number) r[1]).longValue())
                        .orElse(0L);
                points.add(PortfolioAnalyticsResponse.ChartPoint.builder()
                        .label(String.format("%02d:00", hour))
                        .value(count)
                        .build());
            }
            return points;
        } else {
            // Agrupar por día
            List<Object[]> raw = visitaRepo.countByDay(portfolioId, desde, hasta);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
            List<PortfolioAnalyticsResponse.ChartPoint> points = new ArrayList<>();

            LocalDate start = desde.toLocalDate();
            LocalDate end = hasta.toLocalDate();
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                final LocalDate day = d;
                long count = raw.stream()
                        .filter(r -> {
                            Object dateObj = r[0];
                            if (dateObj instanceof java.sql.Date) {
                                return ((java.sql.Date) dateObj).toLocalDate().equals(day);
                            }
                            if (dateObj instanceof LocalDate) {
                                return dateObj.equals(day);
                            }
                            return false;
                        })
                        .findFirst()
                        .map(r -> ((Number) r[1]).longValue())
                        .orElse(0L);
                points.add(PortfolioAnalyticsResponse.ChartPoint.builder()
                        .label(day.format(fmt))
                        .value(count)
                        .build());
            }
            return points;
        }
    }
}
