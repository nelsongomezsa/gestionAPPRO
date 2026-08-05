package com.gestionap.utils;

import com.gestionap.model.Contrato;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Exporta contratos a un archivo XML usando escritura directa (sin dependencias externas).
 * El XML generado es compatible con cualquier parser estandar.
 */
public class XmlExporter {

    private static final DateTimeFormatter FORMATO_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Exporta la lista de contratos al archivo XML indicado.
     *
     * @param contratos Lista de contratos a exportar.
     * @param rutaArchivo Ruta del archivo de destino (se crea o sobreescribe).
     * @throws IOException Si hay un problema al escribir el archivo.
     */
    public void exportarContratos(List<Contrato> contratos, String rutaArchivo) throws IOException {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(rutaArchivo), StandardCharsets.UTF_8)) {

            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.write("<contratos exportado=\"" +
                         LocalDateTime.now().format(FORMATO_TIMESTAMP) + "\" " +
                         "total=\"" + contratos.size() + "\">\n");

            for (Contrato c : contratos) {
                writer.write("  <contrato id=\"" + c.getIdContrato() + "\">\n");

                writer.write("    <inquilino>\n");
                writer.write("      <nombre>" + escaparXml(c.getNombreInquilino()) + "</nombre>\n");
                writer.write("      <dni>"    + escaparXml(c.getDniInquilino())    + "</dni>\n");
                writer.write("    </inquilino>\n");

                writer.write("    <habitacion>\n");
                writer.write("      <numero>"   + c.getNumeroHabitacion()              + "</numero>\n");
                writer.write("      <direccion>" + escaparXml(c.getDireccionPiso())    + "</direccion>\n");
                writer.write("    </habitacion>\n");

                writer.write("    <fechaInicio>"   + c.getFechaInicio()   + "</fechaInicio>\n");
                writer.write("    <fechaFin>"      + c.getFechaFin()      + "</fechaFin>\n");
                writer.write("    <precioMensual>" + c.getPrecioMensual() + "</precioMensual>\n");
                writer.write("    <activo>"        + c.isActivo()         + "</activo>\n");

                writer.write("  </contrato>\n");
            }

            writer.write("</contratos>\n");
        }
    }

    /**
     * Escapa los caracteres especiales XML para evitar XML malformado.
     */
    private String escaparXml(String texto) {
        if (texto == null) return "";
        return texto
            .replace("&",  "&amp;")
            .replace("<",  "&lt;")
            .replace(">",  "&gt;")
            .replace("\"", "&quot;")
            .replace("'",  "&apos;");
    }
}
