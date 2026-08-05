package com.gestionap.controller;

import com.gestionap.dao.HabitacionDAO;
import com.gestionap.dao.PisoDAO;
import com.gestionap.model.Habitacion;
import com.gestionap.model.Habitacion.Estado;
import com.gestionap.model.Piso;
import com.gestionap.utils.PdfExporter;
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
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class HabitacionesController implements Initializable {

    @FXML private TableView<Habitacion>                tablaHabitaciones;
    @FXML private TableColumn<Habitacion, Integer>     colId;
    @FXML private TableColumn<Habitacion, Integer>     colNumero;
    @FXML private TableColumn<Habitacion, String>      colEstado;
    @FXML private TableColumn<Habitacion, Object>      colPrecio;
    @FXML private TableColumn<Habitacion, String>      colInquilinoActual;
    @FXML private TableColumn<Habitacion, String>      colDias;
    @FXML private TableColumn<Habitacion, String>      colCiudad;
    @FXML private TableColumn<Habitacion, String>      colPiso;
    @FXML private ComboBox<String>                     cmbFiltroEstado;
    @FXML private TextField                            txtBuscar;
    @FXML private Button                               btnAnadir;
    @FXML private Button                               btnEditar;
    @FXML private Button                               btnEliminar;
    @FXML private Button                               btnCambiarEstado;
    @FXML private Button                               btnExportarPdf;
    @FXML private Button                               btnVerEnMapa;
    @FXML private ProgressIndicator                    progressIndicator;

    private HabitacionDAO               habitacionDAO;
    private PisoDAO                     pisoDAO;
    private ObservableList<Habitacion>  listaHabitaciones;
    private FilteredList<Habitacion>    listaFiltrada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instances();
        initGUI();
        actions();
    }

    private void instances() {
        habitacionDAO     = new HabitacionDAO();
        pisoDAO           = new PisoDAO();
        listaHabitaciones = FXCollections.observableArrayList();
        listaFiltrada     = new FilteredList<>(listaHabitaciones, h -> true);
    }

    private void initGUI() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idHabitacion"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("nombreCiudad"));
        colPiso.setCellValueFactory(new PropertyValueFactory<>("direccionPiso"));

        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                Label badge = new Label(v);
                badge.getStyleClass().addAll("badge", "badge-" + v);
                setGraphic(badge); setText(null);
            }
        });

        colInquilinoActual.setCellValueFactory(d -> {
            String inq = d.getValue().getInquilinoActual();
            return new SimpleStringProperty(inq != null ? inq : "—");
        });

        colDias.setCellValueFactory(d -> {
            Habitacion h = d.getValue();
            if (h.getEstado() != Estado.alquilada || h.getDiasAlquilada() == 0)
                return new SimpleStringProperty("—");
            return new SimpleStringProperty(h.getDiasAlquilada() + " días");
        });

        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos", "disponible", "alquilada", "mantenimiento"));
        cmbFiltroEstado.setValue("Todos");

        tablaHabitaciones.setPlaceholder(new Label("No hay habitaciones que mostrar."));
        tablaHabitaciones.setItems(listaFiltrada);
        cargarDatos();
    }

    private void actions() {
        cmbFiltroEstado.valueProperty().addListener((obs, old, val) -> aplicarFiltro());
        txtBuscar.textProperty().addListener((obs, old, val)         -> aplicarFiltro());

        btnAnadir.setOnAction(e        -> abrirDialogoHabitacion(null));
        btnEditar.setOnAction(e        -> editarSeleccionada());
        btnEliminar.setOnAction(e      -> eliminarSeleccionada());
        btnCambiarEstado.setOnAction(e -> cambiarEstadoSeleccionada());
        btnExportarPdf.setOnAction(e   -> exportarPdf());
        btnVerEnMapa.setOnAction(e     -> verEnMapa());
    }

    private void aplicarFiltro() {
        String estadoFiltro = cmbFiltroEstado.getValue();
        String texto = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();
        listaFiltrada.setPredicate(h -> {
            boolean pasaEstado = estadoFiltro == null || estadoFiltro.equals("Todos")
                    || h.getEstado().name().equals(estadoFiltro);
            if (!pasaEstado) return false;
            if (texto.isBlank()) return true;
            return String.valueOf(h.getNumero()).contains(texto)
                    || (h.getNombreCiudad() != null && h.getNombreCiudad().toLowerCase().contains(texto))
                    || (h.getDireccionPiso() != null && h.getDireccionPiso().toLowerCase().contains(texto))
                    || (h.getInquilinoActual() != null && h.getInquilinoActual().toLowerCase().contains(texto));
        });
    }

    private void cargarDatos() {
        progressIndicator.setVisible(true);
        tablaHabitaciones.setDisable(true);
        new Thread(() -> {
            try {
                List<Habitacion> data = habitacionDAO.listarTodas();
                Platform.runLater(() -> {
                    listaHabitaciones.setAll(data);
                    progressIndicator.setVisible(false);
                    tablaHabitaciones.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    tablaHabitaciones.setDisable(false);
                    mostrarError("Error al cargar habitaciones", ex.getMessage());
                });
            }
        }).start();
    }

    private void editarSeleccionada() {
        Habitacion sel = tablaHabitaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una habitación para editar."); return; }
        abrirDialogoHabitacion(sel);
    }

    private void eliminarSeleccionada() {
        Habitacion sel = tablaHabitaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una habitación para eliminar."); return; }

        Alert confirm = crearAlerta(Alert.AlertType.CONFIRMATION,
                "Confirmar eliminación",
                "¿Eliminar habitación nº " + sel.getNumero() + "?",
                "Esta acción no se puede deshacer.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                habitacionDAO.eliminar(sel.getIdHabitacion());
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo eliminar", ex.getMessage());
            }
        });
    }

    private void cambiarEstadoSeleccionada() {
        Habitacion sel = tablaHabitaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una habitación para cambiar su estado."); return; }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(sel.getEstado().name(),
                "disponible", "alquilada", "mantenimiento");
        dialog.setTitle("Cambiar Estado");
        dialog.setHeaderText("Habitación nº " + sel.getNumero());
        dialog.setContentText("Nuevo estado:");
        aplicarCSS(dialog.getDialogPane());

        dialog.showAndWait().ifPresent(nuevoEstado -> {
            try {
                habitacionDAO.actualizarEstado(sel.getIdHabitacion(), Estado.valueOf(nuevoEstado));
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo cambiar el estado", ex.getMessage());
            }
        });
    }

    private void abrirDialogoHabitacion(Habitacion habitacion) {
        boolean esNueva = (habitacion == null);

        Dialog<Habitacion> dialog = new Dialog<>();
        dialog.setTitle(esNueva ? "Nueva Habitación" : "Editar Habitación");
        dialog.setHeaderText(esNueva ? "Introduce los datos de la habitación"
                                     : "Modifica los datos de la habitación");
        aplicarCSS(dialog.getDialogPane());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(4, 0, 4, 0));

        TextField       tfNumero  = new TextField();
        TextField       tfPrecio  = new TextField();
        ComboBox<String> cmbEstado = new ComboBox<>(FXCollections.observableArrayList(
                "disponible", "alquilada", "mantenimiento"));
        ComboBox<Piso>   cmbPiso   = new ComboBox<>();

        tfNumero.setPromptText("Número");
        tfPrecio.setPromptText("Precio mensual (ej: 450.00)");
        cmbEstado.setPromptText("Estado");
        cmbPiso.setPromptText("Seleccionar piso");
        cmbPiso.setPrefWidth(280);
        cmbEstado.setPrefWidth(200);

        try {
            cmbPiso.setItems(FXCollections.observableArrayList(pisoDAO.listarTodos()));
        } catch (Exception ex) {
            mostrarError("Error al cargar pisos", ex.getMessage());
        }

        if (!esNueva) {
            tfNumero.setText(String.valueOf(habitacion.getNumero()));
            tfPrecio.setText(habitacion.getPrecio().toPlainString());
            cmbEstado.setValue(habitacion.getEstado().name());
        }

        Label lblErr = new Label();
        lblErr.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");

        grid.add(labelForm("Número:"),   0, 0); grid.add(tfNumero,  1, 0);
        grid.add(labelForm("Precio €:"), 0, 1); grid.add(tfPrecio,  1, 1);
        grid.add(labelForm("Estado:"),   0, 2); grid.add(cmbEstado, 1, 2);
        grid.add(labelForm("Piso:"),     0, 3); grid.add(cmbPiso,   1, 3);
        grid.add(lblErr,                 0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node okNode = dialog.getDialogPane().lookupButton(btnGuardar);
        okNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String num  = tfNumero.getText().trim();
            String prec = tfPrecio.getText().trim();
            if (num.isBlank()) {
                lblErr.setText("El número de habitación es obligatorio.");
                event.consume(); return;
            }
            try { Integer.parseInt(num); } catch (NumberFormatException ex) {
                lblErr.setText("El número debe ser un entero (ej: 1)."); event.consume(); return;
            }
            if (prec.isBlank()) {
                lblErr.setText("El precio es obligatorio."); event.consume(); return;
            }
            try {
                if (new BigDecimal(prec).compareTo(BigDecimal.ZERO) < 0) {
                    lblErr.setText("El precio no puede ser negativo."); event.consume(); return;
                }
            } catch (NumberFormatException ex) {
                lblErr.setText("El precio debe ser un número (ej: 450.00)."); event.consume(); return;
            }
            if (cmbEstado.getValue() == null) {
                lblErr.setText("Selecciona un estado."); event.consume(); return;
            }
            if (cmbPiso.getValue() == null) {
                lblErr.setText("Selecciona un piso."); event.consume();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            try {
                Habitacion h = esNueva ? new Habitacion() : habitacion;
                h.setNumero(Integer.parseInt(tfNumero.getText().trim()));
                h.setPrecio(new BigDecimal(tfPrecio.getText().trim()));
                h.setEstado(Estado.valueOf(cmbEstado.getValue()));
                h.setIdPiso(cmbPiso.getValue().getIdPiso());
                return h;
            } catch (NumberFormatException ex) { return null; }
        });

        dialog.showAndWait().ifPresent(h -> {
            try {
                if (esNueva) habitacionDAO.insertar(h);
                else         habitacionDAO.actualizar(h);
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo guardar", ex.getMessage());
            }
        });
    }

    private void verEnMapa() {
        Habitacion sel = tablaHabitaciones.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una habitación para ver en el mapa."); return; }
        try {
            String dir     = (sel.getNombreCiudad() != null ? sel.getNombreCiudad() + " " : "")
                           + (sel.getDireccionPiso() != null ? sel.getDireccionPiso() : "");
            String encoded = URLEncoder.encode(dir.trim(), StandardCharsets.UTF_8);
            Desktop.getDesktop().browse(new URI("https://maps.google.com/?q=" + encoded));
        } catch (Exception ex) {
            mostrarError("No se pudo abrir el mapa", ex.getMessage());
        }
    }

    private void exportarPdf() {
        List<String> cab = List.of("ID", "NÚM.", "ESTADO", "€/MES", "INQUILINO", "DÍAS", "CIUDAD", "DIRECCIÓN");
        List<String[]> filas = listaFiltrada.stream()
                .map(h -> new String[]{
                        String.valueOf(h.getIdHabitacion()),
                        String.valueOf(h.getNumero()),
                        h.getEstado().name(),
                        h.getPrecio().toPlainString() + " €",
                        h.getInquilinoActual() != null ? h.getInquilinoActual() : "—",
                        h.getEstado() == Estado.alquilada ? h.getDiasAlquilada() + " d" : "—",
                        h.getNombreCiudad() != null ? h.getNombreCiudad() : "",
                        h.getDireccionPiso() != null ? h.getDireccionPiso() : ""
                })
                .collect(Collectors.toList());
        PdfExporter.exportar("Habitaciones", cab, filas);
    }

    private Label labelForm(String texto) {
        Label l = new Label(texto); l.getStyleClass().add("form-label"); return l;
    }

    private void aplicarCSS(DialogPane pane) {
        pane.getStylesheets().add(getClass().getResource("/com/gestionap/styles.css").toExternalForm());
    }

    private Alert crearAlerta(Alert.AlertType tipo, String titulo, String header, String contenido) {
        Alert a = new Alert(tipo); a.setTitle(titulo); a.setHeaderText(header); a.setContentText(contenido);
        aplicarCSS(a.getDialogPane()); return a;
    }

    private void mostrarAviso(String msg) { crearAlerta(Alert.AlertType.WARNING, "Aviso", null, msg).showAndWait(); }
    private void mostrarError(String h, String msg) { crearAlerta(Alert.AlertType.ERROR, "Error", h, msg).showAndWait(); }
}
