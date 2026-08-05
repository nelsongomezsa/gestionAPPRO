package com.gestionap.utils;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.scene.control.Alert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class PdfExporter {

    private static final DeviceRgb HEADER_BG  = new DeviceRgb(108, 99, 255);
    private static final DeviceRgb ROW_ODD    = new DeviceRgb(240, 242, 250);
    private static final DeviceRgb GRAY_TEXT   = new DeviceRgb(120, 120, 140);

    public static void exportar(String titulo, List<String> cabeceras, List<String[]> filas) {
        try {
            String ts   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String path = System.getProperty("user.home") + "/Desktop/" +
                          titulo.replace(" ", "_") + "_" + ts + ".pdf";

            PdfDocument pdf = new PdfDocument(new PdfWriter(path));
            Document doc = new Document(pdf);

            doc.add(new Paragraph(titulo)
                    .setFontSize(18).setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(HEADER_BG)
                    .setMarginBottom(4));

            doc.add(new Paragraph("Generado: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(GRAY_TEXT)
                    .setMarginBottom(14));

            float[] widths = new float[cabeceras.size()];
            Arrays.fill(widths, 1f);
            Table table = new Table(UnitValue.createPercentArray(widths)).useAllAvailableWidth();

            for (String h : cabeceras) {
                table.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE).setFontSize(10))
                        .setBackgroundColor(HEADER_BG)
                        .setPadding(6));
            }

            for (int r = 0; r < filas.size(); r++) {
                DeviceRgb bg = (r % 2 == 0) ? null : ROW_ODD;
                for (String v : filas.get(r)) {
                    Cell cell = new Cell()
                            .add(new Paragraph(v == null ? "" : v).setFontSize(9))
                            .setPadding(5);
                    if (bg != null) cell.setBackgroundColor(bg);
                    table.addCell(cell);
                }
            }

            doc.add(table);
            doc.close();

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("PDF Exportado");
            ok.setHeaderText("Archivo guardado en el Escritorio");
            ok.setContentText(path);
            ok.showAndWait();

        } catch (Exception ex) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Error");
            err.setHeaderText("No se pudo generar el PDF");
            err.setContentText(ex.getMessage());
            err.showAndWait();
        }
    }
}
