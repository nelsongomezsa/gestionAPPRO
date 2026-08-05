package com.gestionap.controller;

import com.gestionap.dao.ActividadDAO;
import com.gestionap.dao.ContratoDAO;
import com.gestionap.dao.HabitacionDAO;
import com.gestionap.dao.InquilinoDAO;
import com.gestionap.model.Contrato;
import com.gestionap.model.Habitacion;
import com.gestionap.model.Inquilino;
import com.gestionap.utils.ContratoGenerator;
import com.gestionap.utils.EmailService;
import com.gestionap.utils.PdfExporter;
import com.gestionap.utils.Session;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.awt.Desktop;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ContratosController implements Initializable {

    @FXML private TableView<Contrato>              tablaContratos;
    @FXML private TableColumn<Contrato, Integer>   colId;
    @FXML private TableColumn<Contrato, String>    colInquilino;
    @FXML private TableColumn<Contrato, Integer>   colHabitacion;
    @FXML private TableColumn<Contrato, String>    colPiso;
    @FXML private TableColumn<Contrato, Object>    colFechaInicio;
    @FXML private TableColumn<Contrato, Object>    colFechaFin;
    @FXML private TableColumn<Contrato, Object>    colPrecio;
    @FXML private TableColumn<Contrato, String>    colEstado;
    @FXML private TableColumn<Contrato, String>    colDiasRestantes;
    @FXML private ComboBox<String>                 cmbFiltro;
    @FXML private ProgressIndicator               progressIndicator;
    @FXML private Button                           btnNuevoContrato;
    @FXML private Button                           btnFinalizar;
    @FXML private Button                           btnExportarPdf;
    @FXML private Button                           btnGenerarContrato;
    @FXML private Button                           btnRecordatorio;
    @FXML private Button                           btnVerEnMapa;

    private ContratoDAO                contratoDAO;
    private HabitacionDAO              habitacionDAO;
    private InquilinoDAO               inquilinoDAO;
    private ActividadDAO               actividadDAO;
    private ObservableList<Contrato>   listaContratos;
    private FilteredList<Contrato>     listaFiltrada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instances();
        initGUI();
        actions();
    }

    private void instances() {
        contratoDAO    = new ContratoDAO();
        habitacionDAO  = new HabitacionDAO();
        inquilinoDAO   = new InquilinoDAO();
        actividadDAO   = new ActividadDAO();
        listaContratos = FXCollections.observableArrayList();
        listaFiltrada  = new FilteredList<>(listaContratos, c -> true);
    }

    private void initGUI() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idContrato"));
        colInquilino.setCellValueFactory(new PropertyValueFactory<>("nombreInquilino"));
        colHabitacion.setCellValueFactory(new PropertyValueFactory<>("numeroHabitacion"));
        colPiso.setCellValueFactory(new PropertyValueFactory<>("direccionPiso"));
        colFechaInicio.setCellValueFactory(new PropertyValueFactory<>("fechaInicio"));
        colFechaFin.setCellValueFactory(new PropertyValueFactory<>("fechaFin"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioMensual"));

        colEstado.setCellValueFactory(cellData -> {
            boolean activo = cellData.getValue().isActivo();
            return new SimpleStringProperty(activo ? "activo" : "finalizado");
        });
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setGraphic(null); return; }
                Label badge = new Label(estado);
                badge.getStyleClass().addAll("badge", "badge-" + estado);
                setGraphic(badge); setText(null);
            }
        });

        colDiasRestantes.setCellValueFactory(d -> {
            Contrato c = d.getValue();
            if (!c.isActivo()) return new SimpleStringProperty("—");
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), c.getFechaFin());
            return new SimpleStringProperty(String.valueOf(dias));
        });
        colDiasRestantes.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); setStyle(""); return; }
                setText(v.equals("—") ? "—" : v + " d");
                if (!v.equals("—")) {
                    long dias = Long.parseLong(v);
                    if (dias < 0)
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    else if (dias <= 7)
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    else if (dias <= 30)
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    else
                        setStyle("-fx-text-fill: #2ecc71;");
                } else {
                    setStyle("-fx-text-fill: #888;");
                }
            }
        });

        cmbFiltro.setItems(FXCollections.observableArrayList("Todos", "Activos", "Finalizados"));
        cmbFiltro.setValue("Todos");
        tablaContratos.setPlaceholder(new Label("No hay contratos que mostrar."));
        tablaContratos.setItems(listaFiltrada);
        cargarDatos();
    }

    private void actions() {
        cmbFiltro.valueProperty().addListener((obs, old, val) -> {
            listaFiltrada.setPredicate(c -> switch (val == null ? "Todos" : val) {
                case "Activos"    -> c.isActivo();
                case "Finalizados" -> !c.isActivo();
                default           -> true;
            });
        });

        btnNuevoContrato.setOnAction(e    -> abrirDialogoNuevoContrato());
        btnFinalizar.setOnAction(e        -> finalizarSeleccionado());
        btnExportarPdf.setOnAction(e      -> exportarPdf());
        btnGenerarContrato.setOnAction(e  -> generarContratoPdf());
        btnRecordatorio.setOnAction(e     -> enviarRecordatorio());
        btnVerEnMapa.setOnAction(e        -> verEnMapa());
    }

    private void cargarDatos() {
        progressIndicator.setVisible(true);
        tablaContratos.setDisable(true);
        new Thread(() -> {
            try {
                List<Contrato> data = contratoDAO.listarTodos();
                Platform.runLater(() -> {
                    listaContratos.setAll(data);
                    progressIndicator.setVisible(false);
                    tablaContratos.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    tablaContratos.setDisable(false);
                    mostrarError("Error al cargar contratos", ex.getMessage());
                });
            }
        }).start();
    }

    private void finalizarSeleccionado() {
        Contrato sel = tablaContratos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un contrato para finalizar."); return; }
        if (!sel.isActivo()) { mostrarAviso("El contrato ya está finalizado."); return; }

        Alert confirm = crearAlerta(Alert.AlertType.CONFIRMATION,
                "Finalizar contrato",
                "¿Finalizar el contrato de " + sel.getNombreInquilino() + "?",
                "Se establecerá la fecha de fin a hoy y la habitación quedará disponible.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                contratoDAO.finalizarContrato(sel.getIdContrato());
                habitacionDAO.actualizarEstado(sel.getIdHabitacion(), Habitacion.Estado.disponible);
                uid(uid -> actividadDAO.registrar(uid, "FINALIZAR_CONTRATO",
                        "Contrato #" + sel.getIdContrato() + " de " + sel.getNombreInquilino() + " finalizado"));
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo finalizar el contrato", ex.getMessage());
            }
        });
    }

    private void abrirDialogoNuevoContrato() {
        Dialog<Contrato> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Contrato");
        dialog.setHeaderText("Crear un nuevo contrato de alquiler");
        aplicarCSS(dialog.getDialogPane());

        ButtonType btnGuardar = new ButtonType("Crear Contrato", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(4, 0, 4, 0));

        ComboBox<Inquilino>   cmbInquilino   = new ComboBox<>();
        ComboBox<Habitacion>  cmbHabitacion  = new ComboBox<>();
        DatePicker            dpInicio       = new DatePicker(LocalDate.now());
        DatePicker            dpFin          = new DatePicker(LocalDate.now().plusMonths(12));
        TextField             tfPrecio       = new TextField();

        cmbInquilino.setPromptText("Seleccionar inquilino");
        cmbHabitacion.setPromptText("Habitación disponible");
        tfPrecio.setPromptText("Precio mensual (ej: 600.00)");

        for (var c : new Control[]{cmbInquilino, cmbHabitacion, dpInicio, dpFin, tfPrecio})
            c.setPrefWidth(280);

        try {
            List<Inquilino> inquilinos = inquilinoDAO.listarTodos();
            cmbInquilino.setItems(FXCollections.observableArrayList(inquilinos));
            cmbInquilino.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Inquilino i) { return i == null ? "" : i.getNombreCompleto() + "  —  " + i.getDni(); }
                public Inquilino fromString(String s) { return null; }
            });

            List<Habitacion> habsDisponibles = habitacionDAO.listarDisponibles();
            cmbHabitacion.setItems(FXCollections.observableArrayList(habsDisponibles));
            cmbHabitacion.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Habitacion h) {
                    return h == null ? "" : "Hab. " + h.getNumero() + "  —  " + h.getDireccionPiso() + "  (" + h.getPrecio() + " €)";
                }
                public Habitacion fromString(String s) { return null; }
            });
        } catch (Exception ex) {
            mostrarError("Error al cargar datos", ex.getMessage());
            return;
        }

        cmbHabitacion.valueProperty().addListener((obs, old, hab) -> {
            if (hab != null && tfPrecio.getText().isBlank())
                tfPrecio.setText(hab.getPrecio().toPlainString());
        });

        Label lblValidacion = new Label();
        lblValidacion.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");

        grid.add(labelForm("Inquilino *:"),     0, 0); grid.add(cmbInquilino,  1, 0);
        grid.add(labelForm("Habitación *:"),    0, 1); grid.add(cmbHabitacion, 1, 1);
        grid.add(labelForm("Fecha inicio *:"),  0, 2); grid.add(dpInicio,      1, 2);
        grid.add(labelForm("Fecha fin *:"),     0, 3); grid.add(dpFin,         1, 3);
        grid.add(labelForm("Precio / mes *:"),  0, 4); grid.add(tfPrecio,      1, 4);
        grid.add(lblValidacion,                 0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node okNode = dialog.getDialogPane().lookupButton(btnGuardar);
        okNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (cmbInquilino.getValue() == null) {
                lblValidacion.setText("Selecciona un inquilino.");
                event.consume(); return;
            }
            if (cmbHabitacion.getValue() == null) {
                lblValidacion.setText("Selecciona una habitación.");
                event.consume(); return;
            }
            if (dpInicio.getValue() == null || dpFin.getValue() == null) {
                lblValidacion.setText("Las fechas son obligatorias.");
                event.consume(); return;
            }
            if (!dpFin.getValue().isAfter(dpInicio.getValue())) {
                lblValidacion.setText("La fecha de fin debe ser posterior a la de inicio.");
                event.consume(); return;
            }
            String prec = tfPrecio.getText().trim();
            if (prec.isBlank()) {
                lblValidacion.setText("El precio es obligatorio.");
                event.consume(); return;
            }
            try { new BigDecimal(prec); } catch (NumberFormatException ex) {
                lblValidacion.setText("El precio debe ser un número (ej: 600.00).");
                event.consume();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            try {
                Contrato c = new Contrato();
                c.setIdInquilino(cmbInquilino.getValue().getIdInquilino());
                c.setIdHabitacion(cmbHabitacion.getValue().getIdHabitacion());
                c.setFechaInicio(dpInicio.getValue());
                c.setFechaFin(dpFin.getValue());
                c.setPrecioMensual(new BigDecimal(tfPrecio.getText().trim()));
                return c;
            } catch (NumberFormatException ex) {
                return null;
            }
        });

        dialog.showAndWait().ifPresent(c -> {
            try {
                contratoDAO.insertar(c);
                habitacionDAO.actualizarEstado(c.getIdHabitacion(), Habitacion.Estado.alquilada);
                uid(uid -> actividadDAO.registrar(uid, "CREAR_CONTRATO",
                        "Contrato creado para hab. " + c.getIdHabitacion()));
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo crear el contrato", ex.getMessage());
            }
        });
    }

    private void generarContratoPdf() {
        Contrato sel = tablaContratos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un contrato para generar el PDF."); return; }
        ContratoGenerator.generar(sel);
    }

    private void enviarRecordatorio() {
        Contrato sel = tablaContratos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un contrato para enviar el recordatorio."); return; }
        try {
            Inquilino inq = inquilinoDAO.buscarPorId(sel.getIdInquilino());
            String email  = (inq != null) ? inq.getEmail() : null;
            String asunto = "Recordatorio de pago - " + sel.getNombreInquilino();
            String cuerpo = "Estimado/a " + sel.getNombreInquilino() + ",\n\n" +
                    "Le recordamos que el próximo pago de su arrendamiento de la habitación " +
                    sel.getNumeroHabitacion() + " en " + sel.getDireccionPiso() + " está pendiente.\n\n" +
                    "Importe mensual: " + sel.getPrecioMensual().toPlainString() + " €\n" +
                    "Fecha fin de contrato: " + sel.getFechaFin() + "\n\n" +
                    "Gracias por su colaboración.\n\nEquipo GestionAp";
            EmailService.mostrarDialogo(email, asunto, cuerpo);
        } catch (Exception ex) {
            mostrarError("Error al preparar el recordatorio", ex.getMessage());
        }
    }

    private void verEnMapa() {
        Contrato sel = tablaContratos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un contrato para ver en el mapa."); return; }
        try {
            String dir     = sel.getDireccionPiso() != null ? sel.getDireccionPiso() : "";
            String encoded = URLEncoder.encode(dir, StandardCharsets.UTF_8);
            Desktop.getDesktop().browse(new URI("https://maps.google.com/?q=" + encoded));
        } catch (Exception ex) {
            mostrarError("No se pudo abrir el mapa", ex.getMessage());
        }
    }

    private void exportarPdf() {
        List<String> cabeceras = List.of("ID", "INQUILINO", "HAB.", "DIRECCIÓN", "INICIO", "FIN", "€/MES", "ESTADO");
        List<String[]> filas = listaFiltrada.stream()
                .map(c -> new String[]{
                        String.valueOf(c.getIdContrato()),
                        c.getNombreInquilino(),
                        String.valueOf(c.getNumeroHabitacion()),
                        c.getDireccionPiso() != null ? c.getDireccionPiso() : "",
                        c.getFechaInicio().toString(),
                        c.getFechaFin().toString(),
                        c.getPrecioMensual().toPlainString() + " €",
                        c.isActivo() ? "activo" : "finalizado"
                })
                .collect(Collectors.toList());
        PdfExporter.exportar("Contratos", cabeceras, filas);
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
