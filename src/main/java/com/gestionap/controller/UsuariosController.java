package com.gestionap.controller;

import com.gestionap.dao.ActividadDAO;
import com.gestionap.dao.UsuarioDAO;
import com.gestionap.model.Usuario;
import com.gestionap.utils.PasswordUtil;
import com.gestionap.utils.Session;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

public class UsuariosController implements Initializable {

    @FXML private TableView<Usuario>              tablaUsuarios;
    @FXML private TableColumn<Usuario, Integer>   colId;
    @FXML private TableColumn<Usuario, String>    colNombre;
    @FXML private TableColumn<Usuario, String>    colEmail;
    @FXML private TableColumn<Usuario, String>    colRol;
    @FXML private TableColumn<Usuario, String>    colActivo;
    @FXML private Button                          btnNuevoUsuario;
    @FXML private Button                          btnActivar;
    @FXML private Button                          btnCambiarRol;

    private UsuarioDAO                 usuarioDAO;
    private ActividadDAO               actividadDAO;
    private ObservableList<Usuario>    listaUsuarios;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usuarioDAO   = new UsuarioDAO();
        actividadDAO = new ActividadDAO();
        listaUsuarios = FXCollections.observableArrayList();
        initGUI();
        actions();
    }

    private void initGUI() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colRol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRol().name()));
        colRol.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String rol, boolean empty) {
                super.updateItem(rol, empty);
                if (empty || rol == null) { setGraphic(null); return; }
                Label b = new Label(rol);
                b.getStyleClass().addAll("badge", "badge-" + rol);
                setGraphic(b); setText(null);
            }
        });

        colActivo.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActivo() ? "activo" : "inactivo"));
        colActivo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setGraphic(null); return; }
                Label b = new Label(estado);
                b.getStyleClass().addAll("badge",
                        "activo".equals(estado) ? "badge-activo" : "badge-finalizado");
                setGraphic(b); setText(null);
            }
        });

        tablaUsuarios.setPlaceholder(new Label("No hay usuarios registrados."));
        tablaUsuarios.setItems(listaUsuarios);
        cargarDatos();
    }

    private void actions() {
        btnNuevoUsuario.setOnAction(e -> abrirDialogoNuevoUsuario());
        btnActivar.setOnAction(e      -> toggleActivar());
        btnCambiarRol.setOnAction(e   -> cambiarRol());
    }

    private void cargarDatos() {
        try {
            listaUsuarios.setAll(usuarioDAO.listarTodos());
        } catch (Exception ex) {
            mostrarError("Error al cargar usuarios", ex.getMessage());
        }
    }

    private void abrirDialogoNuevoUsuario() {
        Dialog<Usuario> dialog = new Dialog<>();
        dialog.setTitle("Nuevo Usuario");
        dialog.setHeaderText("Crear una nueva cuenta de acceso");
        aplicarCSS(dialog.getDialogPane());

        ButtonType btnGuardar = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(4, 0, 4, 0));

        TextField     tfNombre = new TextField();
        TextField     tfEmail  = new TextField();
        PasswordField pfPass   = new PasswordField();
        ComboBox<String> cmbRol = new ComboBox<>(
                FXCollections.observableArrayList("usuario", "admin"));

        tfNombre.setPromptText("Nombre completo");
        tfEmail.setPromptText("correo@ejemplo.com");
        pfPass.setPromptText("Contraseña (mín. 6 caracteres)");
        cmbRol.setValue("usuario");

        for (var c : new Control[]{tfNombre, tfEmail, pfPass, cmbRol}) c.setPrefWidth(280);

        grid.add(labelForm("Nombre *:"),   0, 0); grid.add(tfNombre, 1, 0);
        grid.add(labelForm("Email *:"),    0, 1); grid.add(tfEmail,  1, 1);
        grid.add(labelForm("Contraseña *:"),0,2); grid.add(pfPass,   1, 2);
        grid.add(labelForm("Rol *:"),      0, 3); grid.add(cmbRol,   1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            if (tfNombre.getText().isBlank() || tfEmail.getText().isBlank()) return null;
            if (pfPass.getText().length() < 6) return null;
            Usuario u = new Usuario();
            u.setNombre(tfNombre.getText().trim());
            u.setEmail(tfEmail.getText().trim());
            u.setPasswordHash(PasswordUtil.hash(pfPass.getText()));
            u.setRol(Usuario.Rol.valueOf(cmbRol.getValue()));
            return u;
        });

        dialog.showAndWait().ifPresent(u -> {
            try {
                usuarioDAO.insertar(u, u.getPasswordHash());
                int uid = Session.getInstance().getUsuarioActual().getIdUsuario();
                actividadDAO.registrar(uid, "CREAR_USUARIO",
                        "Usuario creado: " + u.getNombre() + " (" + u.getRol().name() + ")");
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo crear el usuario", ex.getMessage());
            }
        });
    }

    private void toggleActivar() {
        Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un usuario."); return; }
        Usuario actual = Session.getInstance().getUsuarioActual();
        if (actual != null && actual.getIdUsuario() == sel.getIdUsuario()) {
            mostrarAviso("No puedes desactivar tu propia cuenta.");
            return;
        }
        boolean nuevoEstado = !sel.isActivo();
        String accion = nuevoEstado ? "activar" : "desactivar";
        Alert confirm = crearAlerta(Alert.AlertType.CONFIRMATION,
                "Confirmar", "¿" + accion.substring(0,1).toUpperCase() + accion.substring(1) +
                " a " + sel.getNombre() + "?", null);
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                usuarioDAO.activarDesactivar(sel.getIdUsuario(), nuevoEstado);
                int uid = Session.getInstance().getUsuarioActual().getIdUsuario();
                actividadDAO.registrar(uid, "CAMBIAR_ESTADO_USUARIO",
                        sel.getNombre() + " → " + (nuevoEstado ? "activo" : "inactivo"));
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo cambiar el estado", ex.getMessage());
            }
        });
    }

    private void cambiarRol() {
        Usuario sel = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un usuario."); return; }
        ChoiceDialog<String> dlg = new ChoiceDialog<>(sel.getRol().name(), "usuario", "admin");
        dlg.setTitle("Cambiar Rol");
        dlg.setHeaderText("Rol de " + sel.getNombre());
        dlg.setContentText("Nuevo rol:");
        aplicarCSS(dlg.getDialogPane());
        dlg.showAndWait().ifPresent(nuevoRol -> {
            try {
                usuarioDAO.actualizarRol(sel.getIdUsuario(), Usuario.Rol.valueOf(nuevoRol));
                int uid = Session.getInstance().getUsuarioActual().getIdUsuario();
                actividadDAO.registrar(uid, "CAMBIAR_ROL",
                        sel.getNombre() + " → " + nuevoRol);
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo cambiar el rol", ex.getMessage());
            }
        });
    }

    private Label labelForm(String t) {
        Label l = new Label(t); l.getStyleClass().add("form-label"); return l;
    }

    private void aplicarCSS(DialogPane pane) {
        pane.getStylesheets().add(
                getClass().getResource("/com/gestionap/styles.css").toExternalForm());
    }

    private Alert crearAlerta(Alert.AlertType tipo, String titulo, String header, String contenido) {
        Alert a = new Alert(tipo);
        a.setTitle(titulo); a.setHeaderText(header); a.setContentText(contenido);
        aplicarCSS(a.getDialogPane());
        return a;
    }

    private void mostrarAviso(String msg) {
        crearAlerta(Alert.AlertType.WARNING, "Aviso", null, msg).showAndWait();
    }

    private void mostrarError(String header, String msg) {
        crearAlerta(Alert.AlertType.ERROR, "Error", header, msg).showAndWait();
    }
}
