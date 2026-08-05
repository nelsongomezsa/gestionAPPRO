package com.gestionap.controller;

import com.gestionap.dao.AnalisisPisoDAO;
import com.gestionap.model.AnalisisPiso;
import com.gestionap.model.AnalisisPiso.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class DetallePisoController implements Initializable {

    @FXML private VBox   root;
    @FXML private Button btnVolver;
    @FXML private Label  lblTitulo;
    @FXML private Label  lblSubtitulo;
    @FXML private Button btnExportarPdf;
    @FXML private HBox   boxKpis;

    // Habitaciones
    @FXML private TableView<HabitacionDetalle>              tablaHabitaciones;
    @FXML private TableColumn<HabitacionDetalle, Integer>   colHabNum;
    @FXML private TableColumn<HabitacionDetalle, String>    colHabEstado;
    @FXML private TableColumn<HabitacionDetalle, String>    colHabPrecio;
    @FXML private TableColumn<HabitacionDetalle, String>    colHabInquilino;
    @FXML private TableColumn<HabitacionDetalle, String>    colHabFin;

    // Gráfico
    @FXML private BarChart<String, Number> chartIngresos;

    // Contratos
    @FXML private TableView<ContratoDetalle>               tablaContratos;
    @FXML private TableColumn<ContratoDetalle, Integer>    colCtHab;
    @FXML private TableColumn<ContratoDetalle, String>     colCtInquilino;
    @FXML private TableColumn<ContratoDetalle, String>     colCtInicio;
    @FXML private TableColumn<ContratoDetalle, String>     colCtFin;
    @FXML private TableColumn<ContratoDetalle, String>     colCtPrecio;
    @FXML private TableColumn<ContratoDetalle, String>     colCtActivo;

    // Pagos
    @FXML private TableView<PagoDetalle>                   tablaPagos;
    @FXML private TableColumn<PagoDetalle, String>         colPgFecha;
    @FXML private TableColumn<PagoDetalle, String>         colPgInquilino;
    @FXML private TableColumn<PagoDetalle, String>         colPgCantidad;
    @FXML private TableColumn<PagoDetalle, String>         colPgMetodo;

    // Incidencias
    @FXML private TableView<IncidenciaDetalle>             tablaIncidencias;
    @FXML private TableColumn<IncidenciaDetalle, Integer>  colIncHab;
    @FXML private TableColumn<IncidenciaDetalle, String>   colIncFecha;
    @FXML private TableColumn<IncidenciaDetalle, String>   colIncDesc;
    @FXML private TableColumn<IncidenciaDetalle, String>   colIncEstado;

    // Métricas avanzadas
    @FXML private VBox boxMetricas;

    private AnalisisPiso    pisoActual;
    private AnalisisPisoDAO dao;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dao = new AnalisisPisoDAO();
        initColumnas();
        btnVolver.setOnAction(e -> volver());
        btnExportarPdf.setOnAction(e -> exportarPdf());
    }

    public void setPiso(AnalisisPiso piso) {
        this.pisoActual = piso;
        lblTitulo.setText(piso.getDireccion());
        lblSubtitulo.setText(piso.getCiudad() + "   ·   " +
                piso.semaforoEmoji() + " " + piso.semaforoTexto() +
                "   ·   " + piso.getTotalHabitaciones() + " habitaciones");
        buildKpis();
        cargarDetalles();
    }

    // ── Columnas de tablas ────────────────────────────────────────

    private void initColumnas() {
        // Habitaciones
        colHabNum.setCellValueFactory(
                c -> new SimpleObjectProperty<>(c.getValue().numero));
        colHabEstado.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().estado));
        colHabEstado.setCellFactory(col -> estadoCelda());
        colHabPrecio.setCellValueFactory(
                c -> new SimpleStringProperty(formatEur(c.getValue().precio)));
        colHabInquilino.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().inquilinoActual));
        colHabFin.setCellValueFactory(c -> {
            LocalDate f = c.getValue().finContrato;
            return new SimpleStringProperty(f != null ? f.format(FMT) : "—");
        });
        tablaHabitaciones.setPlaceholder(new Label("Sin habitaciones."));

        // Contratos
        colCtHab.setCellValueFactory(
                c -> new SimpleObjectProperty<>(c.getValue().numeroHab));
        colCtInquilino.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().inquilino));
        colCtInicio.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().fechaInicio != null
                        ? c.getValue().fechaInicio.format(FMT) : "—"));
        colCtFin.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().fechaFin != null
                        ? c.getValue().fechaFin.format(FMT) : "—"));
        colCtPrecio.setCellValueFactory(
                c -> new SimpleStringProperty(formatEur(c.getValue().precioMensual)));
        colCtActivo.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().activo ? "activo" : "finalizado"));
        colCtActivo.setCellFactory(col -> estadoCeldaContrato());
        tablaContratos.setPlaceholder(new Label("Sin contratos."));

        // Pagos
        colPgFecha.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().fechaPago != null
                        ? c.getValue().fechaPago.format(FMT) : "—"));
        colPgInquilino.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().inquilino));
        colPgCantidad.setCellValueFactory(
                c -> new SimpleStringProperty(formatEur(c.getValue().cantidad)));
        colPgMetodo.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().metodoPago));
        tablaPagos.setPlaceholder(new Label("Sin pagos registrados."));

        // Incidencias
        colIncHab.setCellValueFactory(
                c -> new SimpleObjectProperty<>(c.getValue().numeroHab));
        colIncFecha.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().fecha != null
                        ? c.getValue().fecha.format(FMT) : "—"));
        colIncDesc.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().descripcion));
        colIncEstado.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().estado));
        tablaIncidencias.setPlaceholder(new Label("Sin incidencias."));
    }

    // ── KPI cards ────────────────────────────────────────────────

    private void buildKpis() {
        boxKpis.getChildren().clear();
        boxKpis.getChildren().addAll(
                kpiCard("💰", formatEur(pisoActual.getIngresosMes()),     "Ingresos / mes",       "#1abc9c"),
                kpiCard("💵", formatEur(pisoActual.getCashFlowMes()),     "Cash flow / mes",      "#6C63FF"),
                kpiCard("📈", String.format("%.1f%%", pisoActual.getRentBruta()), "Rent. bruta",  "#2ecc71"),
                kpiCard("📉", String.format("%.1f%%", pisoActual.getRentNeta()), "Rent. neta",    "#4a9eff"),
                kpiCard("🏠", String.format("%.0f%%", pisoActual.getTasaOcupacion()), "Ocupación","#9b59b6"),
                kpiCard("📄", String.valueOf(pisoActual.getContratosActivos()), "Contratos activos", "#f39c12")
        );
    }

    private Node kpiCard(String icono, String valor, String etiqueta, String color) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12, 18, 12, 18));
        card.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 10; " +
                "-fx-border-color: #1e3a6e; -fx-border-width: 1; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 2);");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lblIco  = new Label(icono);  lblIco.setStyle("-fx-font-size: 18px;");
        Label lblVal  = new Label(valor);  lblVal.setStyle("-fx-text-fill: " + color +
                "; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label lblEtiq = new Label(etiqueta); lblEtiq.setStyle("-fx-text-fill: #64648a; -fx-font-size: 10px;");

        card.getChildren().addAll(lblIco, lblVal, lblEtiq);
        return card;
    }

    // ── Carga de detalles en background ──────────────────────────

    private void cargarDetalles() {
        new Thread(() -> {
            try {
                int id = pisoActual.getIdPiso();
                List<HabitacionDetalle>  habs      = dao.habitacionesDelPiso(id);
                List<ContratoDetalle>    contratos = dao.contratosDelPiso(id);
                List<PagoDetalle>        pagos     = dao.pagosDelPiso(id);
                List<IncidenciaDetalle>  incs      = dao.incidenciasDelPiso(id);
                Map<String, BigDecimal>  meses     = dao.ingresosPorMes(id);

                Platform.runLater(() -> {
                    tablaHabitaciones.setItems(FXCollections.observableArrayList(habs));
                    tablaContratos.setItems(FXCollections.observableArrayList(contratos));
                    tablaPagos.setItems(FXCollections.observableArrayList(pagos));
                    tablaIncidencias.setItems(FXCollections.observableArrayList(incs));
                    buildChart(meses);
                    buildMetricas();
                });
            } catch (Exception ex) {
                Platform.runLater(() ->
                    lblSubtitulo.setText("Error al cargar datos: " + ex.getMessage()));
            }
        }).start();
    }

    // ── Gráfico de barras ─────────────────────────────────────────

    private void buildChart(Map<String, BigDecimal> meses) {
        chartIngresos.getData().clear();
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Ingresos");
        for (Map.Entry<String, BigDecimal> e : meses.entrySet()) {
            serie.getData().add(new XYChart.Data<>(e.getKey(),
                    e.getValue() != null ? e.getValue().doubleValue() : 0.0));
        }
        chartIngresos.getData().add(serie);
        chartIngresos.setStyle("-fx-background-color: transparent;");
    }

    // ── Métricas financieras avanzadas ────────────────────────────

    private void buildMetricas() {
        boxMetricas.getChildren().clear();

        Label titulo = new Label("Métricas financieras avanzadas");
        titulo.setStyle("-fx-text-fill: #e8e8f8; -fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-padding: 4 0 8 0;");
        boxMetricas.getChildren().add(titulo);

        GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(10);
        grid.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 10; " +
                "-fx-padding: 18 24 18 24; -fx-border-color: #1e3a6e; " +
                "-fx-border-width: 1; -fx-border-radius: 10;");

        String[][] metricas = {
            {"ROCE",               String.format("%.1f%%", pisoActual.getRoce())},
            {"PER inmobiliario",   String.format("%.1f años", pisoActual.getPer())},
            {"Punto equilibrio",   String.format("%.1f habs", pisoActual.getPuntoEquilibrio())},
            {"Eficiencia ingresos",String.format("%.1f%%", pisoActual.getEficienciaOcupacion())},
            {"Total cobrado",      formatEur(pisoActual.getTotalCobrado())},
            {"Gastos est./mes",    formatEur(pisoActual.getGastosMesEstimado())},
            {"Cash flow anual",    formatEur(pisoActual.getCashFlowAnual())},
            {"Coste incidencias",  formatEur(pisoActual.getCosteIncidencias())}
        };

        for (int i = 0; i < metricas.length; i++) {
            int col = (i / 4) * 2;
            int row = i % 4;

            Label lblNombre = new Label(metricas[i][0]);
            lblNombre.setStyle("-fx-text-fill: #64648a; -fx-font-size: 12px;");

            Label lblValor = new Label(metricas[i][1]);
            lblValor.setStyle("-fx-text-fill: #e8e8f8; -fx-font-size: 13px; -fx-font-weight: bold;");

            grid.add(lblNombre, col, row);
            grid.add(lblValor, col + 1, row);
        }

        ColumnConstraints cc1 = new ColumnConstraints(160);
        ColumnConstraints cc2 = new ColumnConstraints(140);
        ColumnConstraints cc3 = new ColumnConstraints(160);
        ColumnConstraints cc4 = new ColumnConstraints(140);
        grid.getColumnConstraints().addAll(cc1, cc2, cc3, cc4);

        boxMetricas.getChildren().add(grid);
    }

    // ── Navegación de vuelta ─────────────────────────────────────

    private void volver() {
        if (MainController.instance != null) {
            MainController.instance.volverAtras();
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/gestionap/analisis-pisos-view.fxml"));
                Node vista = loader.load();
                StackPane panelCentro = (StackPane) root.getParent();
                panelCentro.getChildren().setAll(vista);
            } catch (Exception ex) {
                mostrarError("No se pudo volver al análisis de pisos", ex.getMessage());
            }
        }
    }

    // ── Exportar PDF ─────────────────────────────────────────────

    private void exportarPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar informe PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Archivo PDF (*.pdf)", "*.pdf"));
        String nombreArchivo = "informe_" +
                pisoActual.getDireccion().replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚüÜñÑ ]", "")
                        .replaceAll("\\s+", "_") + ".pdf";
        chooser.setInitialFileName(nombreArchivo);
        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;

        try (PdfWriter writer = new PdfWriter(file);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document doc = new Document(pdfDoc)) {

            // Título
            doc.add(new Paragraph("Informe de Análisis de Piso")
                    .setFontSize(20).setBold()
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(4));
            doc.add(new Paragraph(pisoActual.getDireccion() + " — " + pisoActual.getCiudad())
                    .setFontSize(13).setTextAlignment(TextAlignment.CENTER).setMarginBottom(2));
            doc.add(new Paragraph("Generado: " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER).setMarginBottom(16)
                    .setFontColor(ColorConstants.GRAY));

            // Semáforo
            doc.add(new Paragraph("Estado: " + pisoActual.semaforoTexto())
                    .setFontSize(12).setBold().setMarginBottom(12));

            // KPIs principales
            doc.add(new Paragraph("Resumen financiero").setFontSize(13).setBold().setMarginBottom(6));
            float[] kpiWidths = {200f, 200f};
            Table kpiTable = new Table(UnitValue.createPointArray(kpiWidths));
            kpiTable.setWidth(UnitValue.createPercentValue(100));

            addKpiRow(kpiTable, "Ingresos/mes",       formatEur(pisoActual.getIngresosMes()));
            addKpiRow(kpiTable, "Cash flow/mes",       formatEur(pisoActual.getCashFlowMes()));
            addKpiRow(kpiTable, "Rentabilidad bruta",  String.format("%.1f%%", pisoActual.getRentBruta()));
            addKpiRow(kpiTable, "Rentabilidad neta",   String.format("%.1f%%", pisoActual.getRentNeta()));
            addKpiRow(kpiTable, "Tasa de ocupación",   String.format("%.0f%%", pisoActual.getTasaOcupacion()));
            addKpiRow(kpiTable, "Contratos activos",   String.valueOf(pisoActual.getContratosActivos()));
            addKpiRow(kpiTable, "ROCE",                String.format("%.1f%%", pisoActual.getRoce()));
            addKpiRow(kpiTable, "PER inmobiliario",    String.format("%.1f años", pisoActual.getPer()));
            addKpiRow(kpiTable, "Gastos est./mes",     formatEur(pisoActual.getGastosMesEstimado()));
            addKpiRow(kpiTable, "Cash flow anual",     formatEur(pisoActual.getCashFlowAnual()));
            addKpiRow(kpiTable, "Total cobrado",       formatEur(pisoActual.getTotalCobrado()));
            doc.add(kpiTable.setMarginBottom(16));

            // Tabla de habitaciones
            doc.add(new Paragraph("Habitaciones").setFontSize(13).setBold().setMarginBottom(6));
            List<HabitacionDetalle> habs = tablaHabitaciones.getItems();
            if (!habs.isEmpty()) {
                float[] habWidths = {60f, 100f, 100f, 170f, 110f};
                Table habTable = new Table(UnitValue.createPointArray(habWidths));
                habTable.setWidth(UnitValue.createPercentValue(100));
                addHeaderRow(habTable, "Nº", "Estado", "Precio/mes", "Inquilino", "Fin contrato");
                for (HabitacionDetalle h : habs) {
                    habTable.addCell(pdfCell(String.valueOf(h.numero)));
                    habTable.addCell(pdfCell(h.estado));
                    habTable.addCell(pdfCell(formatEur(h.precio)));
                    habTable.addCell(pdfCell(h.inquilinoActual));
                    habTable.addCell(pdfCell(h.finContrato != null ? h.finContrato.format(FMT) : "—"));
                }
                doc.add(habTable.setMarginBottom(16));
            }

            // Tabla de contratos
            doc.add(new Paragraph("Contratos").setFontSize(13).setBold().setMarginBottom(6));
            List<ContratoDetalle> contratos = tablaContratos.getItems();
            if (!contratos.isEmpty()) {
                float[] ctWidths = {60f, 180f, 100f, 100f, 100f};
                Table ctTable = new Table(UnitValue.createPointArray(ctWidths));
                ctTable.setWidth(UnitValue.createPercentValue(100));
                addHeaderRow(ctTable, "Hab.", "Inquilino", "Inicio", "Fin", "Precio/mes");
                for (ContratoDetalle c : contratos) {
                    ctTable.addCell(pdfCell(String.valueOf(c.numeroHab)));
                    ctTable.addCell(pdfCell(c.inquilino));
                    ctTable.addCell(pdfCell(c.fechaInicio != null ? c.fechaInicio.format(FMT) : "—"));
                    ctTable.addCell(pdfCell(c.fechaFin    != null ? c.fechaFin.format(FMT) : "—"));
                    ctTable.addCell(pdfCell(formatEur(c.precioMensual)));
                }
                doc.add(ctTable.setMarginBottom(16));
            }

            // Tabla de incidencias
            List<IncidenciaDetalle> incs = tablaIncidencias.getItems();
            if (!incs.isEmpty()) {
                doc.add(new Paragraph("Incidencias").setFontSize(13).setBold().setMarginBottom(6));
                float[] incWidths = {50f, 100f, 220f, 90f};
                Table incTable = new Table(UnitValue.createPointArray(incWidths));
                incTable.setWidth(UnitValue.createPercentValue(100));
                addHeaderRow(incTable, "Hab.", "Fecha", "Descripción", "Estado");
                for (IncidenciaDetalle inc : incs) {
                    incTable.addCell(pdfCell(String.valueOf(inc.numeroHab)));
                    incTable.addCell(pdfCell(inc.fecha != null ? inc.fecha.format(FMT) : "—"));
                    incTable.addCell(pdfCell(inc.descripcion != null ? inc.descripcion : "—"));
                    incTable.addCell(pdfCell(inc.estado != null ? inc.estado : "—"));
                }
                doc.add(incTable);
            }

            mostrarInfo("PDF generado correctamente", file.getName());
        } catch (Exception ex) {
            mostrarError("Error al generar PDF", ex.getMessage());
        }
    }

    private void addKpiRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setFontSize(10)
                .setFontColor(ColorConstants.GRAY)));
        table.addCell(new Cell().add(new Paragraph(value).setFontSize(10).setBold()));
    }

    private void addHeaderRow(Table table, String... headers) {
        for (String h : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setFontSize(9).setBold())
                    .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(15, 52, 96)));
        }
    }

    private Cell pdfCell(String text) {
        return new Cell().add(new Paragraph(text != null ? text : "—").setFontSize(9));
    }

    // ── Helpers de celda con badge ────────────────────────────────

    private <T> TableCell<T, String> estadoCelda() {
        return new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                Label b = new Label(v);
                String css = switch (v.toLowerCase()) {
                    case "alquilada"    -> "badge-activo";
                    case "disponible"   -> "badge-pendiente";
                    case "mantenimiento"-> "badge-mantenimiento";
                    default             -> "badge-finalizado";
                };
                b.getStyleClass().addAll("badge", css);
                setGraphic(b); setText(null);
            }
        };
    }

    private <T> TableCell<T, String> estadoCeldaContrato() {
        return new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                Label b = new Label(v);
                b.getStyleClass().addAll("badge",
                        "activo".equals(v) ? "badge-activo" : "badge-finalizado");
                setGraphic(b); setText(null);
            }
        };
    }

    // ── Helpers de formato ────────────────────────────────────────

    private String formatEur(BigDecimal bd) {
        if (bd == null) return "0,00 €";
        return String.format("%,.2f €", bd.doubleValue());
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(mensaje);
        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/gestionap/styles.css").toExternalForm());
        a.showAndWait();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error"); a.setHeaderText(titulo); a.setContentText(mensaje);
        a.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/gestionap/styles.css").toExternalForm());
        a.showAndWait();
    }
}
