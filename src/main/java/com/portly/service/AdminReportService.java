package com.portly.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.portly.domain.entity.Usuario;
import com.portly.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final UsuarioRepository usuarioRepository;
    private final com.portly.domain.repository.HabilidadBlandaRepository habilidadBlandaRepository;
    private final com.portly.domain.repository.HabilidadTecnicaRepository habilidadTecnicaRepository;
    private final com.portly.domain.repository.PortafolioRepository portafolioRepository;

    public byte[] generateUserReportPdf(LocalDate desde, LocalDate hasta, String estado) {
        LocalDateTime fechaDesde = desde.atStartOfDay();
        LocalDateTime fechaHasta = hasta.atTime(23, 59, 59);
        String estadoFiltro = "Todos".equalsIgnoreCase(estado) ? null : estado.toUpperCase();

        List<Usuario> usuarios = usuarioRepository.findByFechaCreacionBetweenAndEstado(fechaDesde, fechaHasta, estadoFiltro);

        if (usuarios.isEmpty()) {
            return new byte[0]; // Retorna array vacío si no hay datos
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título
            Paragraph titulo = new Paragraph("Reporte de Usuarios Registrados")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18);
            document.add(titulo);

            // Metadatos
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            document.add(new Paragraph("Fecha de generación: " + LocalDateTime.now().format(dtf)));
            document.add(new Paragraph(String.format("Filtros - Rango: %s a %s | Estado: %s",
                    desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    estado != null ? estado : "Todos")));
            
            document.add(new Paragraph("\n"));

            // Tabla
            float[] columnWidths = {2, 3, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Encabezados
            table.addHeaderCell(new Cell().add(new Paragraph("Nombre Completo").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Correo").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Fecha de Registro").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Estado de Cuenta").setBold()));

            // Filas
            for (Usuario u : usuarios) {
                String nombreCompleto = u.getPerfil() != null ? 
                    u.getPerfil().getNombre() + " " + u.getPerfil().getApellido() : "N/A";
                
                table.addCell(new Cell().add(new Paragraph(nombreCompleto)));
                table.addCell(new Cell().add(new Paragraph(u.getEmail() != null ? u.getEmail() : "N/A")));
                table.addCell(new Cell().add(new Paragraph(u.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))));
                table.addCell(new Cell().add(new Paragraph(u.getEstado() != null ? u.getEstado() : "N/A")));
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de reporte de usuarios", e);
            throw new RuntimeException("Error al generar el reporte PDF");
        }
    }

    public byte[] generateSkillReportPdf(LocalDate desde, LocalDate hasta, String skillType) {
        LocalDateTime fechaDesde = desde.atStartOfDay();
        LocalDateTime fechaHasta = hasta.atTime(23, 59, 59);

        java.util.List<com.portly.dto.SkillReportDto> skills = new java.util.ArrayList<>();

        if ("Todas".equalsIgnoreCase(skillType) || "Blandas".equalsIgnoreCase(skillType)) {
            skills.addAll(habilidadBlandaRepository.getSkillReport(fechaDesde, fechaHasta));
        }
        
        if ("Todas".equalsIgnoreCase(skillType) || "Técnicas".equalsIgnoreCase(skillType)) {
            skills.addAll(habilidadTecnicaRepository.getSkillReport(fechaDesde, fechaHasta));
        }

        if (skills.isEmpty()) {
            return new byte[0];
        }

        skills.sort((a, b) -> b.getCantidadUsuarios().compareTo(a.getCantidadUsuarios()));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Paragraph titulo = new Paragraph("Reporte de Habilidades Registradas")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18);
            document.add(titulo);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            document.add(new Paragraph("Fecha de generación: " + LocalDateTime.now().format(dtf)));
            document.add(new Paragraph(String.format("Filtros - Rango: %s a %s | Tipo: %s",
                    desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    skillType != null ? skillType : "Todas")));
            
            document.add(new Paragraph("\n"));

            float[] columnWidths = {4, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(new Cell().add(new Paragraph("Nombre de Habilidad").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Tipo").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Cantidad de Usuarios").setBold()));

            for (com.portly.dto.SkillReportDto s : skills) {
                table.addCell(new Cell().add(new Paragraph(s.getNombreHabilidad())));
                table.addCell(new Cell().add(new Paragraph(s.getTipo())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(s.getCantidadUsuarios()))));
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de reporte de habilidades", e);
            throw new RuntimeException("Error al generar el reporte PDF");
        }
    }

    public byte[] generateTemplateReportPdf(LocalDate desde, LocalDate hasta, String estado) {
        LocalDateTime fechaDesde = desde.atStartOfDay();
        LocalDateTime fechaHasta = hasta.atTime(23, 59, 59);
        String estadoFiltro = "Todas".equalsIgnoreCase(estado) ? null : 
            ("Activas".equalsIgnoreCase(estado) ? "ACTIVA" : "INACTIVA");

        List<com.portly.dto.TemplateReportDto> plantillas = portafolioRepository.getTemplateUsageReport(fechaDesde, fechaHasta, estadoFiltro);

        if (plantillas.isEmpty()) {
            return new byte[0];
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            Paragraph titulo = new Paragraph("Reporte de Uso de Plantillas")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(18);
            document.add(titulo);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            document.add(new Paragraph("Fecha de generación: " + LocalDateTime.now().format(dtf)));
            document.add(new Paragraph(String.format("Filtros - Rango: %s a %s | Estado: %s",
                    desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    estado != null ? estado : "Todas")));
            
            document.add(new Paragraph("\n"));

            float[] columnWidths = {1, 4, 3, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addHeaderCell(new Cell().add(new Paragraph("N°").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Nombre de Plantilla").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Usuarios que la usan").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Estado").setBold()));

            int index = 1;
            for (com.portly.dto.TemplateReportDto p : plantillas) {
                table.addCell(new Cell().add(new Paragraph(String.valueOf(index++))));
                table.addCell(new Cell().add(new Paragraph(p.getNombrePlantilla())));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(p.getCantidadUsuarios()))));
                table.addCell(new Cell().add(new Paragraph(p.getEstadoPlantilla() != null ? p.getEstadoPlantilla() : "ACTIVA")));
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error al generar PDF de reporte de plantillas", e);
            throw new RuntimeException("Error al generar el reporte PDF");
        }
    }
}
