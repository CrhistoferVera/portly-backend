package com.portly.service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.UsuarioRepository;
import com.portly.dto.SkillReportDto;
import com.portly.dto.TemplateReportDto;
import com.portly.util.ChartPdfUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final UsuarioRepository usuarioRepository;
    private final com.portly.domain.repository.HabilidadBlandaRepository habilidadBlandaRepository;
    private final com.portly.domain.repository.HabilidadTecnicaRepository habilidadTecnicaRepository;
    private final com.portly.domain.repository.PortafolioRepository portafolioRepository;

    // --- Colores iText para estilos de tabla ---
    private static final DeviceRgb COLOR_HEADER_BG  = new DeviceRgb(0x7C, 0x6B, 0xEC); // Morado
    private static final DeviceRgb COLOR_ROW_ALT    = new DeviceRgb(0xF3, 0xF0, 0xFF); // Morado muy claro
    private static final DeviceRgb COLOR_HEADER_TXT = new DeviceRgb(0xFF, 0xFF, 0xFF); // Blanco
    private static final DeviceRgb COLOR_BODY_TXT   = new DeviceRgb(0x1E, 0x1B, 0x4B); // Índigo oscuro
    private static final DeviceRgb COLOR_SEPARATOR  = new DeviceRgb(0xC4, 0xBE, 0xF8); // Morado claro

    // =========================================================================
    // REPORTE DE USUARIOS REGISTRADOS
    // =========================================================================

    public byte[] generateUserReportPdf(LocalDate desde, LocalDate hasta, String estado) {
        LocalDateTime fechaDesde = desde.atStartOfDay();
        LocalDateTime fechaHasta = hasta.atTime(23, 59, 59);
        String estadoFiltro = "Todos".equalsIgnoreCase(estado) ? null : estado.toLowerCase();

        List<Usuario> usuarios = usuarioRepository.findByFechaCreacionBetweenAndEstado(fechaDesde, fechaHasta, estadoFiltro);

        if (usuarios.isEmpty()) {
            return new byte[0];
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Resolver fecha "desde" para el encabezado
            LocalDate displayDesde = desde;
            if (desde.getYear() <= 2000 && !usuarios.isEmpty()) {
                displayDesde = usuarios.stream()
                        .map(Usuario::getFechaCreacion)
                        .filter(f -> f != null)
                        .min(LocalDateTime::compareTo)
                        .map(LocalDateTime::toLocalDate)
                        .orElse(desde);
            }

            // ---- Encabezado ----
            addReportHeader(document, "Reporte de Usuarios Registrados",
                    displayDesde, hasta, "Estado: " + (estado != null ? estado : "Todos"));

            // ---- Tabla de usuarios ----
            float[] columnWidths = {2.5f, 3.5f, 2f, 2f};
            Table table = buildStyledTable(columnWidths, "Nombre Completo", "Correo", "Fecha de Registro", "Estado de Cuenta");

            int rowIndex = 0;
            for (Usuario u : usuarios) {
                String nombreCompleto = u.getPerfil() != null
                        ? u.getPerfil().getNombre() + " " + u.getPerfil().getApellido()
                        : "N/A";
                boolean isAlt = (rowIndex % 2 == 1);

                addStyledRow(table, isAlt,
                        nombreCompleto,
                        u.getEmail() != null ? u.getEmail() : "N/A",
                        u.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        u.getEstado() != null ? u.getEstado() : "N/A");
                rowIndex++;
            }
            document.add(table);

            // ---- Gráfico de BARRAS: registros por mes ----
            addSectionSeparator(document, "Distribución de Registros por Mes");

            Map<String, Long> registrosPorMes = usuarios.stream().collect(
                    Collectors.groupingBy(
                            u -> u.getFechaCreacion().format(DateTimeFormatter.ofPattern("MMM yyyy")),
                            LinkedHashMap::new,
                            Collectors.counting()
                    )
            );

            if (!registrosPorMes.isEmpty()) {
                List<String> meses = new ArrayList<>(registrosPorMes.keySet());
                List<Long> counts = new ArrayList<>(registrosPorMes.values());

                // Limitar a los últimos 12 meses para no saturar
                int startIdx = Math.max(0, meses.size() - 12);
                meses  = meses.subList(startIdx, meses.size());
                counts = counts.subList(startIdx, counts.size());

                ImageData barChartData = ChartPdfUtils.createBarChart(
                        "Usuarios registrados por mes",
                        "Cantidad",
                        meses,
                        counts,
                        580, 360
                );
                document.add(new Image(barChartData)
                        .setWidth(UnitValue.createPercentValue(100))
                        .setMarginTop(8));
            }

            // ---- Gráfico de PASTEL: distribución de estados ----
            addSectionSeparator(document, "Distribución por Estado de Cuenta");

            Map<String, Long> estadosDist = usuarios.stream().collect(
                    Collectors.groupingBy(
                            u -> u.getEstado() != null ? capitalize(u.getEstado()) : "Desconocido",
                            Collectors.counting()
                    )
            );

            if (!estadosDist.isEmpty()) {
                ImageData pieChartData = ChartPdfUtils.createPieChart(
                        "Estado de cuentas de usuarios",
                        estadosDist,
                        460, 280
                );
                document.add(new Image(pieChartData)
                        .setWidth(UnitValue.createPercentValue(80))
                        .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
                        .setMarginTop(8));
            }

            // ---- Resumen de totales ----
            addSummaryFooter(document, "Total de usuarios en el reporte: " + usuarios.size());

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF de reporte de usuarios", e);
            throw new RuntimeException("Error al generar el reporte PDF");
        }
    }

    // =========================================================================
    // REPORTE DE HABILIDADES REGISTRADAS
    // =========================================================================

    public byte[] generateSkillReportPdf(LocalDate desde, LocalDate hasta, String skillType) {
        LocalDateTime fechaDesde = desde.atStartOfDay();
        LocalDateTime fechaHasta = hasta.atTime(23, 59, 59);

        List<SkillReportDto> skills = new ArrayList<>();

        if ("Todas".equalsIgnoreCase(skillType) || "Blandas".equalsIgnoreCase(skillType)) {
            skills.addAll(habilidadBlandaRepository.getSkillReport(fechaDesde, fechaHasta));
        }
        if ("Todas".equalsIgnoreCase(skillType) || "Técnicas".equalsIgnoreCase(skillType)) {
            skills.addAll(habilidadTecnicaRepository.getSkillReport(fechaDesde, fechaHasta));
        }

        if (skills.isEmpty()) {
            return new byte[0];
        }

        // Ordenar de mayor a menor uso
        skills.sort((a, b) -> b.getCantidadUsuarios().compareTo(a.getCantidadUsuarios()));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Resolver fecha "desde" para el encabezado
            LocalDate displayDesde = desde;
            if (desde.getYear() <= 2000) {
                LocalDate minTecnica = habilidadTecnicaRepository.findFirstByOrderByFechaCreacionAsc()
                        .map(com.portly.domain.entity.HabilidadTecnica::getFechaCreacion)
                        .filter(f -> f != null).map(LocalDateTime::toLocalDate).orElse(LocalDate.MAX);
                LocalDate minBlanda = habilidadBlandaRepository.findFirstByOrderByFechaCreacionAsc()
                        .map(com.portly.domain.entity.HabilidadBlanda::getFechaCreacion)
                        .filter(f -> f != null).map(LocalDateTime::toLocalDate).orElse(LocalDate.MAX);
                
                LocalDate minSkill = minTecnica.isBefore(minBlanda) ? minTecnica : minBlanda;
                if (!minSkill.equals(LocalDate.MAX)) {
                    displayDesde = minSkill;
                } else {
                    displayDesde = desde;
                }
            }

            // ---- Encabezado ----
            addReportHeader(document, "Reporte de Habilidades Registradas",
                    displayDesde, hasta, "Tipo: " + skillType);

            // ---- Tabla de habilidades ----
            float[] columnWidths = {4f, 2f, 2.5f};
            Table table = buildStyledTable(columnWidths, "Nombre de Habilidad", "Tipo", "Cantidad de Usuarios");

            int rowIndex = 0;
            for (SkillReportDto s : skills) {
                addStyledRow(table, rowIndex % 2 == 1,
                        s.getNombreHabilidad(),
                        s.getTipo(),
                        String.valueOf(s.getCantidadUsuarios()));
                rowIndex++;
            }
            document.add(table);

            // ---- Gráfico de BARRAS: Top 10 habilidades ----
            addSectionSeparator(document, "Top 10 Habilidades más Registradas");

            List<SkillReportDto> top10 = skills.stream().limit(10).collect(Collectors.toList());
            List<String> nombres = top10.stream().map(SkillReportDto::getNombreHabilidad).collect(Collectors.toList());
            List<Long>   valores = top10.stream().map(SkillReportDto::getCantidadUsuarios).collect(Collectors.toList());

            ImageData barChartData = ChartPdfUtils.createBarChart(
                    "Top 10 Habilidades",
                    "Usuarios",
                    nombres,
                    valores,
                    580, 380
            );
            document.add(new Image(barChartData)
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(8));

            // ---- Gráfico de PASTEL: Blandas vs Técnicas ----
            // Mostrar siempre que haya datos, con comparación insensible a acentos
            addSectionSeparator(document, "Distribución por Tipo de Habilidad");

            long totalBlandas  = skills.stream()
                    .filter(s -> s.getTipo() != null && s.getTipo().toLowerCase().replace("é", "e").contains("blanda"))
                    .mapToLong(SkillReportDto::getCantidadUsuarios).sum();
            long totalTecnicas = skills.stream()
                    .filter(s -> s.getTipo() != null && s.getTipo().toLowerCase().replace("é", "e").contains("tecnica"))
                    .mapToLong(SkillReportDto::getCantidadUsuarios).sum();

            Map<String, Long> tipoDist = new LinkedHashMap<>();
            if (totalBlandas  > 0) tipoDist.put("Habilidades Blandas",   totalBlandas);
            if (totalTecnicas > 0) tipoDist.put("Habilidades Técnicas",  totalTecnicas);

            if (!tipoDist.isEmpty()) {
                ImageData pieChartData = ChartPdfUtils.createPieChart(
                        "Distribución Blandas vs Técnicas",
                        tipoDist,
                        460, 280
                );
                document.add(new Image(pieChartData)
                        .setWidth(UnitValue.createPercentValue(80))
                        .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
                        .setMarginTop(8));
            }

            // ---- Resumen ----
            long totalUsos = skills.stream().mapToLong(SkillReportDto::getCantidadUsuarios).sum();
            addSummaryFooter(document, "Total de habilidades en el reporte: " + totalUsos);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF de reporte de habilidades", e);
            throw new RuntimeException("Error al generar el reporte PDF");
        }
    }

    // =========================================================================
    // REPORTE DE USO DE PLANTILLAS
    // =========================================================================

    public byte[] generateTemplateReportPdf(LocalDate desde, LocalDate hasta, String estado) {
        LocalDateTime fechaDesde = desde.atStartOfDay();
        LocalDateTime fechaHasta = hasta.atTime(23, 59, 59);
        String estadoFiltro = "Todas".equalsIgnoreCase(estado) ? null : estado.toUpperCase().substring(0, 5) + "%";

        List<TemplateReportDto> plantillas = portafolioRepository.getTemplateUsageReport(fechaDesde, fechaHasta, estadoFiltro);

        if (plantillas.isEmpty()) {
            return new byte[0];
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Resolver fecha "desde" para el encabezado
            LocalDate displayDesde = desde;
            if (desde.getYear() <= 2000) {
                displayDesde = portafolioRepository.findFirstByOrderByFechaCreacionAsc()
                        .map(com.portly.domain.entity.Portafolio::getFechaCreacion)
                        .filter(f -> f != null)
                        .map(LocalDateTime::toLocalDate)
                        .orElse(desde);
            }

            // ---- Encabezado ----
            addReportHeader(document, "Reporte de Uso de Plantillas",
                    displayDesde, hasta, "Estado: " + (estado != null ? estado : "Todas"));

            // ---- Tabla de plantillas ----
            float[] columnWidths = {0.8f, 4f, 2.5f, 2f};
            Table table = buildStyledTable(columnWidths, "N°", "Nombre de Plantilla", "Usuarios que la usan", "Estado");

            int index = 1;
            for (TemplateReportDto p : plantillas) {
                boolean isAlt = (index % 2 == 0);
                addStyledRow(table, isAlt,
                        String.valueOf(index),
                        p.getNombrePlantilla(),
                        String.valueOf(p.getCantidadUsuarios()),
                        p.getEstadoPlantilla() != null ? p.getEstadoPlantilla() : "ACTIVA");
                index++;
            }
            document.add(table);

            // ---- Gráfico de BARRAS: Top 10 plantillas más usadas ----
            addSectionSeparator(document, "Top 10 Plantillas más Utilizadas");

            List<TemplateReportDto> top10 = plantillas.stream()
                    .sorted(Comparator.comparing(TemplateReportDto::getCantidadUsuarios).reversed())
                    .limit(10)
                    .collect(Collectors.toList());

            List<String> nombres = top10.stream().map(TemplateReportDto::getNombrePlantilla).collect(Collectors.toList());
            List<Long>   valores = top10.stream().map(TemplateReportDto::getCantidadUsuarios).collect(Collectors.toList());

            ImageData barChartData = ChartPdfUtils.createBarChart(
                    "Top 10 Plantillas más Usadas",
                    "Usuarios",
                    nombres,
                    valores,
                    580, 300
            );
            document.add(new Image(barChartData)
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(8));

            // ---- Gráfico de PASTEL: Activas vs Inactivas ----
            addSectionSeparator(document, "Distribución por Estado de Plantilla");

            Map<String, Long> estadosDist = plantillas.stream().collect(
                    Collectors.groupingBy(
                            p -> p.getEstadoPlantilla() != null ? p.getEstadoPlantilla() : "ACTIVA",
                            Collectors.summingLong(TemplateReportDto::getCantidadUsuarios)
                    )
            );

            if (!estadosDist.isEmpty()) {
                ImageData pieChartData = ChartPdfUtils.createPieChart(
                        "Estado de Plantillas",
                        estadosDist,
                        460, 280
                );
                document.add(new Image(pieChartData)
                        .setWidth(UnitValue.createPercentValue(80))
                        .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER)
                        .setMarginTop(8));
            }

            // ---- Resumen ----
            addSummaryFooter(document, "Total de plantillas en el reporte: " + plantillas.size());

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar PDF de reporte de plantillas", e);
            throw new RuntimeException("Error al generar el reporte PDF");
        }
    }

    // =========================================================================
    // MÉTODOS AUXILIARES DE CONSTRUCCIÓN DE PDF
    // =========================================================================

    /**
     * Agrega un encabezado estilizado al documento con título, fecha de generación y filtros aplicados.
     */
    private void addReportHeader(Document document, String title, LocalDate desde, LocalDate hasta, String extraFilter) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        DateTimeFormatter df  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String now = java.time.ZonedDateTime.now(java.time.ZoneId.of("America/Santiago")).format(dtf);

        // Título principal
        document.add(new Paragraph(title)
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(20)
                .setFontColor(COLOR_BODY_TXT)
                .setMarginBottom(4));

        // Línea decorativa bajo el título
        document.add(new Paragraph("")
                .setBorderBottom(new SolidBorder(COLOR_SEPARATOR, 2))
                .setMarginBottom(8));

        // Metadatos en dos columnas (fecha de generación y filtros)
        float[] metaWidths = {1f, 1f};
        Table metaTable = new Table(UnitValue.createPercentArray(metaWidths)).setWidth(UnitValue.createPercentValue(100));
        metaTable.addCell(new Cell()
                .add(new Paragraph("Fecha de generación: " + now).setFontSize(9).setFontColor(COLOR_BODY_TXT))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        metaTable.addCell(new Cell()
                .add(new Paragraph("Periodo: " + desde.format(df) + " — " + hasta.format(df) + " | " + extraFilter).setFontSize(9).setFontColor(COLOR_BODY_TXT))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        document.add(metaTable);
        document.add(new Paragraph("\n").setMarginBottom(4));
    }

    /**
     * Construye una tabla con encabezados estilizados (fondo morado, texto blanco).
     */
    private Table buildStyledTable(float[] columnWidths, String... headers) {
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100));

        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header).setBold().setFontSize(9).setFontColor(COLOR_HEADER_TXT))
                    .setBackgroundColor(COLOR_HEADER_BG)
                    .setPadding(6)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        }
        return table;
    }

    /**
     * Agrega una fila a la tabla con alternancia de color de fondo.
     */
    private void addStyledRow(Table table, boolean alternate, String... values) {
        DeviceRgb rowBg = alternate ? COLOR_ROW_ALT : new DeviceRgb(255, 255, 255);
        for (String value : values) {
            table.addCell(new Cell()
                    .add(new Paragraph(value != null ? value : "N/A").setFontSize(9).setFontColor(COLOR_BODY_TXT))
                    .setBackgroundColor(rowBg)
                    .setPadding(5)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                    .setBorderBottom(new SolidBorder(COLOR_SEPARATOR, 0.3f)));
        }
    }

    /**
     * Agrega un separador visual con texto de sección antes de los gráficos.
     */
    private void addSectionSeparator(Document document, String sectionTitle) {
        document.add(new Paragraph("\n").setMarginTop(16));
        document.add(new Paragraph(sectionTitle)
                .setBold()
                .setFontSize(12)
                .setFontColor(COLOR_BODY_TXT)
                .setMarginBottom(2));
        document.add(new Paragraph("")
                .setBorderBottom(new SolidBorder(COLOR_SEPARATOR, 1.5f))
                .setMarginBottom(4));
    }

    /**
     * Agrega un pie de página con el total de registros y la nota de generación.
     */
    private void addSummaryFooter(Document document, String summaryText) {
        document.add(new Paragraph("\n").setMarginTop(16));
        document.add(new Paragraph("")
                .setBorderTop(new SolidBorder(COLOR_SEPARATOR, 1f))
                .setMarginBottom(4));
        document.add(new Paragraph(summaryText)
                .setFontSize(9)
                .setFontColor(COLOR_BODY_TXT)
                .setItalic()
                .setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("Generado por Sistema Portly")
                .setFontSize(8)
                .setFontColor(new DeviceRgb(0x9C, 0xA3, 0xAF))
                .setTextAlignment(TextAlignment.RIGHT));
    }

    /** Capitaliza la primera letra de un string. */
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
