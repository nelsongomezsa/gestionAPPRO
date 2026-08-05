package com.gestionap.controller;

import com.gestionap.dao.ActividadDAO;
import com.gestionap.dao.HabitacionDAO;
import com.gestionap.dao.IncidenciaDAO;
import com.gestionap.dao.InquilinoDAO;
import com.gestionap.model.Habitacion;
import com.gestionap.model.Incidencia;
import com.gestionap.model.Incidencia.Estado;
import com.gestionap.model.Incidencia.Prioridad;
import com.gestionap.model.Inquilino;
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

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

public class IncidenciasController implements Initializable {

    @FXML private TableView<Incidencia>               tablaIncidencias;
    @FXML private TableColumn<Incidencia, Integer>    colId;
    @FXML private TableColumn<Incidencia, String>     colPrioridad;
    @FXML private TableColumn<Incidencia, Integer>    colHabitacion;
    @FXML private TableColumn<Incidencia, String>     colInquilino;
    @FXML private TableColumn<Incidencia, String>     colDescripcion;
    @FXML private TableColumn<Incidencia, String>     colEstado;
    @FXML private TableColumn<Incidencia, Object>     colFecha;
    @FXML private TableColumn<Incidencia, String>     colDias;
    @FXML private TableColumn<Incidencia, String>     colCoste;
    @FXML private ComboBox<String>                    cmbFiltroEstado;
    @FXML private Button                              btnNuevaIncidencia;
    @FXML private Button                              btnCambiarEstado;
    @FXML private Button                              btnEliminar;
    @FXML private ProgressIndicator                   progressIndicator;

    private IncidenciaDAO              incidenciaDAO;
    private HabitacionDAO              habitacionDAO;
    private InquilinoDAO               inquilinoDAO;
    private ActividadDAO               actividadDAO;
    private ObservableList<Incidencia> listaIncidencias;
    private FilteredList<Incidencia>   listaFiltrada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instances();
        initGUI();
        actions();
    }

    private void instances() {
        incidenciaDAO    = new IncidenciaDAO();
        habitacionDAO    = new HabitacionDAO();
        inquilinoDAO     = new InquilinoDAO();
        actividadDAO     = new ActividadDAO();
        listaIncidencias = FXCollections.observableArrayList();
        listaFiltrada    = new FilteredList<>(listaIncidencias, i -> true);
    }

    private void initGUI() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idIncidencia"));
        colHabitacion.setCellValueFactory(new PropertyValueFactory<>("numeroHabitacion"));
        colInquilino.setCellValueFactory(new PropertyValueFactory<>("nombreInquilino"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));

        colPrioridad.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getPrioridad() != null
                        ? d.getValue().getPrioridad().name() : "Media"));
        colPrioridad.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setGraphic(null); return; }
                Label badge = new Label(p);
                badge.getStyleClass().addAll("badge", "badge-prioridad-" + p.toLowerCase());
                setGraphic(badge); setText(null);
            }
        });

        colEstado.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEstado().name()));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                Label badge = new Label(v);
                badge.getStyleClass().addAll("badge", "badge-" + v);
                setGraphic(badge); setText(null);
            }
        });

        colDias.setCellValueFactory(d -> {
            LocalDate fecha = d.getValue().getFecha();
            if (fecha == null) return new SimpleStringProperty("—");
            long dias = ChronoUnit.DAYS.between(fecha, LocalDate.now());
            if (d.getValue().getEstado() == Estado.resuelta)
                return new SimpleStringProperty("Resuelta");
            return new SimpleStringProperty(dias + " d");
        });

        colCoste.setCellValueFactory(d -> {
            var coste = d.getValue().getCosteReparacion();
            if (coste == null || coste.compareTo(java.math.BigDecimal.ZERO) == 0)
                return new SimpleStringProperty("—");
            return new SimpleStringProperty(String.format("%.2f €", coste));
        });

        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos", "pendiente", "en_proceso", "resuelta"));
        cmbFiltroEstado.setValue("Todos");
        tablaIncidencias.setPlaceholder(new Label("No hay incidencias registradas."));
        tablaIncidencias.setItems(listaFiltrada);
        cargarDatos();
    }

    private void actions() {
        cmbFiltroEstado.valueProperty().addListener((obs, old, val) -> {
            listaFiltrada.setPredicate(inc -> {
                if (val == null || val.equals("Todos")) return true;
                return inc.getEstado().name().equals(val);
            });
        });

        btnNuevaIncidencia.setOnAction(e -> abrirDialogoNuevaIncidencia());
        btnCambiarEstado.setOnAction(e   -> avanzarEstadoSeleccionada());
        btnEliminar.setOnAction(e        -> eliminarSeleccionada());
    }

    private void cargarDatos() {
        progressIndicator.setVisible(true);
        tablaIncidencias.setDisable(true);
        new Thread(() -> {
            try {
                List<Incidencia> data = incidenciaDAO.listarTodas();
                Platform.runLater(() -> {
                    listaIncidencias.setAll(data);
                    progressIndicator.setVisible(false);
                    tablaIncidencias.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    tablaIncidencias.setDisable(false);
                    mostrarError("Error al cargar incidencias", ex.getMessage());
                });
            }
        }).start();
    }

    private void avanzarEstadoSeleccionada() {
        Incidencia sel = tablaIncidencias.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una incidencia para avanzar su estado."); return; }

        Estado siguiente = siguienteEstado(sel.getEstado());
        if (siguiente == null) { mostrarAviso("La incidencia ya está resuelta."); return; }

        Alert confirm = crearAlerta(Alert.AlertType.CONFIRMATION,
                "Cambiar estado",
                "¿Avanzar estado de la incidencia #" + sel.getIdIncidencia() + "?",
                sel.getEstado().name() + "  →  " + siguiente.name());
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                incidenciaDAO.actualizarEstado(sel.getIdIncidencia(), siguiente);
                if (siguiente == Estado.resuelta) {
                    uid(uid -> actividadDAO.registrar(uid, "RESOLVER_INCIDENCIA",
                            "Incidencia #" + sel.getIdIncidencia() + " resuelta"));
                }
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo cambiar el estado", ex.getMessage());
            }
        });
    }

    private Estado siguienteEstado(Estado actual) {
        return switch (actual) {
            case pendiente  -> Estado.en_proceso;
            case en_proceso -> Estado.resuelta;
            case resuelta   -> null;
        };
    }

    private void eliminarSeleccionada() {
        Incidencia sel = tablaIncidencias.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona una incidencia para eliminar."); return; }

        Alert confirm = crearAlerta(Alert.AlertType.CONFIRMATION,
                "Eliminar incidencia",
                "¿Eliminar la incidencia #" + sel.getIdIncidencia() + "?",
                sel.getDescripcion());
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                incidenciaDAO.eliminar(sel.getIdIncidencia());
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo eliminar la incidencia", ex.getMessage());
            }
        });
    }

    private void abrirDialogoNuevaIncidencia() {
        Dialog<Incidencia> dialog = new Dialog<>();
        dialog.setTitle("Nueva Incidencia");
        dialog.setHeaderText("Registrar una nueva incidencia");
        aplicarCSS(dialog.getDialogPane());

        ButtonType btnGuardar = new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(4, 0, 4, 0));

        ComboBox<Habitacion> cmbHabitacion = new ComboBox<>();
        ComboBox<Inquilino>  cmbInquilino  = new ComboBox<>();
        TextArea             taDesc        = new TextArea();
        DatePicker           dpFecha       = new DatePicker(LocalDate.now());

        cmbHabitacion.setPromptText("Seleccionar habitación");
        cmbInquilino.setPromptText("Seleccionar inquilino");
        taDesc.setPromptText("Descripción de la incidencia...");
        taDesc.setPrefRowCount(3);
        taDesc.setWrapText(true);

        for (var c : new Control[]{cmbHabitacion, cmbInquilino, taDesc, dpFecha})
            c.setPrefWidth(300);

        try {
            List<Habitacion> habs = habitacionDAO.listarTodas();
            cmbHabitacion.setItems(FXCollections.observableArrayList(habs));
            cmbHabitacion.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Habitacion h) {
                    return h == null ? "" : "Hab. " + h.getNumero() + "  —  " + h.getDireccionPiso();
                }
                public Habitacion fromString(String s) { return null; }
            });

            List<Inquilino> inquilinos = inquilinoDAO.listarTodos();
            cmbInquilino.setItems(FXCollections.observableArrayList(inquilinos));
            cmbInquilino.setConverter(new javafx.util.StringConverter<>() {
                public String toString(Inquilino i) {
                    return i == null ? "" : i.getNombreCompleto() + "  —  " + i.getDni();
                }
                public Inquilino fromString(String s) { return null; }
            });
        } catch (Exception ex) {
            mostrarError("Error al cargar datos", ex.getMessage()); return;
        }

        Label lblErr = new Label();
        lblErr.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");

        grid.add(labelForm("Habitación *:"), 0, 0); grid.add(cmbHabitacion, 1, 0);
        grid.add(labelForm("Inquilino *:"),  0, 1); grid.add(cmbInquilino,  1, 1);
        grid.add(labelForm("Descripción *:"),0, 2); grid.add(taDesc,        1, 2);
        grid.add(labelForm("Fecha *:"),      0, 3); grid.add(dpFecha,       1, 3);
        grid.add(lblErr,                     0, 4, 2, 1);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node okNode = dialog.getDialogPane().lookupButton(btnGuardar);
        okNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (cmbHabitacion.getValue() == null) {
                lblErr.setText("Selecciona una habitación."); event.consume(); return;
            }
            if (cmbInquilino.getValue() == null) {
                lblErr.setText("Selecciona un inquilino."); event.consume(); return;
            }
            if (taDesc.getText().isBlank()) {
                lblErr.setText("La descripción es obligatoria."); event.consume(); return;
            }
            if (dpFecha.getValue() == null) {
                lblErr.setText("La fecha es obligatoria."); event.consume();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            Incidencia inc = new Incidencia();
            inc.setIdHabitacion(cmbHabitacion.getValue().getIdHabitacion());
            inc.setIdInquilino(cmbInquilino.getValue().getIdInquilino());
            inc.setDescripcion(taDesc.getText().trim());
            inc.setEstado(Estado.pendiente);
            inc.setFecha(dpFecha.getValue() != null ? dpFecha.getValue() : LocalDate.now());
            return inc;
        });

        dialog.showAndWait().ifPresent(inc -> {
            try {
                incidenciaDAO.insertar(inc);
                uid(uid -> actividadDAO.registrar(uid, "CREAR_INCIDENCIA",
                        "Incidencia registrada: " + inc.getDescripcion()));
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo registrar la incidencia", ex.getMessage());
            }
        });
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

    private void uid(java.util.function.IntConsumer consumer) {
        var u = Session.getInstance().getUsuarioActual();
        if (u != null) consumer.accept(u.getIdUsuario());
    }
}
