package com.portly.util;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Utilidad para generar gráficas JFreeChart con estilos personalizados
 * y convertirlas en ImageData lista para ser incrustada en PDFs iText.
 *
 * Todas las gráficas se renderizan a 2× resolución (Retina) para garantizar
 * texto nítido cuando iText escala la imagen al tamaño de página.
 */
public class ChartPdfUtils {

    /**
     * Factor de escala para renderizado de alta resolución.
     * El chart se genera a SCALE× píxeles; iText lo reduce al tamaño lógico,
     * produciendo texto nítido equivalente a ~144 DPI en el PDF.
     */
    private static final int SCALE = 2;

    // --- Paleta de colores corporativa ---
    private static final Color COLOR_PRIMARY = new Color(0x7C, 0x6B, 0xEC);
    private static final Color COLOR_SUCCESS = new Color(0x4A, 0xDE, 0x80);
    private static final Color COLOR_WARNING = new Color(0xFB, 0xBF, 0x24);
    private static final Color COLOR_DANGER  = new Color(0xF8, 0x71, 0x71);
    private static final Color COLOR_BG      = new Color(0xF8, 0xF9, 0xFF);
    private static final Color COLOR_GRID    = new Color(0xE2, 0xE8, 0xF0);
    private static final Color COLOR_TEXT    = new Color(0x1E, 0x1B, 0x4B);

    /** Paleta de colores para el gráfico de pastel */
    private static final Color[] PIE_COLORS = {
            COLOR_SUCCESS, COLOR_DANGER, COLOR_WARNING,
            new Color(0x60, 0xA5, 0xFA),
            new Color(0xC0, 0x84, 0xFC)
    };

    private ChartPdfUtils() { /* Clase estática, no instanciar */ }

    // -------------------------------------------------------------------------
    // GRÁFICO DE BARRAS VERTICAL
    // -------------------------------------------------------------------------

    /**
     * Genera un gráfico de barras verticales.
     * La imagen se renderiza a SCALE× resolución para máxima nitidez en PDF.
     */
    public static ImageData createBarChart(
            String title,
            String axisLabel,
            List<String> labels,
            List<Long> values,
            int width,
            int height) throws IOException {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < labels.size(); i++) {
            dataset.addValue(values.get(i), axisLabel, labels.get(i));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title, "", axisLabel, dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        styleBarChart(chart);
        return toImageData(chart, width * SCALE, height * SCALE);
    }

    /**
     * Gráfico de barras horizontales — útil cuando los nombres son largos.
     */
    public static ImageData createHorizontalBarChart(
            String title,
            String axisLabel,
            List<String> labels,
            List<Long> values,
            int width,
            int height) throws IOException {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < labels.size(); i++) {
            dataset.addValue(values.get(i), axisLabel, labels.get(i));
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title, "", axisLabel, dataset,
                PlotOrientation.HORIZONTAL,
                false, true, false
        );

        styleBarChart(chart);
        return toImageData(chart, width * SCALE, height * SCALE);
    }

    // -------------------------------------------------------------------------
    // GRÁFICO DE PASTEL
    // -------------------------------------------------------------------------

    /**
     * Genera un gráfico de pastel con etiquetas nombre + cantidad + porcentaje.
     */
    public static ImageData createPieChart(
            String title,
            Map<String, Long> data,
            int width,
            int height) throws IOException {

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        data.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);

        chart.setBackgroundPaint(COLOR_BG);
        chart.setBorderVisible(false);

        // Título escalado
        TextTitle chartTitle = chart.getTitle();
        chartTitle.setFont(new Font("SansSerif", Font.BOLD, 14 * SCALE));
        chartTitle.setPaint(COLOR_TEXT);

        @SuppressWarnings("unchecked")
        PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
        plot.setBackgroundPaint(COLOR_BG);
        plot.setOutlineVisible(false);
        plot.setShadowPaint(null);

        // Colores de secciones
        int colorIndex = 0;
        for (String key : data.keySet()) {
            plot.setSectionPaint(key, PIE_COLORS[colorIndex % PIE_COLORS.length]);
            colorIndex++;
        }

        // Etiqueta: "NombreEstado\n42 (55.3%)"
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0}\n{1} ({2})",
                new DecimalFormat("0"),
                new DecimalFormat("0.0%")
        ));
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 11 * SCALE));
        plot.setLabelPaint(COLOR_TEXT);
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(COLOR_GRID);
        plot.setLabelShadowPaint(null);
        plot.setSimpleLabels(false);
        // Espacio entre la torta y el borde para que los labels no se corten
        plot.setInteriorGap(0.08);

        return toImageData(chart, width * SCALE, height * SCALE);
    }

    // -------------------------------------------------------------------------
    // MÉTODOS PRIVADOS
    // -------------------------------------------------------------------------

    /** Aplica estilos visuales a un gráfico de barras con fuentes escaladas a SCALE×. */
    private static void styleBarChart(JFreeChart chart) {
        chart.setBackgroundPaint(COLOR_BG);
        chart.setBorderVisible(false);

        // Título
        TextTitle chartTitle = chart.getTitle();
        if (chartTitle != null) {
            chartTitle.setFont(new Font("SansSerif", Font.BOLD, 14 * SCALE));
            chartTitle.setPaint(COLOR_TEXT);
        }

        // Plot
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(COLOR_GRID);
        plot.setRangeGridlineStroke(new BasicStroke(0.8f * SCALE));
        plot.setDomainGridlinesVisible(false);

        // Eje X — etiquetas rotadas 45° con fuente escalada, sin truncar
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 9 * SCALE));
        domainAxis.setTickLabelPaint(COLOR_TEXT);
        domainAxis.setAxisLinePaint(COLOR_GRID);
        domainAxis.setCategoryMargin(0.2);
        domainAxis.setMaximumCategoryLabelLines(1);
        domainAxis.setCategoryLabelPositions(
                org.jfree.chart.axis.CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)
        );

        // Eje Y — margen superior para que los labels sobre las barras no se corten
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10 * SCALE));
        rangeAxis.setLabelFont(new Font("SansSerif", Font.BOLD, 11 * SCALE));
        rangeAxis.setTickLabelPaint(COLOR_TEXT);
        rangeAxis.setAxisLinePaint(COLOR_GRID);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setUpperMargin(0.15);

        // Renderer de barras
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, COLOR_PRIMARY);
        renderer.setMaximumBarWidth(0.08);

        // Etiquetas encima de las barras con fuente escalada
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0"))
        );
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.BOLD, 10 * SCALE));
        renderer.setDefaultItemLabelPaint(COLOR_TEXT);
        renderer.setDefaultPositiveItemLabelPosition(
                new org.jfree.chart.labels.ItemLabelPosition(
                        org.jfree.chart.labels.ItemLabelAnchor.OUTSIDE12,
                        org.jfree.chart.ui.TextAnchor.BOTTOM_CENTER
                )
        );
    }

    /** Renderiza un {@link JFreeChart} a {@link ImageData} de iText vía PNG en memoria. */
    private static ImageData toImageData(JFreeChart chart, int width, int height) throws IOException {
        // TYPE_INT_RGB produce PNG más nítido (sin canal alpha innecesario)
        BufferedImage image = chart.createBufferedImage(width, height, BufferedImage.TYPE_INT_RGB, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return ImageDataFactory.create(baos.toByteArray());
    }
}
