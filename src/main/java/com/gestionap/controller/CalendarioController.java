package com.gestionap.controller;

import com.gestionap.dao.CalendarioDAO;
import com.gestionap.model.Contrato;
import com.gestionap.model.Pago;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CalendarioController implements Initializable {

    @FXML private Label             lblMesAnio;
    @FXML private Button            btnMesAnterior;
    @FXML private Button            btnMesSiguiente;
    @FXML private GridPane          gridCalendario;
    @FXML private Label             lblTotalEsperado;
    @FXML private Label             lblTotalCobrado;
    @FXML private Label             lblPendiente;
    @FXML private ProgressIndicator progressIndicator;

    private CalendarioDAO calendarioDAO;
    private YearMonth     mesActual;

    private static final String[]         DIAS  = {"LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM"};
    private static final DateTimeFormatter FMT   = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        calendarioDAO = new CalendarioDAO();
        mesActual     = YearMonth.now();
        actualizarTitulo();
        cargarCalendario();
        btnMesAnterior.setOnAction(e  -> { mesActual = mesActual.minusMonths(1); actualizarTitulo(); cargarCalendario(); });
        btnMesSiguiente.setOnAction(e -> { mesActual = mesActual.plusMonths(1);  actualizarTitulo(); cargarCalendario(); });
    }

    private void actualizarTitulo() {
        String t = mesActual.format(FMT);
        lblMesAnio.setText(Character.toUpperCase(t.charAt(0)) + t.substring(1));
    }

    private void cargarCalendario() {
        progressIndicator.setVisible(true);
        gridCalendario.setDisable(true);
        YearMonth mes = mesActual;
        new Thread(() -> {
            try {
                List<Contrato> contratos = calendarioDAO.listarContratosActivosMes(mes);
                List<Pago>     pagos     = calendarioDAO.listarPagosMes(mes);
                Platform.runLater(() -> {
                    construirGrid(mes, contratos, pagos);
                    actualizarTotales(contratos, pagos);
                    progressIndicator.setVisible(false);
                    gridCalendario.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    gridCalendario.setDisable(false);
                    lblTotalEsperado.setText("Error: " + ex.getMessage());
                });
            }
        }).start();
    }

    private void construirGrid(YearMonth mes, List<Contrato> contratos, List<Pago> pagos) {
        gridCalendario.getChildren().clear();
        gridCalendario.getColumnConstraints().clear();
        gridCalendario.getRowConstraints().clear();

        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setMinWidth(60);
            cc.setPercentWidth(100.0 / 7);
            gridCalendario.getColumnConstraints().add(cc);
        }

        // Day-of-week headers
        for (int i = 0; i < 7; i++) {
            Label hdr = new Label(DIAS[i]);
            hdr.setMaxWidth(Double.MAX_VALUE);
            hdr.setAlignment(Pos.CENTER);
            hdr.setStyle("-fx-text-fill: #6C63FF; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 2 4 2;");
            gridCalendario.add(hdr, i, 0);
        }

        Map<Integer, List<Pago>> pagosPorDia = pagos.stream()
                .collect(Collectors.groupingBy(p -> p.getFechaPago().getDayOfMonth()));

        // Contratos con al menos un pago registrado en el mes
        Set<Integer> contratosPagados = pagos.stream()
                .map(Pago::getIdContrato)
                .collect(Collectors.toSet());
        boolean hayContratos         = !contratos.isEmpty();
        boolean hayContratosImpagados = contratos.stream()
                .anyMatch(c -> !contratosPagados.contains(c.getIdContrato()));
        LocalDate hoy = LocalDate.now();

        int primerCol = mes.atDay(1).getDayOfWeek().getValue() - 1;
        int diasEnMes = mes.lengthOfMonth();
        int col = primerCol, row = 1;

        for (int dia = 1; dia <= diasEnMes; dia++) {
            LocalDate fecha      = mes.atDay(dia);
            List<Pago> pagosDia  = pagosPorDia.getOrDefault(dia, Collections.emptyList());
            String estado        = calcularEstado(dia, fecha, hoy, pagosDia, hayContratos, hayContratosImpagados);
            VBox celda           = crearCelda(dia, fecha, estado, pagosDia, contratos);
            gridCalendario.add(celda, col, row);
            if (++col == 7) { col = 0; row++; }
        }

        RowConstraints rHdr = new RowConstraints(28);
        gridCalendario.getRowConstraints().add(rHdr);
        for (int r = 0; r < row; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setMinHeight(60);
            gridCalendario.getRowConstraints().add(rc);
        }
    }

    private String calcularEstado(int dia, LocalDate fecha, LocalDate hoy,
                                  List<Pago> pagosDia, boolean hayContratos, boolean hayContratosImpagados) {
        if (!pagosDia.isEmpty())                        return "PAGADO";
        if (hayContratos && hayContratosImpagados && dia <= 5) {
            if (fecha.isAfter(hoy))                     return "ESPERADO";
            return "VENCIDO";
        }
        return "NORMAL";
    }

    private VBox crearCelda(int dia, LocalDate fecha, String estado,
                            List<Pago> pagosDia, List<Contrato> contratos) {
        VBox celda = new VBox(3);
        celda.setAlignment(Pos.TOP_LEFT);
        celda.setPadding(new Insets(5, 5, 5, 7));
        celda.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        boolean esHoy = fecha.equals(LocalDate.now());
        String bgColor = switch (estado) {
            case "PAGADO"   -> "#152a15";
            case "ESPERADO" -> "#2a1f00";
            case "VENCIDO"  -> "#2a0000";
            default         -> esHoy ? "#1e2a50" : "#16213e";
        };
        String borderColor = esHoy ? "#6C63FF" : "#2a3a5a";
        String borderWidth = esHoy ? "2" : "1";
        celda.setStyle("-fx-background-color: " + bgColor + "; " +
                       "-fx-border-color: " + borderColor + "; " +
                       "-fx-border-width: " + borderWidth + "; " +
                       "-fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");

        String diaColor = switch (estado) {
            case "PAGADO"   -> "#2ecc71";
            case "ESPERADO" -> "#f39c12";
            case "VENCIDO"  -> "#e74c3c";
            default         -> esHoy ? "#6C63FF" : "#c8c8e8";
        };

        Label lblDia = new Label(String.valueOf(dia));
        lblDia.setStyle("-fx-text-fill: " + diaColor + "; -fx-font-weight: bold; -fx-font-size: 13px;");
        celda.getChildren().add(lblDia);

        switch (estado) {
            case "PAGADO" -> {
                BigDecimal total = pagosDia.stream().map(Pago::getCantidad).reduce(BigDecimal.ZERO, BigDecimal::add);
                Label lbl = new Label("✓ " + String.format("%.0f €", total));
                lbl.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 9px;");
                celda.getChildren().add(lbl);
            }
            case "ESPERADO" -> {
                Label lbl = new Label("⏳");
                lbl.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 10px;");
                celda.getChildren().add(lbl);
            }
            case "VENCIDO" -> {
                Label lbl = new Label("⚠");
                lbl.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 10px;");
                celda.getChildren().add(lbl);
            }
        }

        celda.setOnMouseEntered(e -> celda.setStyle(celda.getStyle()
                .replace(bgColor, ajustarBrillo(bgColor))));
        celda.setOnMouseExited(e  -> celda.setStyle(celda.getStyle()
                .replace(ajustarBrillo(bgColor), bgColor)));
        celda.setOnMouseClicked(e -> mostrarDetalle(fecha, pagosDia, contratos, estado));
        return celda;
    }

    private String ajustarBrillo(String hex) {
        return switch (hex) {
            case "#152a15" -> "#1e3e1e";
            case "#2a1f00" -> "#3d2d00";
            case "#2a0000" -> "#3d0000";
            case "#16213e" -> "#1e2d55";
            case "#1e2a50" -> "#2a3a6a";
            default        -> hex;
        };
    }

    private void mostrarDetalle(LocalDate fecha, List<Pago> pagosDia,
                                List<Contrato> contratos, String estado) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle del día " + fecha.getDayOfMonth());
        alert.setHeaderText(fecha.toString());
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/gestionap/styles.css").toExternalForm());

        StringBuilder sb = new StringBuilder();
        if (!pagosDia.isEmpty()) {
            sb.append("PAGOS REGISTRADOS:\n");
            pagosDia.forEach(p -> sb.append("  • ").append(p.getNombreInquilino())
                    .append("  →  ").append(p.getCantidad()).append(" €")
                    .append("  (").append(p.getMetodoPago().name()).append(")\n"));
        } else if ("ESPERADO".equals(estado) || "VENCIDO".equals(estado)) {
            sb.append("Pagos esperados (ventana días 1-5):\n");
            contratos.forEach(c -> sb.append("  • ").append(c.getNombreInquilino())
                    .append("  →  ").append(c.getPrecioMensual()).append(" €")
                    .append("  (Hab. ").append(c.getNumeroHabitacion()).append(")\n"));
        } else {
            sb.append("Sin eventos en este día.");
        }

        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private void actualizarTotales(List<Contrato> contratos, List<Pago> pagos) {
        BigDecimal esperado  = contratos.stream().map(Contrato::getPrecioMensual).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cobrado   = pagos.stream().map(Pago::getCantidad).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pendiente = esperado.subtract(cobrado).max(BigDecimal.ZERO);
        lblTotalEsperado.setText(String.format("Esperado: %.0f €", esperado));
        lblTotalCobrado.setText(String.format("Cobrado: %.0f €", cobrado));
        lblPendiente.setText(String.format("Pendiente: %.0f €", pendiente));
    }
}
