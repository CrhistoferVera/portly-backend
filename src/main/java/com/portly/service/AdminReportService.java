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
}
