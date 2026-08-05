package com.gestionap.controller;

import com.gestionap.dao.ActividadDAO;
import com.gestionap.dao.ContratoDAO;
import com.gestionap.dao.InquilinoDAO;
import com.gestionap.dao.PagoDAO;
import com.gestionap.model.Contrato;
import com.gestionap.model.Inquilino;
import com.gestionap.model.Pago;
import com.gestionap.model.Pago.MetodoPago;
import com.gestionap.utils.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.ResourceBundle;

public class PagosController implements Initializable {

    @FXML private TableView<Pago>              tablaPagos;
    @FXML private TableColumn<Pago, Integer>   colId;
    @FXML private TableColumn<Pago, String>    colInquilino;
    @FXML private TableColumn<Pago, Integer>   colContrato;
    @FXML private TableColumn<Pago, Object>    colCantidad;
    @FXML private TableColumn<Pago, String>    colMetodo;
    @FXML private TableColumn<Pago, Object>    colFecha;
    @FXML private ComboBox<String>             cmbFiltroInquilino;
    @FXML private TextField                    txtBuscar;
    @FXML private Button                       btnNuevoPago;
    @FXML private Button                       btnEliminarPago;
    @FXML private Label                        lblTotalPagos;
    @FXML private Label                        lblMesActual;
    @FXML private ProgressIndicator            progressIndicator;

    private PagoDAO                  pagoDAO;
    private ContratoDAO              contratoDAO;
    private InquilinoDAO             inquilinoDAO;
    private ActividadDAO             actividadDAO;
    private ObservableList<Pago>     listaPagos;
    private FilteredList<Pago>       listaFiltrada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instances();
        initGUI();
        actions();
    }

    private void instances() {
        pagoDAO      = new PagoDAO();
        contratoDAO  = new ContratoDAO();
        inquilinoDAO = new InquilinoDAO();
        actividadDAO = new ActividadDAO();
        listaPagos   = FXCollections.observableArrayList();
        listaFiltrada = new FilteredList<>(listaPagos, p -> true);
    }

    private void initGUI() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idPago"));
        colInquilino.setCellValueFactory(new PropertyValueFactory<>("nombreInquilino"));
        colContrato.setCellValueFactory(new PropertyValueFactory<>("idContrato"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaPago"));

        colMetodo.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getMetodoPago().name()));
        colMetodo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String metodo, boolean empty) {
                super.updateItem(metodo, empty);
                if (empty || metodo == null) { setGraphic(null); return; }
                Label badge = new Label(metodo);
                badge.getStyleClass().addAll("badge", "badge-" + metodo);
                setGraphic(badge); setText(null);
            }
        });

        cargarFiltroInquilinos();
        tablaPagos.setPlaceholder(new Label("No hay pagos registrados."));
        tablaPagos.setItems(listaFiltrada);
        cargarDatos();
    }

    private void actions() {
        cmbFiltroInquilino.valueProperty().addListener((obs, old, val) -> aplicarFiltro());
        txtBuscar.textProperty().addListener((obs, old, val)           -> aplicarFiltro());

        btnNuevoPago.setOnAction(e    -> abrirDialogoNuevoPago());
        btnEliminarPago.setOnAction(e -> eliminarSeleccionado());
    }

    private void aplicarFiltro() {
        String combo = cmbFiltroInquilino.getValue();
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        listaFiltrada.setPredicate(p -> {
            boolean pasaCombo = combo == null || combo.equals("Todos")
                    || (p.getNombreInquilino() != null && p.getNombreInquilino().equals(combo));
            if (!pasaCombo) return false;
            if (texto.isBlank()) return true;
            return p.getNombreInquilino() != null && p.getNombreInquilino().toLowerCase().contains(texto);
        });
        actualizarTotal();
    }

    private void cargarDatos() {
        progressIndicator.setVisible(true);
        tablaPagos.setDisable(true);
        new Thread(() -> {
            try {
                List<Pago> data = pagoDAO.listarTodos();
                Platform.runLater(() -> {
                    listaPagos.setAll(data);
                    actualizarTotal();
                    progressIndicator.setVisible(false);
                    tablaPagos.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    tablaPagos.setDisable(false);
                    mostrarError("Error al cargar pagos", ex.getMessage());
                });
            }
        }).start();
    }

    private void cargarFiltroInquilinos() {
        try {
            List<Inquilino> inquilinos = inquilinoDAO.listarTodos();
            ObservableList<String> nombres = FXCollections.observableArrayList();
            nombres.add("Todos");
            inquilinos.forEach(i -> nombres.add(i.getNombreCompleto()));
            cmbFiltroInquilino.setItems(nombres);
            cmbFiltroInquilino.setValue("Todos");
        } catch (Exception ex) {
            cmbFiltroInquilino.setItems(FXCollections.observableArrayList("Todos"));
            cmbFiltroInquilino.setValue("Todos");
        }
    }

    private void actualizarTotal() {
        BigDecimal total = listaFiltrada.stream()
                .map(Pago::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalPagos.setText(String.format("Total: %.2f €", total));

        YearMonth mesActual = YearMonth.now();
        BigDecimal totalMes = listaFiltrada.stream()
                .filter(p -> p.getFechaPago() != null
                        && YearMonth.from(p.getFechaPago()).equals(mesActual))
                .map(Pago::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblMesActual.setText(String.format("Este mes: %.2f €", totalMes));
    }

    private void eliminarSeleccionado() {
        Pago sel = tablaPagos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un pago para eliminar."); return; }

        Alert confirm = crearAlerta(Alert.AlertType.CONFIRMATION,
                "Eliminar pago",
                "¿Eliminar el pago de " + sel.getCantidad() + " €?",
                "Inquilino: " + sel.getNombreInquilino());
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                pagoDAO.eliminar(sel.getIdPago());
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo eliminar el pago", ex.getMessage());
            }
        });
    }

    private void abrirDialogoNuevoPago() {
        Dialog<Pago> dialog = new Dialog<>();
        dialog.setTitle("Registrar Pago");
        dialog.setHeaderText("Registrar un nuevo pago de alquiler");
        aplicarCSS(dialog.getDialogPane());

        ButtonType btnGuardar = new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(4, 0, 4, 0));

        ComboBox<Contrato>  cmbContrato = new ComboBox<>();
        TextField           tfCantidad  = new TextField();
        ComboBox<MetodoPago> cmbMetodo  = new ComboBox<>(
                FXCollections.observableArrayList(MetodoPago.values()));
        DatePicker dpFecha = new DatePicker(LocalDate.now());

        cmbContrato.setPromptText("Seleccionar contrato activo");
        tfCantidad.setPromptText("Importe (ej: 600.00)");
        cmbMetodo.setPromptText("Método de pago");

        for (var c : new Control[]{cmbContrato, tfCantidad, cmbMetodo, dpFecha})
            c.setPrefWidth(280);

        try {
            List<Contrato> contratos = contratoDAO.listarActivos();
            cmbContrato.setItems(FXCollections.observableArrayList(contratos));
            cmbContrato.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Contrato c) {
                    return c == null ? "" : "#" + c.getIdContrato() + "  —  " +
                           c.getNombreInquilino() + "  (Hab. " + c.getNumeroHabitacion() + ")";
                }
                public Contrato fromString(String s) { return null; }
            });
        } catch (Exception ex) {
            mostrarError("Error al cargar contratos", ex.getMessage());
            return;
        }

        cmbContrato.valueProperty().addListener((obs, old, c) -> {
            if (c != null && tfCantidad.getText().isBlank())
                tfCantidad.setText(c.getPrecioMensual().toPlainString());
        });

        Label lblValidacion = new Label();
        lblValidacion.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");

        grid.add(labelForm("Contrato *:"),  0, 0); grid.add(cmbContrato, 1, 0);
        grid.add(labelForm("Importe *:"),   0, 1); grid.add(tfCantidad,  1, 1);
        grid.add(labelForm("Método *:"),    0, 2); grid.add(cmbMetodo,   1, 2);
        grid.add(labelForm("Fecha *:"),     0, 3); grid.add(dpFecha,     1, 3);
        grid.add(lblValidacion,             0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node okNode = dialog.getDialogPane().lookupButton(btnGuardar);
        okNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (cmbContrato.getValue() == null) {
                lblValidacion.setText("Selecciona un contrato activo.");
                event.consume(); return;
            }
            if (cmbMetodo.getValue() == null) {
                lblValidacion.setText("Selecciona el método de pago.");
                event.consume(); return;
            }
            String imp = tfCantidad.getText().trim();
            if (imp.isBlank()) {
                lblValidacion.setText("El importe es obligatorio.");
                event.consume(); return;
            }
            try {
                if (new BigDecimal(imp).compareTo(BigDecimal.ZERO) <= 0) {
                    lblValidacion.setText("El importe debe ser mayor que cero.");
                    event.consume();
                }
            } catch (NumberFormatException ex) {
                lblValidacion.setText("El importe debe ser un número (ej: 600.00).");
                event.consume();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            if (cmbContrato.getValue() == null || cmbMetodo.getValue() == null) return null;
            try {
                Pago p = new Pago();
                p.setIdContrato(cmbContrato.getValue().getIdContrato());
                p.setCantidad(new BigDecimal(tfCantidad.getText().trim()));
                p.setMetodoPago(cmbMetodo.getValue());
                p.setFechaPago(dpFecha.getValue() != null ? dpFecha.getValue() : LocalDate.now());
                return p;
            } catch (NumberFormatException ex) {
                return null;
            }
        });

        dialog.showAndWait().ifPresent(p -> {
            try {
                pagoDAO.insertar(p);
                uid(uid -> actividadDAO.registrar(uid, "REGISTRAR_PAGO",
                        "Pago de " + p.getCantidad() + " € registrado (contrato #" + p.getIdContrato() + ")"));
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo registrar el pago", ex.getMessage());
            }
        });
    }

    private Label labelForm(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("form-label");
        return l;
    }

    private void aplicarCSS(DialogPane pane) {
        pane.getStylesheets().add(
                getClass().getResource("/com/gestionap/styles.css").toExternalForm());
    }

    private Alert crearAlerta(Alert.AlertType tipo, String titulo, String header, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(contenido);
        aplicarCSS(alert.getDialogPane());
        return alert;
    }

    private void mostrarAviso(String msg) {
        crearAlerta(Alert.AlertType.WARNING, "Aviso", null, msg).showAndWait();
    }

    private void mostrarError(String header, String msg) {
        crearAlerta(Alert.AlertType.ERROR, "Error", header, msg).showAndWait();
    }

    private void uid(java.util.function.IntConsumer consumer) {
        var u = Session.getInstance().getUsuarioActual();
        if (u != null) consumer.accept(u.getIdUsuario());
    }
}
