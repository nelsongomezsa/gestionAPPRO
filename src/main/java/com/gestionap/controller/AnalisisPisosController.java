package com.gestionap.controller;

import com.gestionap.dao.AnalisisPisoDAO;
import com.gestionap.dao.DatosPisoDAO;
import com.gestionap.model.AnalisisPiso;
import com.gestionap.model.DatosPiso;
import com.gestionap.service.CalculadoraFinancieraPiso;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AnalisisPisosController implements Initializable {

    @FXML private VBox              root;
    @FXML private HBox              boxResumenCartera;
    @FXML private Label             lblUltimaActualizacion;
    @FXML private Button            btnActualizar;
    @FXML private VBox              vboxPisos;
    @FXML private ProgressIndicator progressIndicator;

    private AnalisisPisoDAO dao;
    private DatosPisoDAO    datosPisoDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instances();
        initGUI();
        actions();
    }

    private void instances() {
        dao          = new AnalisisPisoDAO();
        datosPisoDAO = new DatosPisoDAO();
    }

    private void initGUI() {
        vboxPisos.setFillWidth(true);
        vboxPisos.setMaxWidth(Double.MAX_VALUE);
        cargarDatos();
    }

    private void actions() {
        btnActualizar.setOnAction(e -> cargarDatos());
    }

    // ── Carga de datos ────────────────────────────────────────────

    private void cargarDatos() {
        lblUltimaActualizacion.setText("— cargando —");
        progressIndicator.setVisible(true);
        btnActualizar.setDisable(true);
        vboxPisos.getChildren().clear();
        boxResumenCartera.getChildren().clear();

        new Thread(() -> {
            try {
                Map<String, Object> resumen = dao.resumenCartera();
                List<AnalisisPiso>  pisos   = dao.listarTodos();
                Platform.runLater(() -> {
                    buildResumenCartera(resumen);
                    buildPisoCards(pisos);
                    lblUltimaActualizacion.setText("Actualizado: " +
                            LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    progressIndicator.setVisible(false);
                    btnActualizar.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    lblUltimaActualizacion.setText("Error: " + ex.getMessage());
                    progressIndicator.setVisible(false);
                    btnActualizar.setDisable(false);
                });
            }
        }).start();
    }

    // ── Tarjetas de resumen de cartera ────────────────────────────

    private void buildResumenCartera(Map<String, Object> data) {
        boxResumenCartera.getChildren().clear();

        int    pisos    = toInt(data.get("totalPisos"));
        int    habs     = toInt(data.get("totalHabs"));
        int    alq      = toInt(data.get("alquiladas"));
        int    disp     = toInt(data.get("disponibles"));
        double ocup     = toDouble(data.get("ocupacionMedia"));
        Object ingMes   = data.get("ingresosMes");
        String ingTexto = ingMes != null
                ? String.format("%,.0f €", Double.parseDouble(ingMes.toString())) : "—";

        boxResumenCartera.getChildren().addAll(
                kpiCard("🏠", String.valueOf(pisos),          "Pisos",        "#6C63FF"),
                kpiCard("🚪", String.valueOf(habs),           "Habitaciones", "#4a9eff"),
                kpiCard("✅", String.valueOf(alq),            "Alquiladas",   "#2ecc71"),
                kpiCard("⬜", String.valueOf(disp),           "Disponibles",  "#f39c12"),
                kpiCard("📊", String.format("%.0f%%", ocup), "Ocupación",    "#9b59b6"),
                kpiCard("💰", ingTexto,                       "Ingresos/mes", "#1abc9c")
        );
    }

    private Node kpiCard(String icono, String valor, String etiqueta, String color) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(14, 20, 14, 20));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
                "-fx-background-color: #0f3460; -fx-background-radius: 10; " +
                "-fx-border-color: #1e3a6e; -fx-border-width: 1; -fx-border-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 6, 0, 0, 2);");
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lblIcono = new Label(icono);
        lblIcono.setStyle("-fx-font-size: 20px;");
        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label lblEtiq = new Label(etiqueta);
        lblEtiq.setStyle("-fx-text-fill: #64648a; -fx-font-size: 11px;");

        card.getChildren().addAll(lblIcono, lblValor, lblEtiq);
        return card;
    }

    // ── Tarjetas de piso ──────────────────────────────────────────

    private void buildPisoCards(List<AnalisisPiso> pisos) {
        vboxPisos.getChildren().clear();
        if (pisos.isEmpty()) {
            Label vacio = new Label("No hay pisos registrados.");
            vacio.setStyle("-fx-text-fill: #64648a; -fx-font-size: 14px; -fx-padding: 30;");
            vboxPisos.getChildren().add(vacio);
            return;
        }
        for (AnalisisPiso p : pisos) {
            vboxPisos.getChildren().add(buildPisoCard(p));
        }
    }

    private Node buildPisoCard(AnalisisPiso p) {
        HBox card = new HBox(0);
        card.getStyleClass().add("piso-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMinWidth(420);
        card.setStyle(
                "-fx-background-color: #0f3460; -fx-background-radius: 12; " +
                "-fx-border-color: #1e3a6e; -fx-border-width: 1; -fx-border-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 8, 0, 0, 2);");

        // Franja semáforo izquierda
        Rectangle strip = new Rectangle(6, 120);
        String color = p.semaforoColor();
        strip.setStyle("-fx-fill: " + color + ";");
        StackPane stripPane = new StackPane(strip);
        stripPane.setMinWidth(10);
        stripPane.setStyle("-fx-background-radius: 12 0 0 12;");

        // Contenido
        VBox content = new VBox(10);
        content.setPadding(new Insets(14, 18, 14, 16));
        content.setMaxWidth(Double.MAX_VALUE);
        // setPrefWidth(MAX) NOT set — would put HBox in shrink mode; hgrow=ALWAYS is enough
        HBox.setHgrow(content, Priority.ALWAYS);

        // Fila cabecera
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        headerRow.setMaxWidth(Double.MAX_VALUE);

        Label lblDir = new Label(p.getDireccion());
        lblDir.setStyle("-fx-text-fill: #e8e8f8; -fx-font-size: 15px; -fx-font-weight: bold;");
        lblDir.setWrapText(true);
        lblDir.setMaxWidth(Double.MAX_VALUE);
        lblDir.setMinWidth(0);
        HBox.setHgrow(lblDir, Priority.ALWAYS);

        Label lblCiudad = new Label("  " + p.getCiudad());
        lblCiudad.setStyle("-fx-text-fill: #64648a; -fx-font-size: 12px;");
        lblCiudad.setMinWidth(Region.USE_PREF_SIZE);

        Label lblSemaforo = new Label(p.semaforoEmoji() + "  " + p.semaforoTexto());
        lblSemaforo.setMinWidth(Region.USE_PREF_SIZE);
        lblSemaforo.setStyle(
                "-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-weight: bold; " +
                "-fx-padding: 3 8 3 8; -fx-background-color: " + hexToRgba(color, "0.15") + "; " +
                "-fx-background-radius: 6;");

        headerRow.getChildren().addAll(lblDir, lblCiudad, lblSemaforo);

        // Fila KPI — FlowPane envuelve a la línea siguiente si no caben, nunca trunca
        FlowPane kpiRow = new FlowPane(20, 6);
        kpiRow.setAlignment(Pos.CENTER_LEFT);
        kpiRow.setMaxWidth(Double.MAX_VALUE);
        kpiRow.getChildren().addAll(
                kpiMini("🚪", p.getTotalHabitaciones() + " hab"),
                kpiMini("✅", p.getAlquiladas() + " alq. / " + p.getDisponibles() + " disp."),
                kpiMini("💰", String.format("%,.0f €/mes", p.getIngresosMes().doubleValue())),
                kpiMini("📈", String.format("%.1f%% bruta", p.getRentBruta())),
                kpiMini("💵", String.format("%,.0f € CF/mes", p.getCashFlowMes().doubleValue()))
        );

        // Barra de ocupación
        VBox barraBox = new VBox(4);
        barraBox.setMaxWidth(Double.MAX_VALUE);
        double tasa = p.getTasaOcupacion() / 100.0;
        ProgressBar barra = new ProgressBar(Math.max(0, Math.min(1, tasa)));
        barra.setPrefWidth(Double.MAX_VALUE);
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.setPrefHeight(8);
        barra.setStyle("-fx-accent: " + color + "; -fx-background-color: #1e3050; -fx-background-radius: 4;");
        Label barraLbl = new Label(String.format("%.0f%% ocupado", p.getTasaOcupacion()));
        barraLbl.setStyle("-fx-text-fill: #64648a; -fx-font-size: 10px;");
        barraBox.getChildren().addAll(barra, barraLbl);

        // Fila inferior: alertas + botones
        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);
        bottomRow.setMaxWidth(Double.MAX_VALUE);

        VBox alertas = new VBox(2);
        alertas.setMaxWidth(Double.MAX_VALUE);  // must expand so labels get real width
        HBox.setHgrow(alertas, Priority.ALWAYS);
        if (!p.getProximosVencimientos().isEmpty()) {
            Label alerta = new Label("⚠  Vencimientos: " + String.join(", ", p.getProximosVencimientos()));
            alerta.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 11px;");
            alerta.setWrapText(true);
            alerta.setMaxWidth(Double.MAX_VALUE);  // fill VBox width so wrapText uses full width
            alertas.getChildren().add(alerta);
        }
        if (p.getTotalIncidencias() > 0) {
            Label incLbl = new Label("🔧  " + p.getTotalIncidencias() + " incidencia(s)");
            incLbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
            incLbl.setMinWidth(Region.USE_PREF_SIZE);
            alertas.getChildren().add(incLbl);
        }

        Button btnFinanciero = new Button("💰  Datos financieros");
        btnFinanciero.getStyleClass().add("btn-secondary");
        btnFinanciero.setMinWidth(Region.USE_PREF_SIZE);
        btnFinanciero.setOnAction(e -> abrirDialogoDatosFinancieros(p));

        Button btnDetalle = new Button("Ver detalle  →");
        btnDetalle.getStyleClass().add("btn-primary");
        btnDetalle.setMinWidth(Region.USE_PREF_SIZE);
        btnDetalle.setOnAction(e -> abrirDetalle(p));

        bottomRow.getChildren().addAll(alertas, btnFinanciero, btnDetalle);
        content.getChildren().addAll(headerRow, kpiRow, barraBox, bottomRow);
        card.getChildren().addAll(stripPane, content);
        return card;
    }

    private Node kpiMini(String icono, String texto) {
        HBox box = new HBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMinWidth(Region.USE_PREF_SIZE); // never shrink below natural width
        Label li = new Label(icono); li.setStyle("-fx-font-size: 13px;");
        Label lt = new Label(texto);
        lt.setStyle("-fx-text-fill: #c8c8e8; -fx-font-size: 12px;");
        lt.setMinWidth(Region.USE_PREF_SIZE);
        box.getChildren().addAll(li, lt);
        return box;
    }

    private Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    // ── Diálogo de datos financieros ──────────────────────────────

    private void abrirDialogoDatosFinancieros(AnalisisPiso piso) {
        DatosPiso datos;
        try {
            datos = datosPisoDAO.obtenerPorPiso(piso.getIdPiso());
        } catch (Exception ex) {
            mostrarError("Error al cargar datos financieros",
                    "No se pudieron cargar los datos guardados: " + ex.getMessage());
            datos = new DatosPiso(piso.getIdPiso());
        }
        final DatosPiso datosCargados = datos;

        Dialog<DatosPiso> dialog = new Dialog<>();
        dialog.setTitle("Datos Financieros — " + piso.getDireccion());
        dialog.setHeaderText(null);
        dialog.setResizable(true);
        aplicarCSS(dialog.getDialogPane());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        dialog.getDialogPane().setPrefSize(740, 680);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setMinWidth(550);

        // ── Campos de adquisición ──
        TextField tfPrecio     = tf(datosCargados.getPrecioCompra());
        TextField tfItp        = tf(datosCargados.getItpPagado());
        TextField tfNotaria    = tf(datosCargados.getGastosNotaria());
        TextField tfRegistro   = tf(datosCargados.getGastosRegistro());
        TextField tfReforma    = tf(datosCargados.getCosteReforma());
        TextField tfMobiliario = tf(datosCargados.getCosteMobiliario());
        TextField tfOtros      = tf(datosCargados.getOtrosGastosCompra());
        Label lblTotalAdq      = resultLabel("—");

        // ── Campos de financiación ──
        CheckBox cbHipoteca   = new CheckBox("Tiene hipoteca");
        cbHipoteca.setSelected(datosCargados.isTieneHipoteca());
        cbHipoteca.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 12px;");
        TextField tfImporteHip = tf(datosCargados.getImporteHipoteca());
        TextField tfTipoInt    = tf(datosCargados.getTipoInteres());
        TextField tfPlazo      = new TextField(datosCargados.getPlazoAnos() > 0
                ? String.valueOf(datosCargados.getPlazoAnos()) : "");
        tfPlazo.setPromptText("ej: 20");
        tfPlazo.setStyle(TF_STYLE);
        Label lblCuota         = resultLabel("—");

        // ── Campos de gastos ──
        TextField tfIbi          = tf(datosCargados.getGastoIbiAnual());
        TextField tfComunidad    = tf(datosCargados.getGastoComunidadMensual());
        TextField tfSeguro       = tf(datosCargados.getGastoSeguroAnual());
        TextField tfMantenimiento = tf(datosCargados.getGastoMantenimientoAnual());
        Label lblTotalGastos     = resultLabel("—");

        // ── Campos de valor actual ──
        TextField tfValorMercado = tf(datosCargados.getValorMercadoActual());
        DatePicker dpFechaCompra = new DatePicker(datosCargados.getFechaCompra());
        TextArea   taNotas       = new TextArea(datosCargados.getNotas() != null ? datosCargados.getNotas() : "");
        taNotas.setPrefRowCount(3);
        taNotas.setWrapText(true);
        taNotas.setStyle(
            "-fx-background-color: #1a2744; -fx-text-fill: white; " +
            "-fx-prompt-text-fill: #6688aa; -fx-border-color: #2a4a7a; " +
            "-fx-border-radius: 4; -fx-background-radius: 4; -fx-font-size: 12px;");

        // ── Panel de resultados ──
        Label lblInvTotal   = resultLabel("—");
        Label lblRentBruta  = resultLabel("—");
        Label lblRentNeta   = resultLabel("—");
        Label lblCFMes      = resultLabel("—");
        Label lblROCE       = resultLabel("—");
        Label lblCapPropio  = resultLabel("—");
        Label lblPlusvalia  = resultLabel("—");
        Label lblPER        = resultLabel("—");
        Label lblSemaforo   = new Label("—");
        lblSemaforo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        // ── Lógica de recalculo — delega el cálculo a CalculadoraFinancieraPiso,
        // el controller solo lee los campos, arma la Entrada y pinta el Resultado.
        Runnable recalcular = () -> {
            var adquisicion = new CalculadoraFinancieraPiso.DatosAdquisicion(
                    CalculadoraFinancieraPiso.parse(tfPrecio.getText()),
                    CalculadoraFinancieraPiso.parse(tfItp.getText()),
                    CalculadoraFinancieraPiso.parse(tfNotaria.getText()),
                    CalculadoraFinancieraPiso.parse(tfRegistro.getText()),
                    CalculadoraFinancieraPiso.parse(tfReforma.getText()),
                    CalculadoraFinancieraPiso.parse(tfMobiliario.getText()),
                    CalculadoraFinancieraPiso.parse(tfOtros.getText()));

            var hipoteca = new CalculadoraFinancieraPiso.DatosHipoteca(
                    cbHipoteca.isSelected(),
                    CalculadoraFinancieraPiso.parse(tfImporteHip.getText()),
                    CalculadoraFinancieraPiso.parse(tfTipoInt.getText()),
                    CalculadoraFinancieraPiso.parseInt(tfPlazo.getText()));

            var gastos = new CalculadoraFinancieraPiso.DatosGastos(
                    CalculadoraFinancieraPiso.parse(tfIbi.getText()),
                    CalculadoraFinancieraPiso.parse(tfComunidad.getText()),
                    CalculadoraFinancieraPiso.parse(tfSeguro.getText()),
                    CalculadoraFinancieraPiso.parse(tfMantenimiento.getText()));

            var entrada = new CalculadoraFinancieraPiso.Entrada(
                    adquisicion, hipoteca, gastos,
                    CalculadoraFinancieraPiso.parse(tfValorMercado.getText()),
                    piso.getIngresosMes());

            CalculadoraFinancieraPiso.Resultado r = CalculadoraFinancieraPiso.calcular(entrada);

            lblTotalAdq.setText(String.format("%,.2f €", r.inversionTotal()));

            if (cbHipoteca.isSelected()) {
                lblCuota.setText(String.format("%,.2f €/mes", r.cuotaMensualHipoteca()));
            } else {
                lblCuota.setText("—");
            }

            lblTotalGastos.setText(String.format("%,.2f €/año", r.totalGastosAnuales()));

            lblInvTotal.setText(String.format("%,.2f €", r.inversionTotal()));

            // Si inversionTotal <= 0, r.rentabilidadBrutaPct()/calificacion() vienen
            // null (no calculables) y, como antes de la extracción, los labels NO
            // se tocan — se quedan con el último valor mostrado.
            if (r.rentabilidadBrutaPct() != null) {
                lblRentBruta.setText(String.format("%.2f%%", r.rentabilidadBrutaPct()));
                lblRentNeta.setText(String.format("%.2f%%", r.rentabilidadNetaPct()));

                switch (r.calificacion()) {
                    case EXCELENTE -> {
                        lblSemaforo.setText("🟢  EXCELENTE (>7%)");
                        lblSemaforo.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 13px; -fx-font-weight: bold;");
                    }
                    case ACEPTABLE -> {
                        lblSemaforo.setText("🟡  ACEPTABLE (5-7%)");
                        lblSemaforo.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 13px; -fx-font-weight: bold;");
                    }
                    case A_REVISAR -> {
                        lblSemaforo.setText("🔴  A REVISAR (<5%)");
                        lblSemaforo.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px; -fx-font-weight: bold;");
                    }
                }
            }

            lblCFMes.setText(String.format("%,.2f €/mes", r.cashFlowMensual()));

            if (r.rocePct() != null) {
                lblROCE.setText(String.format("%.2f%%", r.rocePct()));
            }
            lblCapPropio.setText(String.format("%,.2f €", r.capitalPropio()));

            lblPlusvalia.setText(String.format("%,.2f €", r.plusvaliaLatente()));

            if (r.perAnios() != null) {
                lblPER.setText(String.format("%.1f años", r.perAnios()));
            }
        };

        // Atar listeners a todos los campos
        for (TextField tf : new TextField[]{tfPrecio, tfItp, tfNotaria, tfRegistro,
                tfReforma, tfMobiliario, tfOtros, tfImporteHip, tfTipoInt,
                tfPlazo, tfIbi, tfComunidad, tfSeguro, tfMantenimiento, tfValorMercado}) {
            tf.textProperty().addListener((o, ov, nv) -> recalcular.run());
        }
        cbHipoteca.selectedProperty().addListener((o, ov, nv) -> {
            boolean on = nv;
            tfImporteHip.setDisable(!on);
            tfTipoInt.setDisable(!on);
            tfPlazo.setDisable(!on);
            recalcular.run();
        });
        // Estado inicial hipoteca
        boolean hip = datosCargados.isTieneHipoteca();
        tfImporteHip.setDisable(!hip);
        tfTipoInt.setDisable(!hip);
        tfPlazo.setDisable(!hip);

        // ── Construcción de la UI del diálogo ──
        VBox mainBox = new VBox(18);
        mainBox.setPadding(new Insets(4, 8, 4, 8));

        mainBox.getChildren().addAll(
            seccion("💰  COSTE DE ADQUISICIÓN", formGrid(new Object[][]{
                {"Precio de compra (€):",    tfPrecio},
                {"ITP pagado (€):",          tfItp},
                {"Gastos notaría (€):",      tfNotaria},
                {"Gastos registro (€):",     tfRegistro},
                {"Coste reforma (€):",       tfReforma},
                {"Coste mobiliario (€):",    tfMobiliario},
                {"Otros gastos (€):",        tfOtros},
                {"→ Inversión total:",        lblTotalAdq}
            })),
            seccion("🏦  FINANCIACIÓN", formGrid(new Object[][]{
                {"",                         cbHipoteca},
                {"Importe hipoteca (€):",    tfImporteHip},
                {"Tipo de interés (% TIN):", tfTipoInt},
                {"Plazo (años):",            tfPlazo},
                {"→ Cuota mensual:",         lblCuota}
            })),
            seccion("📋  GASTOS ANUALES", formGrid(new Object[][]{
                {"IBI anual (€):",           tfIbi},
                {"Comunidad mensual (€):",   tfComunidad},
                {"Seguro anual (€):",        tfSeguro},
                {"Mantenimiento anual (€):", tfMantenimiento},
                {"→ Total gastos/año:",      lblTotalGastos}
            })),
            seccion("📍  VALOR ACTUAL", formGrid(new Object[][]{
                {"Valor de mercado (€):",    tfValorMercado},
                {"Fecha de compra:",         dpFechaCompra},
                {"Notas:",                   taNotas}
            })),
            seccion("📊  RESULTADOS", formGrid(new Object[][]{
                {"Inversión total:",         lblInvTotal},
                {"Rentabilidad bruta:",      lblRentBruta},
                {"Rentabilidad neta:",       lblRentNeta},
                {"Cash flow mensual:",       lblCFMes},
                {"ROCE:",                    lblROCE},
                {"Capital propio:",          lblCapPropio},
                {"Plusvalía latente:",       lblPlusvalia},
                {"PER inmobiliario:",        lblPER},
                {"Calificación:",            lblSemaforo}
            }))
        );

        ScrollPane scroll = new ScrollPane(mainBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scroll.setPrefHeight(560);

        dialog.getDialogPane().setContent(scroll);

        // Primer cálculo
        recalcular.run();

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            DatosPiso d = new DatosPiso(piso.getIdPiso());
            d.setId(datosCargados.getId());
            d.setPrecioCompra(CalculadoraFinancieraPiso.parse(tfPrecio.getText()));
            d.setItpPagado(CalculadoraFinancieraPiso.parse(tfItp.getText()));
            d.setGastosNotaria(CalculadoraFinancieraPiso.parse(tfNotaria.getText()));
            d.setGastosRegistro(CalculadoraFinancieraPiso.parse(tfRegistro.getText()));
            d.setCosteReforma(CalculadoraFinancieraPiso.parse(tfReforma.getText()));
            d.setCosteMobiliario(CalculadoraFinancieraPiso.parse(tfMobiliario.getText()));
            d.setOtrosGastosCompra(CalculadoraFinancieraPiso.parse(tfOtros.getText()));
            d.setTieneHipoteca(cbHipoteca.isSelected());
            // Si la hipoteca no está activa, el importe se guarda en 0 sin
            // importar qué texto quede en el campo — misma regla que usa el
            // recálculo en vivo, vía el único sitio que la decide.
            d.setImporteHipoteca(CalculadoraFinancieraPiso.importeHipotecaEfectivo(
                    cbHipoteca.isSelected(), CalculadoraFinancieraPiso.parse(tfImporteHip.getText())));
            d.setTipoInteres(CalculadoraFinancieraPiso.parse(tfTipoInt.getText()));
            d.setPlazoAnos(CalculadoraFinancieraPiso.parseInt(tfPlazo.getText()));
            d.setCuotaMensualHipoteca(CalculadoraFinancieraPiso.cuotaMensual(
                    d.getImporteHipoteca(), d.getTipoInteres(), d.getPlazoAnos()));
            d.setGastoIbiAnual(CalculadoraFinancieraPiso.parse(tfIbi.getText()));
            d.setGastoComunidadMensual(CalculadoraFinancieraPiso.parse(tfComunidad.getText()));
            d.setGastoSeguroAnual(CalculadoraFinancieraPiso.parse(tfSeguro.getText()));
            d.setGastoMantenimientoAnual(CalculadoraFinancieraPiso.parse(tfMantenimiento.getText()));
            d.setValorMercadoActual(CalculadoraFinancieraPiso.parse(tfValorMercado.getText()));
            d.setFechaCompra(dpFechaCompra.getValue());
            d.setNotas(taNotas.getText().trim());
            return d;
        });

        dialog.showAndWait().ifPresent(d -> {
            try {
                datosPisoDAO.guardar(d);
                mostrarInfo("Datos guardados", "Los datos financieros del piso han sido guardados correctamente.");
            } catch (Exception ex) {
                mostrarError("Error al guardar", ex.getMessage());
            }
        });
    }

    // ── Helpers de UI para el diálogo ────────────────────────────

    private VBox seccion(String titulo, GridPane grid) {
        Label lbl = new Label(titulo);
        lbl.setStyle(
            "-fx-text-fill: #a89fff; -fx-font-size: 12px; " +
            "-fx-font-weight: bold; -fx-padding: 0 0 2 0;");
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #2a4a7a;");
        VBox box = new VBox(8, lbl, sep, grid);
        return box;
    }

    private GridPane formGrid(Object[][] rows) {
        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(9);
        grid.setPrefWidth(550);
        grid.setMinWidth(500);
        ColumnConstraints col0 = new ColumnConstraints(180, 200, 220);
        ColumnConstraints col1 = new ColumnConstraints(200, 280, Double.MAX_VALUE);
        col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        for (int r = 0; r < rows.length; r++) {
            String label = (String) rows[r][0];
            Node   field = (Node)   rows[r][1];
            if (!label.isBlank()) {
                Label lbl = new Label(label);
                lbl.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 11px; -fx-min-width: 160px;");
                grid.add(lbl, 0, r);
            }
            if (field instanceof TextField tf) {
                tf.setPrefWidth(Double.MAX_VALUE);
                if (tf.getStyle().isBlank()) tf.setStyle(TF_STYLE);
            }
            if (field instanceof DatePicker dp) {
                dp.setPrefWidth(Double.MAX_VALUE);
                dp.setStyle("-fx-background-color: #1a2744; -fx-border-color: #2a4a7a; -fx-border-radius: 4;");
                // make the inner text field visible too
                Platform.runLater(() -> {
                    if (dp.getEditor() != null)
                        dp.getEditor().setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
                });
            }
            if (field instanceof TextArea ta) GridPane.setColumnSpan(ta, 2);
            grid.add(field, label.isBlank() ? 0 : 1, r,
                    (field instanceof TextArea || label.isBlank()) ? 2 : 1, 1);
        }
        return grid;
    }

    private static final String TF_STYLE =
        "-fx-text-fill: white; -fx-background-color: #1a2744; " +
        "-fx-control-inner-background: #1a2744; " +
        "-fx-prompt-text-fill: #6688aa; -fx-border-color: #2a4a7a; " +
        "-fx-border-radius: 4; -fx-background-radius: 4; " +
        "-fx-padding: 6 10 6 10; -fx-font-size: 12px; -fx-pref-height: 30; " +
        "-fx-background-insets: 0; ";

    private TextField tf(BigDecimal valor) {
        TextField tf = new TextField();
        tf.setText(valor != null ? valor.stripTrailingZeros().toPlainString() : "");
        tf.setPromptText("0");
        tf.setStyle(TF_STYLE);
        tf.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                tf.lookup(".text").setStyle("-fx-fill: white;");
                tf.lookup(".content").setStyle("-fx-background-color: #1a2744;");
            }
        });
        return tf;
    }

    private Label resultLabel(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-text-fill: #1abc9c; -fx-font-size: 13px; -fx-font-weight: bold;");
        return l;
    }

    // ── Navegación a detalle ──────────────────────────────────────

    private void abrirDetalle(AnalisisPiso piso) {
        try {
            if (MainController.instance != null) MainController.instance.pushCurrentToHistorial();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionap/detalle-piso-view.fxml"));
            Node vista = loader.load();
            if (MainController.instance != null) MainController.instance.aplicarTemaAVista(vista);
            DetallePisoController ctrl = loader.getController();
            ctrl.setPiso(piso);
            StackPane panelCentro = (StackPane) root.getParent();
            panelCentro.getChildren().setAll(vista);
        } catch (Exception ex) {
            mostrarError("Error al abrir detalle", ex.getMessage());
        }
    }

    // ── Helpers generales ─────────────────────────────────────────

    private int toInt(Object o) {
        return o instanceof Number n ? n.intValue() : 0;
    }

    private double toDouble(Object o) {
        return o instanceof Number n ? n.doubleValue() : 0.0;
    }

    private String hexToRgba(String hex, String alpha) {
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return "rgba(" + r + "," + g + "," + b + "," + alpha + ")";
        } catch (Exception e) {
            return "rgba(100,100,100," + alpha + ")";
        }
    }

    private void aplicarCSS(DialogPane pane) {
        try {
            pane.getStylesheets().add(
                    getClass().getResource("/com/gestionap/styles.css").toExternalForm());
        } catch (Exception ignored) {}
    }

    private void mostrarInfo(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg);
        aplicarCSS(a.getDialogPane()); a.showAndWait();
    }

    private void mostrarError(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg);
        aplicarCSS(a.getDialogPane()); a.showAndWait();
    }
}
