package com.gestionap.controller;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class RentabilidadController implements Initializable {

    // ── FXML ────────────────────────────────────────────────────
    @FXML private TextField        tfDireccion;
    @FXML private ComboBox<String> cmbComunidad;
    @FXML private Label            lblItpPorcentaje;

    @FXML private TextField  tfPrecioCompra;
    @FXML private Label      lblItpImporte;
    @FXML private TextField  tfGastos;
    @FXML private TextField  tfGastosHipoteca;
    @FXML private TextField  tfReforma;
    @FXML private TextField  tfComision;
    @FXML private TextField  tfMobiliario;
    @FXML private Label      lblTotalCompra;

    @FXML private TextField  tfIbiMensual;
    @FXML private Label      lblIbiAnual;
    @FXML private TextField  tfSegurosMensual;
    @FXML private Label      lblSegurosAnual;
    @FXML private TextField  tfComunidadMensual;
    @FXML private Label      lblComunidadAnual;
    @FXML private TextField  tfMantenimientoMensual;
    @FXML private Label      lblMantenimientoAnual;
    @FXML private TextField  tfVacioMensual;
    @FXML private Label      lblVacioAnual;
    @FXML private Label      lblTotalGastosMensual;
    @FXML private Label      lblTotalGastosAnual;

    @FXML private TextField  tfAlquilerMensual;
    @FXML private Label      lblAlquilerAnual;

    @FXML private TextField  tfRevalorizacion;
    @FXML private Label      lblPlusvalia5;
    @FXML private Label      lblPlusvalia10;

    @FXML private TextField  tfPctFinanciado;
    @FXML private Label      lblHipoteca;
    @FXML private Label      lblCapitalAportado;
    @FXML private TextField  tfPlazo;
    @FXML private TextField  tfTipoInteres;
    @FXML private Label      lblCuotaHipotecaMensual;
    @FXML private Label      lblCuotaHipotecaAnual;
    @FXML private Label      lblInteresesMensual;
    @FXML private Label      lblInteresesAnual;
    @FXML private Label      lblAmortizacionMensual;
    @FXML private Label      lblAmortizacionAnual;

    @FXML private Label      lblRentabilidadBruta;
    @FXML private Label      lblRentabilidadNeta;
    @FXML private Label      lblCashFlowMensual;
    @FXML private Label      lblCashFlowAnual;
    @FXML private Label      lblAniosRecuperar;

    @FXML private Button     btnLimpiar;
    @FXML private Button     btnExportarPdf;

    // ── ITP por Comunidad Autónoma ───────────────────────────────
    private static final Map<String, Double> ITP_MAP = new LinkedHashMap<>();
    static {
        ITP_MAP.put("Andalucía",             7.0);
        ITP_MAP.put("Aragón",                8.0);
        ITP_MAP.put("Asturias",              8.0);
        ITP_MAP.put("Baleares",              8.0);
        ITP_MAP.put("Canarias",              6.5);
        ITP_MAP.put("Cantabria",            10.0);
        ITP_MAP.put("Castilla-La Mancha",    9.0);
        ITP_MAP.put("Castilla y León",       8.0);
        ITP_MAP.put("Cataluña",             10.0);
        ITP_MAP.put("Ceuta",                 6.0);
        ITP_MAP.put("Comunitat Valenciana", 10.0);
        ITP_MAP.put("Extremadura",           8.0);
        ITP_MAP.put("Galicia",              10.0);
        ITP_MAP.put("La Rioja",              7.0);
        ITP_MAP.put("Madrid",                6.0);
        ITP_MAP.put("Melilla",               6.0);
        ITP_MAP.put("Murcia",                8.0);
        ITP_MAP.put("Navarra",               6.0);
        ITP_MAP.put("País Vasco",            4.0);
    }

    // cached values used by PDF export
    private double _precioCompra, _itp, _totalCompra, _itpPct;
    private double _gastosMensual, _gastosAnual;
    private double _alquilerMensual, _alquilerAnual;
    private double _hipoteca, _capitalAportado;
    private double _cuotaMensual, _interesesMensual, _amortizacionMensual;
    private double _rentBruta, _rentNeta;
    private double _cashFlowMensual, _cashFlowAnual, _aniosRecuperar;
    private double _revalorizacion, _plusvalia5, _plusvalia10;
    private String _comunidad;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbComunidad.setItems(FXCollections.observableArrayList(ITP_MAP.keySet()));
        cmbComunidad.setValue("Madrid");

        cmbComunidad.valueProperty().addListener((obs, old, val) -> recalcular());

        TextField[] inputs = {
            tfPrecioCompra, tfGastos, tfGastosHipoteca, tfReforma, tfComision, tfMobiliario,
            tfIbiMensual, tfSegurosMensual, tfComunidadMensual, tfMantenimientoMensual, tfVacioMensual,
            tfAlquilerMensual, tfRevalorizacion, tfPctFinanciado, tfPlazo, tfTipoInteres
        };
        for (TextField tf : inputs) tf.textProperty().addListener((obs, old, v) -> recalcular());

        btnLimpiar.setOnAction(e -> limpiar());
        btnExportarPdf.setOnAction(e -> exportarPdf());

        recalcular();
    }

    private void recalcular() {
        _comunidad = cmbComunidad.getValue();
        _itpPct    = ITP_MAP.getOrDefault(_comunidad, 6.0);
        lblItpPorcentaje.setText(String.format("%.1f %%", _itpPct));

        _precioCompra = parse(tfPrecioCompra);
        double gastos  = parse(tfGastos);
        double gastosH = parse(tfGastosHipoteca);
        double reforma = parse(tfReforma);
        double comision = parse(tfComision);
        double mobil   = parse(tfMobiliario);

        _itp        = _precioCompra * _itpPct / 100.0;
        _totalCompra = _precioCompra + _itp + gastos + gastosH + reforma + comision + mobil;

        lblItpImporte.setText(fmt(_itp));
        lblTotalCompra.setText(fmt(_totalCompra));

        // Gastos mensuales/anuales
        double ibi  = parse(tfIbiMensual);
        double seg  = parse(tfSegurosMensual);
        double com  = parse(tfComunidadMensual);
        double mant = parse(tfMantenimientoMensual);
        double vac  = parse(tfVacioMensual);

        _gastosMensual = ibi + seg + com + mant + vac;
        _gastosAnual   = _gastosMensual * 12;

        lblIbiAnual.setText(fmt(ibi * 12));
        lblSegurosAnual.setText(fmt(seg * 12));
        lblComunidadAnual.setText(fmt(com * 12));
        lblMantenimientoAnual.setText(fmt(mant * 12));
        lblVacioAnual.setText(fmt(vac * 12));
        lblTotalGastosMensual.setText(fmt(_gastosMensual));
        lblTotalGastosAnual.setText(fmt(_gastosAnual));

        // Ingresos
        _alquilerMensual = parse(tfAlquilerMensual);
        _alquilerAnual   = _alquilerMensual * 12;
        lblAlquilerAnual.setText(fmt(_alquilerAnual));

        // Revalorización
        _revalorizacion = parse(tfRevalorizacion);
        if (_precioCompra > 0 && _revalorizacion > 0) {
            _plusvalia5  = _precioCompra * (Math.pow(1 + _revalorizacion / 100.0, 5)  - 1);
            _plusvalia10 = _precioCompra * (Math.pow(1 + _revalorizacion / 100.0, 10) - 1);
            lblPlusvalia5.setText(fmt(_plusvalia5));
            lblPlusvalia10.setText(fmt(_plusvalia10));
        } else {
            _plusvalia5 = 0; _plusvalia10 = 0;
            lblPlusvalia5.setText("—");
            lblPlusvalia10.setText("—");
        }

        // Financiación
        double pct   = parse(tfPctFinanciado);
        _hipoteca        = _precioCompra * (pct / 100.0);
        _capitalAportado = _totalCompra - _hipoteca;

        lblHipoteca.setText(fmt(_hipoteca));
        lblCapitalAportado.setText(fmt(_capitalAportado));

        // Cuota hipoteca — fórmula francesa: M = P * r(1+r)^n / ((1+r)^n - 1)
        double tipoInteres = parse(tfTipoInteres);
        int    plazo       = (int) parse(tfPlazo);
        double r = tipoInteres / 100.0 / 12.0;
        int    n = plazo * 12;

        if (_hipoteca > 0 && n > 0) {
            if (r == 0) {
                _cuotaMensual = _hipoteca / n;
            } else {
                double factor = Math.pow(1 + r, n);
                _cuotaMensual = _hipoteca * (r * factor) / (factor - 1);
            }
            double totalIntereses = _cuotaMensual * n - _hipoteca;
            _interesesMensual    = totalIntereses / n;
            _amortizacionMensual = _cuotaMensual - _interesesMensual;
        } else {
            _cuotaMensual = _interesesMensual = _amortizacionMensual = 0;
        }

        lblCuotaHipotecaMensual.setText(fmt(_cuotaMensual));
        lblCuotaHipotecaAnual.setText(fmt(_cuotaMensual * 12));
        lblInteresesMensual.setText(fmt(_interesesMensual));
        lblInteresesAnual.setText(fmt(_interesesMensual * 12));
        lblAmortizacionMensual.setText(fmt(_amortizacionMensual));
        lblAmortizacionAnual.setText(fmt(_amortizacionMensual * 12));

        // KPIs finales
        _rentBruta       = _totalCompra > 0 ? (_alquilerAnual / _totalCompra) * 100.0 : 0;
        _rentNeta        = _totalCompra > 0 ? ((_alquilerAnual - _gastosAnual) / _totalCompra) * 100.0 : 0;
        _cashFlowMensual = _alquilerMensual - _cuotaMensual - _gastosMensual;
        _cashFlowAnual   = _cashFlowMensual * 12;
        _aniosRecuperar  = (_cashFlowAnual > 0 && _capitalAportado > 0)
                           ? _capitalAportado / _cashFlowAnual : -1;

        lblRentabilidadBruta.setText(String.format("%.2f%%", _rentBruta));
        lblRentabilidadBruta.setStyle(kpiStyle(_rentBruta >= 6 ? "#2ecc71" : _rentBruta >= 3 ? "#f39c12" : "#e74c3c", 28));
        lblRentabilidadNeta.setText(String.format("%.2f%%", _rentNeta));
        lblRentabilidadNeta.setStyle(kpiStyle(_rentNeta >= 4 ? "#1abc9c" : _rentNeta >= 2 ? "#f39c12" : "#e74c3c", 28));

        lblCashFlowMensual.setText(fmt(_cashFlowMensual));
        lblCashFlowMensual.setStyle(kpiStyle(_cashFlowMensual >= 0 ? "#4A90E2" : "#e74c3c", 24));
        lblCashFlowAnual.setText(fmt(_cashFlowAnual));
        lblCashFlowAnual.setStyle(kpiStyle(_cashFlowAnual >= 0 ? "#6C63FF" : "#e74c3c", 24));

        if (_aniosRecuperar > 0) {
            String colorAnios = _aniosRecuperar <= 10 ? "#2ecc71" : _aniosRecuperar <= 20 ? "#f39c12" : "#e74c3c";
            lblAniosRecuperar.setText(String.format("%.1f años", _aniosRecuperar));
            lblAniosRecuperar.setStyle(kpiStyle(colorAnios, 32));
        } else {
            lblAniosRecuperar.setText("—");
            lblAniosRecuperar.setStyle(kpiStyle("#888888", 32));
        }
    }

    private String kpiStyle(String color, int size) {
        return "-fx-text-fill: " + color + "; -fx-font-size: " + size + "px; -fx-font-weight: bold;";
    }

    private void limpiar() {
        for (TextField tf : new TextField[]{
                tfDireccion, tfPrecioCompra, tfGastos, tfGastosHipoteca, tfReforma, tfComision, tfMobiliario,
                tfIbiMensual, tfSegurosMensual, tfComunidadMensual, tfMantenimientoMensual, tfVacioMensual,
                tfAlquilerMensual, tfRevalorizacion, tfPctFinanciado, tfPlazo, tfTipoInteres})
            tf.clear();
        cmbComunidad.setValue("Madrid");
    }

    // ── PDF Export ───────────────────────────────────────────────

    private void exportarPdf() {
        String fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String ruta  = Paths.get(System.getProperty("user.home"), "Desktop",
                "Analisis_Rentabilidad_" + fecha + ".pdf").toString();
        try {
            PdfDocument pdf = new PdfDocument(new PdfWriter(ruta));
            Document doc    = new Document(pdf);

            DeviceRgb purple = new DeviceRgb(108,  99, 255);
            DeviceRgb green  = new DeviceRgb( 46, 204, 113);
            DeviceRgb red    = new DeviceRgb(231,  76,  60);
            DeviceRgb orange = new DeviceRgb(243, 156,  18);
            DeviceRgb teal   = new DeviceRgb( 26, 188, 156);
            DeviceRgb blue   = new DeviceRgb( 52, 152, 219);
            DeviceRgb grey   = new DeviceRgb(100, 100, 130);

            doc.add(new Paragraph("ANÁLISIS DE RENTABILIDAD INMOBILIARIA")
                    .setFontSize(20).setBold().setFontColor(purple)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Generado el " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(9).setFontColor(grey).setTextAlignment(TextAlignment.CENTER));
            String dir = tfDireccion.getText().trim();
            if (!dir.isBlank())
                doc.add(new Paragraph("Dirección: " + dir).setFontSize(10).setFontColor(grey));
            doc.add(new Paragraph(" "));

            // COSTE COMPRA
            doc.add(seccion("COSTE DE COMPRA", purple));
            Table t = tabla2();
            fila(t, "Precio de compra",                  fmt(_precioCompra));
            fila(t, "ITP " + _comunidad + " (" + String.format("%.1f%%", _itpPct) + ")", fmt(_itp));
            fila(t, "Gastos notaría/registro",            fmt(parse(tfGastos)));
            fila(t, "Gastos hipoteca",                    fmt(parse(tfGastosHipoteca)));
            fila(t, "Reforma",                            fmt(parse(tfReforma)));
            fila(t, "Comisión compra",                    fmt(parse(tfComision)));
            fila(t, "Mobiliario",                         fmt(parse(tfMobiliario)));
            filaNegrita(t, "TOTAL COMPRA",                fmt(_totalCompra), purple);
            doc.add(t);
            doc.add(new Paragraph(" "));

            // GASTOS ANUALES
            doc.add(seccion("GASTOS ANUALES", red));
            Table t3 = tabla3("Concepto", "Mensual", "Anual");
            fila3(t3, "Impuestos (IBI, basuras...)", fmt(parse(tfIbiMensual)),          fmt(parse(tfIbiMensual) * 12));
            fila3(t3, "Seguros",                     fmt(parse(tfSegurosMensual)),       fmt(parse(tfSegurosMensual) * 12));
            fila3(t3, "Comunidad propietarios",      fmt(parse(tfComunidadMensual)),     fmt(parse(tfComunidadMensual) * 12));
            fila3(t3, "Mantenimiento",               fmt(parse(tfMantenimientoMensual)), fmt(parse(tfMantenimientoMensual) * 12));
            fila3(t3, "Períodos vacío",              fmt(parse(tfVacioMensual)),         fmt(parse(tfVacioMensual) * 12));
            filaNegrita3(t3, "TOTAL GASTOS",         fmt(_gastosMensual),                fmt(_gastosAnual), red);
            doc.add(t3);
            doc.add(new Paragraph(" "));

            // INGRESOS
            doc.add(seccion("INGRESOS", green));
            t = tabla2();
            fila(t, "Alquiler mensual", fmt(_alquilerMensual));
            filaNegrita(t, "Alquiler anual", fmt(_alquilerAnual), green);
            doc.add(t);
            doc.add(new Paragraph(" "));

            // FINANCIACIÓN
            doc.add(seccion("FINANCIACIÓN", orange));
            t = tabla2();
            fila(t, "Hipoteca (capital prestado)",      fmt(_hipoteca));
            filaNegrita(t, "Capital aportado",          fmt(_capitalAportado), orange);
            fila(t, "Plazo",                            (int) parse(tfPlazo) + " años");
            fila(t, "Tipo de interés anual",            String.format("%.2f%%", parse(tfTipoInteres)));
            fila(t, "Cuota mensual hipoteca",           fmt(_cuotaMensual));
            fila(t, "Intereses promedio mensuales",     fmt(_interesesMensual));
            fila(t, "Amortización promedio mensual",    fmt(_amortizacionMensual));
            doc.add(t);
            doc.add(new Paragraph(" "));

            // ANÁLISIS
            doc.add(seccion("ANÁLISIS DE RENTABILIDAD", purple));
            t = tabla2();
            filaNegrita(t, "Rentabilidad Bruta",  String.format("%.2f%%", _rentBruta), _rentBruta >= 6 ? green : _rentBruta >= 3 ? orange : red);
            filaNegrita(t, "Rentabilidad Neta",   String.format("%.2f%%", _rentNeta),  _rentNeta  >= 4 ? teal  : _rentNeta  >= 2 ? orange : red);
            filaNegrita(t, "Cash Flow Mensual",   fmt(_cashFlowMensual),               _cashFlowMensual >= 0 ? blue : red);
            filaNegrita(t, "Cash Flow Anual",     fmt(_cashFlowAnual),                 _cashFlowAnual   >= 0 ? blue : red);
            filaNegrita(t, "Años para recuperar", _aniosRecuperar > 0 ? String.format("%.1f años", _aniosRecuperar) : "—", orange);
            doc.add(t);

            if (_revalorizacion > 0) {
                doc.add(new Paragraph(" "));
                doc.add(seccion("REVALORIZACIÓN ESPERADA", blue));
                t = tabla2();
                fila(t, "Revalorización anual",          String.format("%.2f%%", _revalorizacion));
                fila(t, "Plusvalía estimada (5 años)",   fmt(_plusvalia5));
                fila(t, "Plusvalía estimada (10 años)",  fmt(_plusvalia10));
                doc.add(t);
            }

            doc.close();
            mostrarInfo("PDF exportado correctamente", "Guardado en:\n" + ruta);

        } catch (Exception ex) {
            mostrarError("No se pudo generar el PDF", ex.getMessage());
        }
    }

    // ── PDF builders ─────────────────────────────────────────────

    private Paragraph seccion(String titulo, DeviceRgb color) {
        return new Paragraph(titulo).setFontSize(12).setBold()
                .setFontColor(color).setMarginTop(4).setMarginBottom(2);
    }

    private Table tabla2() {
        return new Table(UnitValue.createPercentArray(new float[]{55, 45}))
                .useAllAvailableWidth();
    }

    private Table tabla3(String h1, String h2, String h3) {
        DeviceRgb headerBg = new DeviceRgb(22, 33, 62);
        DeviceRgb white    = new DeviceRgb(255, 255, 255);
        Table t = new Table(UnitValue.createPercentArray(new float[]{50, 25, 25}))
                .useAllAvailableWidth();
        for (String h : new String[]{h1, h2, h3})
            t.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontColor(white))
                    .setBackgroundColor(headerBg).setPadding(4));
        return t;
    }

    private void fila(Table t, String label, String value) {
        DeviceRgb txt = new DeviceRgb(40, 40, 60);
        t.addCell(new Cell().add(new Paragraph(label).setFontColor(txt).setFontSize(10)).setPadding(4));
        t.addCell(new Cell().add(new Paragraph(value).setFontColor(txt).setFontSize(10)).setPadding(4));
    }

    private void filaNegrita(Table t, String label, String value, DeviceRgb color) {
        DeviceRgb txt = new DeviceRgb(40, 40, 60);
        t.addCell(new Cell().add(new Paragraph(label).setBold().setFontColor(txt).setFontSize(10)).setPadding(4));
        t.addCell(new Cell().add(new Paragraph(value).setBold().setFontColor(color).setFontSize(11)).setPadding(4));
    }

    private void fila3(Table t, String label, String v1, String v2) {
        DeviceRgb txt = new DeviceRgb(40, 40, 60);
        t.addCell(new Cell().add(new Paragraph(label).setFontColor(txt).setFontSize(10)).setPadding(4));
        t.addCell(new Cell().add(new Paragraph(v1).setFontColor(txt).setFontSize(10)).setPadding(4));
        t.addCell(new Cell().add(new Paragraph(v2).setFontColor(txt).setFontSize(10)).setPadding(4));
    }

    private void filaNegrita3(Table t, String label, String v1, String v2, DeviceRgb color) {
        DeviceRgb txt = new DeviceRgb(40, 40, 60);
        t.addCell(new Cell().add(new Paragraph(label).setBold().setFontColor(txt).setFontSize(10)).setPadding(4));
        t.addCell(new Cell().add(new Paragraph(v1).setBold().setFontColor(color).setFontSize(10)).setPadding(4));
        t.addCell(new Cell().add(new Paragraph(v2).setBold().setFontColor(color).setFontSize(10)).setPadding(4));
    }

    // ── Utilities ────────────────────────────────────────────────

    private double parse(TextField tf) {
        if (tf == null || tf.getText() == null) return 0;
        try { return Double.parseDouble(tf.getText().trim().replace(",", ".")); }
        catch (NumberFormatException e) { return 0; }
    }

    private String fmt(double v) { return String.format("%.2f €", v); }

    private void mostrarInfo(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Rentabilidad"); a.setHeaderText(header); a.setContentText(msg);
        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/gestionap/styles.css").toExternalForm());
        a.showAndWait();
    }

    private void mostrarError(String header, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(header); a.setContentText(msg);
        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/gestionap/styles.css").toExternalForm());
        a.showAndWait();
    }
}
