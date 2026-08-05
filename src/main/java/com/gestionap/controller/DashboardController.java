package com.gestionap.controller;

import com.gestionap.dao.ActividadDAO;
import com.gestionap.dao.CalendarioDAO;
import com.gestionap.dao.ContratoDAO;
import com.gestionap.dao.DashboardDAO;
import com.gestionap.dao.IncidenciaDAO;
import com.gestionap.model.ActividadLog;
import com.gestionap.model.Contrato;
import com.gestionap.model.Incidencia;
import com.gestionap.model.Pago;
import com.gestionap.utils.Session;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.io.font.constants.StandardFonts;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    @FXML private Label             lblBienvenida;
    @FXML private Label             lblTotalHabitaciones;
    @FXML private Label             lblDisponibles;
    @FXML private Label             lblAlquiladas;
    @FXML private Label             lblMantenimiento;
    @FXML private Label             lblTotalInquilinos;
    @FXML private Label             lblContratosActivos;
    @FXML private Label             lblIngresos;
    @FXML private Label             lblTrendIngresos;
    @FXML private Label             lblIncidenciasPendientes;
    @FXML private Label             lblSaludSistema;
    @FXML private Label             lblSaludTexto;
    @FXML private Label             lblAlertasContratos;
    @FXML private HBox              panelAlertas;
    @FXML private BarChart<String, Number> chartIngresos;
    @FXML private VBox              vboxVencimientos;
    @FXML private VBox              vboxActividades;
    @FXML private Button            btnActualizar;
    @FXML private Button            btnInforme;
    @FXML private ProgressIndicator progressIndicator;

    private DashboardDAO  dashboardDAO;
    private ContratoDAO   contratoDAO;
    private ActividadDAO  actividadDAO;
    private CalendarioDAO calendarioDAO;
    private IncidenciaDAO incidenciaDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instances();
        initGUI();
        actions();
    }

    private void instances() {
        dashboardDAO  = new DashboardDAO();
        contratoDAO   = new ContratoDAO();
        actividadDAO  = new ActividadDAO();
        calendarioDAO = new CalendarioDAO();
        incidenciaDAO = new IncidenciaDAO();
    }

    private void initGUI() {
        var usuario = Session.getInstance().getUsuarioActual();
        if (usuario != null)
            lblBienvenida.setText("Bienvenido, " + usuario.getNombre()
                    + "  —  " + LocalDate.now());
        cargarTodo();
    }

    private void actions() {
        btnActualizar.setOnAction(e -> cargarTodo());
        btnInforme.setOnAction(e    -> generarInformePDF());
    }

    // ── Carga asíncrona de todos los datos ────────────────────────

    private void cargarTodo() {
        progressIndicator.setVisible(true);
        btnActualizar.setDisable(true);

        new Thread(() -> {
            try {
                int     totalHabs     = dashboardDAO.contarHabitaciones();
                int     disponibles   = dashboardDAO.contarHabitacionesPorEstado("disponible");
                int     alquiladas    = dashboardDAO.contarHabitacionesPorEstado("alquilada");
                int     mantenimiento = dashboardDAO.contarHabitacionesPorEstado("mantenimiento");
                int     inquilinos    = dashboardDAO.contarInquilinos();
                int     contratos     = dashboardDAO.contarContratosActivos();
                int     incPend       = dashboardDAO.contarIncidenciasPendientes();
                double  tasaOcup      = dashboardDAO.tasaOcupacion();
                Map<String, BigDecimal> ingresosMes = dashboardDAO.ingresosPorMes();
                List<Contrato>          activos      = contratoDAO.listarActivos();
                List<ActividadLog>      logs         = actividadDAO.listarUltimasGlobal(5);

                // Cobrado este mes desde Pagos (más real que contratos activos)
                BigDecimal cobradoMes = cobradomesActual(ingresosMes);

                Platform.runLater(() -> {
                    actualizarStats(totalHabs, disponibles, alquiladas, mantenimiento,
                            inquilinos, contratos, cobradoMes, incPend, tasaOcup);
                    actualizarTrendIngresos(ingresosMes);
                    actualizarGrafico(ingresosMes);
                    actualizarAlertas(activos);
                    actualizarVencimientos(activos);
                    actualizarActividades(logs);
                    progressIndicator.setVisible(false);
                    btnActualizar.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    btnActualizar.setDisable(false);
                    lblBienvenida.setText("Error al cargar datos: " + ex.getMessage());
                });
            }
        }).start();
    }

    // ── Actualización de secciones UI ────────────────────────────

    private void actualizarStats(int totalHabs, int disponibles, int alquiladas,
            int mantenimiento, int inquilinos, int contratos,
            BigDecimal cobradoMes, int incPend, double tasaOcup) {

        lblTotalHabitaciones.setText(String.valueOf(totalHabs));
        lblDisponibles.setText(String.valueOf(disponibles));
        lblAlquiladas.setText(String.valueOf(alquiladas));
        lblMantenimiento.setText(String.valueOf(mantenimiento));
        lblTotalInquilinos.setText(String.valueOf(inquilinos));
        lblContratosActivos.setText(String.valueOf(contratos));
        lblIngresos.setText(String.format("%,.0f €", cobradoMes));
        lblIncidenciasPendientes.setText(String.valueOf(incPend));

        String color, texto;
        if (tasaOcup >= 75)      { color = "#2ecc71"; texto = "Sistema saludable"; }
        else if (tasaOcup >= 40) { color = "#f39c12"; texto = "Ocupación media"; }
        else                     { color = "#e74c3c"; texto = "Baja ocupación"; }

        lblSaludSistema.setText(String.format("%.0f%%", tasaOcup));
        lblSaludSistema.setStyle(
                "-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        lblSaludTexto.setText(texto);
        lblSaludTexto.setStyle("-fx-text-fill: " + color +
                "; -fx-font-size: 11px; -fx-font-weight: bold;");
    }

    private void actualizarTrendIngresos(Map<String, BigDecimal> ingresosMes) {
        List<BigDecimal> valores = new ArrayList<>(ingresosMes.values());
        if (valores.size() < 2) {
            lblTrendIngresos.setText("");
            return;
        }
        BigDecimal actual   = valores.get(valores.size() - 1);
        BigDecimal anterior = valores.get(valores.size() - 2);
        if (anterior.compareTo(BigDecimal.ZERO) == 0) { lblTrendIngresos.setText(""); return; }

        double pct = actual.subtract(anterior)
                .divide(anterior, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

        if (pct > 0) {
            lblTrendIngresos.setText(String.format("▲ +%.1f%% vs mes anterior", pct));
            lblTrendIngresos.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 10px;");
        } else if (pct < 0) {
            lblTrendIngresos.setText(String.format("▼ %.1f%% vs mes anterior", pct));
            lblTrendIngresos.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 10px;");
        } else {
            lblTrendIngresos.setText("→ Sin variación");
            lblTrendIngresos.setStyle("-fx-text-fill: #64648a; -fx-font-size: 10px;");
        }
    }

    private void actualizarGrafico(Map<String, BigDecimal> datos) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cobrado");
        datos.forEach((mes, total) -> {
            XYChart.Data<String, Number> d = new XYChart.Data<>(mes, total);
            series.getData().add(d);
        });
        chartIngresos.getData().setAll(series);

        // Color de barras: último mes en acento, resto en azul
        Platform.runLater(() -> {
            List<XYChart.Data<String, Number>> dataList = series.getData();
            for (int i = 0; i < dataList.size(); i++) {
                var node = dataList.get(i).getNode();
                if (node != null) {
                    boolean isLast = (i == dataList.size() - 1);
                    node.setStyle(isLast
                            ? "-fx-bar-fill: #6C63FF;"
                            : "-fx-bar-fill: #2a4a7a;");
                }
            }
        });
    }

    private void actualizarAlertas(List<Contrato> activos) {
        LocalDate limite = LocalDate.now().plusDays(30);
        List<Contrato> proximos = activos.stream()
                .filter(c -> !c.getFechaFin().isAfter(limite))
                .collect(Collectors.toList());

        if (!proximos.isEmpty()) {
            String detalle = proximos.stream()
                    .map(c -> c.getNombreInquilino() + " (vence " + c.getFechaFin() + ")")
                    .collect(Collectors.joining("  •  "));
            int n = proximos.size();
            lblAlertasContratos.setText(n == 1
                    ? "1 contrato vence en menos de 30 días: " + detalle
                    : n + " contratos vencen próximamente: " + detalle);
            panelAlertas.setVisible(true);
            panelAlertas.setManaged(true);
        } else {
            panelAlertas.setVisible(false);
            panelAlertas.setManaged(false);
        }
    }

    private void actualizarVencimientos(List<Contrato> activos) {
        if (vboxVencimientos == null) return;
        LocalDate limite = LocalDate.now().plusDays(30);
        List<Contrato> proximos = activos.stream()
                .filter(c -> !c.getFechaFin().isAfter(limite))
                .collect(Collectors.toList());

        vboxVencimientos.getChildren().clear();

        if (proximos.isEmpty()) {
            Label ok = new Label("Sin vencimientos en los próximos 30 días ✓");
            ok.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 12px;");
            vboxVencimientos.getChildren().add(ok);
            return;
        }

        for (Contrato c : proximos) {
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), c.getFechaFin());
            HBox fila = new HBox(10);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("activity-item");

            Label icono = new Label("📋");
            icono.setStyle("-fx-font-size: 14px;");

            VBox detalle = new VBox(1);
            Label nombre = new Label(c.getNombreInquilino() + "  —  Hab. " + c.getNumeroHabitacion());
            nombre.setStyle("-fx-text-fill: #c8c8e8; -fx-font-size: 12px; -fx-font-weight: bold;");
            String colorDias = dias <= 7 ? "#e74c3c" : dias <= 15 ? "#f39c12" : "#4A90E2";
            Label info = new Label("Vence en " + dias + " día(s)  ·  " + c.getFechaFin()
                    + "  ·  " + String.format("%.0f €/mes", c.getPrecioMensual().doubleValue()));
            info.setStyle("-fx-text-fill: " + colorDias + "; -fx-font-size: 11px;");
            detalle.getChildren().addAll(nombre, info);

            fila.getChildren().addAll(icono, detalle);
            vboxVencimientos.getChildren().add(fila);
        }
    }

    private void actualizarActividades(List<ActividadLog> logs) {
        if (vboxActividades == null) return;
        vboxActividades.getChildren().clear();

        if (logs.isEmpty()) {
            Label vacio = new Label("Sin actividad registrada todavía.");
            vacio.getStyleClass().add("info-label");
            vboxActividades.getChildren().add(vacio);
            return;
        }

        for (ActividadLog log : logs) {
            HBox fila = new HBox(10);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.getStyleClass().add("activity-item");

            Label icono = new Label(log.getIcono());
            icono.setStyle("-fx-font-size: 14px;");

            VBox detalle = new VBox(1);
            Label desc  = new Label(log.getDescripcion() != null ? log.getDescripcion() : log.getAccion());
            desc.setStyle("-fx-text-fill: #c8c8e8; -fx-font-size: 12px;");
            Label fecha = new Label(log.getFechaFormateada());
            fecha.setStyle("-fx-text-fill: #44648a; -fx-font-size: 10px;");
            detalle.getChildren().addAll(desc, fecha);

            fila.getChildren().addAll(icono, detalle);
            vboxActividades.getChildren().add(fila);
        }
    }

    // ── Informe mensual PDF ──────────────────────────────────────

    private void generarInformePDF() {
        progressIndicator.setVisible(true);
        btnInforme.setDisable(true);

        YearMonth mes = YearMonth.now();
        new Thread(() -> {
            try {
                List<Contrato>   contratos   = calendarioDAO.listarContratosActivosMes(mes);
                List<Pago>       pagos       = calendarioDAO.listarPagosMes(mes);
                List<Incidencia> incidencias = incidenciaDAO.listarPendientes();
                int totalHabs   = dashboardDAO.contarHabitaciones();
                int alquiladas  = dashboardDAO.contarHabitacionesPorEstado("alquilada");
                double ocupacion = dashboardDAO.tasaOcupacion();

                String path = generarPdfEnDisco(mes, contratos, pagos, incidencias,
                                                totalHabs, alquiladas, ocupacion);

                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    btnInforme.setDisable(false);
                    Alert ok = new Alert(Alert.AlertType.INFORMATION);
                    ok.setTitle("Informe generado");
                    ok.setHeaderText("Informe mensual guardado en el Escritorio");
                    ok.setContentText(path);
                    ok.showAndWait();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    btnInforme.setDisable(false);
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Error");
                    err.setHeaderText("No se pudo generar el informe");
                    err.setContentText(ex.getMessage());
                    err.showAndWait();
                });
            }
        }).start();
    }

    private String generarPdfEnDisco(YearMonth mes, List<Contrato> contratos, List<Pago> pagos,
                                     List<Incidencia> incidencias, int totalHabs,
                                     int alquiladas, double ocupacion) throws Exception {
        String nombreMes = mes.format(DateTimeFormatter.ofPattern("MMMM_yyyy", new Locale("es", "ES")));
        String path = System.getProperty("user.home") + "/Desktop/Informe_" + nombreMes + ".pdf";

        DeviceRgb ACCENT  = new DeviceRgb(108, 99, 255);
        DeviceRgb DARK    = new DeviceRgb(15,  30,  60);
        DeviceRgb ROW_ALT = new DeviceRgb(235, 237, 248);

        PdfFont bold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(path));
        Document doc = new Document(pdfDoc, PageSize.A4);
        doc.setMargins(50, 50, 50, 50);

        String tituloMes = mes.format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES")));
        tituloMes = Character.toUpperCase(tituloMes.charAt(0)) + tituloMes.substring(1);

        // ── Portada ──────────────────────────────────────────────
        doc.add(new Paragraph("GestionAp")
                .setFont(bold).setFontSize(28).setFontColor(ACCENT)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(60));
        doc.add(new Paragraph("INFORME MENSUAL")
                .setFont(bold).setFontSize(18).setFontColor(DARK)
                .setTextAlignment(TextAlignment.CENTER));
        doc.add(new Paragraph(tituloMes)
                .setFont(normal).setFontSize(14).setFontColor(ACCENT)
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(8));
        doc.add(new Paragraph("Generado: " + LocalDate.now())
                .setFont(normal).setFontSize(9).setFontColor(new DeviceRgb(120, 120, 140))
                .setTextAlignment(TextAlignment.CENTER).setMarginBottom(40));

        // ── Resumen ejecutivo ─────────────────────────────────────
        BigDecimal esperado  = contratos.stream().map(Contrato::getPrecioMensual).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cobrado   = pagos.stream().map(Pago::getCantidad).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendiente = esperado.subtract(cobrado).max(BigDecimal.ZERO);

        doc.add(new Paragraph("RESUMEN EJECUTIVO")
                .setFont(bold).setFontSize(13).setFontColor(ACCENT).setMarginBottom(6));

        Table resumen = new Table(UnitValue.createPercentArray(new float[]{2, 1})).useAllAvailableWidth();
        String[][] kpis = {
            {"Habitaciones totales", String.valueOf(totalHabs)},
            {"Habitaciones alquiladas", String.valueOf(alquiladas)},
            {"Tasa de ocupación", String.format("%.1f%%", ocupacion)},
            {"Contratos activos en el mes", String.valueOf(contratos.size())},
            {"Ingresos esperados", String.format("%.2f €", esperado)},
            {"Ingresos cobrados", String.format("%.2f €", cobrado)},
            {"Pendiente de cobro", String.format("%.2f €", pendiente)},
            {"Incidencias pendientes", String.valueOf(incidencias.size())}
        };
        for (int i = 0; i < kpis.length; i++) {
            DeviceRgb bg = (i % 2 == 0) ? null : ROW_ALT;
            Cell cLabel = new Cell().add(new Paragraph(kpis[i][0]).setFont(normal).setFontSize(10)).setPadding(5);
            Cell cValue = new Cell().add(new Paragraph(kpis[i][1]).setFont(bold).setFontSize(10)).setPadding(5);
            if (bg != null) { cLabel.setBackgroundColor(bg); cValue.setBackgroundColor(bg); }
            resumen.addCell(cLabel);
            resumen.addCell(cValue);
        }
        doc.add(resumen);
        doc.add(new Paragraph(" ").setMarginBottom(14));

        // ── Tabla de contratos activos ────────────────────────────
        doc.add(new Paragraph("CONTRATOS ACTIVOS")
                .setFont(bold).setFontSize(13).setFontColor(ACCENT).setMarginBottom(6));
        if (contratos.isEmpty()) {
            doc.add(new Paragraph("Sin contratos activos en este mes.")
                    .setFont(normal).setFontSize(10).setMarginBottom(14));
        } else {
            Table tContratos = new Table(UnitValue.createPercentArray(new float[]{2.5f, 1, 2.5f, 1.2f, 1.5f}))
                    .useAllAvailableWidth();
            for (String h : new String[]{"INQUILINO", "HAB.", "DIRECCIÓN", "€/MES", "FIN CONTRATO"})
                tContratos.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontColor(ColorConstants.WHITE).setFontSize(9))
                        .setBackgroundColor(ACCENT).setPadding(5));
            for (int i = 0; i < contratos.size(); i++) {
                Contrato c = contratos.get(i);
                DeviceRgb bg = (i % 2 == 0) ? null : ROW_ALT;
                String[] vals = {c.getNombreInquilino(),
                        String.valueOf(c.getNumeroHabitacion()),
                        c.getDireccionPiso() != null ? c.getDireccionPiso() : "",
                        String.format("%.0f €", c.getPrecioMensual()),
                        c.getFechaFin().toString()};
                for (String v : vals) {
                    Cell cell = new Cell().add(new Paragraph(v).setFont(normal).setFontSize(9)).setPadding(4);
                    if (bg != null) cell.setBackgroundColor(bg);
                    tContratos.addCell(cell);
                }
            }
            doc.add(tContratos);
            doc.add(new Paragraph(" ").setMarginBottom(14));
        }

        // ── Tabla de pagos del mes ────────────────────────────────
        doc.add(new Paragraph("PAGOS REGISTRADOS EN " + tituloMes.toUpperCase())
                .setFont(bold).setFontSize(13).setFontColor(ACCENT).setMarginBottom(6));
        if (pagos.isEmpty()) {
            doc.add(new Paragraph("Sin pagos registrados en este mes.")
                    .setFont(normal).setFontSize(10).setMarginBottom(14));
        } else {
            Table tPagos = new Table(UnitValue.createPercentArray(new float[]{2.5f, 1.2f, 1.5f, 1.5f}))
                    .useAllAvailableWidth();
            for (String h : new String[]{"INQUILINO", "CANTIDAD", "MÉTODO", "FECHA"})
                tPagos.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontColor(ColorConstants.WHITE).setFontSize(9))
                        .setBackgroundColor(ACCENT).setPadding(5));
            for (int i = 0; i < pagos.size(); i++) {
                Pago p = pagos.get(i);
                DeviceRgb bg = (i % 2 == 0) ? null : ROW_ALT;
                String[] vals = {p.getNombreInquilino(),
                        String.format("%.2f €", p.getCantidad()),
                        p.getMetodoPago().name(),
                        p.getFechaPago().toString()};
                for (String v : vals) {
                    Cell cell = new Cell().add(new Paragraph(v).setFont(normal).setFontSize(9)).setPadding(4);
                    if (bg != null) cell.setBackgroundColor(bg);
                    tPagos.addCell(cell);
                }
            }
            doc.add(tPagos);
            doc.add(new Paragraph(" ").setMarginBottom(14));
        }

        // ── Incidencias pendientes ────────────────────────────────
        doc.add(new Paragraph("INCIDENCIAS PENDIENTES")
                .setFont(bold).setFontSize(13).setFontColor(ACCENT).setMarginBottom(6));
        if (incidencias.isEmpty()) {
            doc.add(new Paragraph("Sin incidencias pendientes. ✓")
                    .setFont(normal).setFontSize(10));
        } else {
            Table tInc = new Table(UnitValue.createPercentArray(new float[]{3f, 1f, 2f, 1.5f}))
                    .useAllAvailableWidth();
            for (String h : new String[]{"DESCRIPCIÓN", "HAB.", "INQUILINO", "FECHA"})
                tInc.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setFont(bold).setFontColor(ColorConstants.WHITE).setFontSize(9))
                        .setBackgroundColor(ACCENT).setPadding(5));
            for (int i = 0; i < incidencias.size(); i++) {
                Incidencia inc = incidencias.get(i);
                DeviceRgb bg = (i % 2 == 0) ? null : ROW_ALT;
                String[] vals = {inc.getDescripcion(),
                        String.valueOf(inc.getNumeroHabitacion()),
                        inc.getNombreInquilino(),
                        inc.getFecha().toString()};
                for (String v : vals) {
                    Cell cell = new Cell().add(new Paragraph(v != null ? v : "").setFont(normal).setFontSize(9)).setPadding(4);
                    if (bg != null) cell.setBackgroundColor(bg);
                    tInc.addCell(cell);
                }
            }
            doc.add(tInc);
        }

        doc.close();
        return path;
    }

    // ── Helpers ───────────────────────────────────────────────────

    private BigDecimal cobradomesActual(Map<String, BigDecimal> ingresosMes) {
        // El último mes del mapa es el más reciente
        if (ingresosMes.isEmpty()) return BigDecimal.ZERO;
        List<BigDecimal> vals = new ArrayList<>(ingresosMes.values());
        return vals.get(vals.size() - 1);
    }
}
