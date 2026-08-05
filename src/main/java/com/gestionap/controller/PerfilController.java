package com.gestionap.controller;

import com.gestionap.dao.ActividadDAO;
import com.gestionap.dao.UsuarioDAO;
import com.gestionap.model.ActividadLog;
import com.gestionap.model.Usuario;
import com.gestionap.utils.PasswordUtil;
import com.gestionap.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PerfilController implements Initializable {

    // Datos de usuario
    @FXML private Label         lblIniciales;
    @FXML private Label         lblNombre;
    @FXML private Label         lblEmail;
    @FXML private Label         lblRol;

    // Estadísticas
    @FXML private Label         lblContratosCreados;
    @FXML private Label         lblPagosRegistrados;
    @FXML private Label         lblIncidenciasResueltas;
    @FXML private Label         lblTotalAcciones;

    // Editar nombre/email
    @FXML private TextField     tfNombreEditar;
    @FXML private TextField     tfEmailEditar;
    @FXML private Button        btnGuardarPerfil;
    @FXML private Label         lblMensajePerfil;

    // Cambiar contraseña
    @FXML private PasswordField pfActual;
    @FXML private PasswordField pfNueva;
    @FXML private PasswordField pfConfirmar;
    @FXML private Button        btnGuardarPassword;
    @FXML private Label         lblMensajePassword;

    // Historial
    @FXML private VBox          vboxHistorial;

    // Navegación
    @FXML private Button        btnVolver;

    // Acciones
    @FXML private Button        btnCerrarSesion;

    private UsuarioDAO   usuarioDAO;
    private ActividadDAO actividadDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        usuarioDAO   = new UsuarioDAO();
        actividadDAO = new ActividadDAO();
        cargarDatosUsuario();
        cargarEstadisticas();
        cargarHistorial();
        btnVolver.setOnAction(e           -> { if (MainController.instance != null) MainController.instance.volverAtras(); });
        btnGuardarPerfil.setOnAction(e    -> guardarPerfil());
        btnGuardarPassword.setOnAction(e  -> cambiarPassword());
        btnCerrarSesion.setOnAction(e     -> cerrarSesion());
    }

    private void cargarDatosUsuario() {
        Usuario u = Session.getInstance().getUsuarioActual();
        if (u == null) return;
        lblIniciales.setText(u.getIniciales());
        lblNombre.setText(u.getNombre());
        lblEmail.setText(u.getEmail());
        lblRol.setText(u.getRol().name());
        lblRol.getStyleClass().clear();
        lblRol.getStyleClass().addAll("badge", "badge-" + u.getRol().name());
        tfNombreEditar.setText(u.getNombre());
        tfEmailEditar.setText(u.getEmail());
    }

    private void cargarEstadisticas() {
        Usuario u = Session.getInstance().getUsuarioActual();
        if (u == null) return;
        int id = u.getIdUsuario();
        int contratos  = actividadDAO.contarPorAccion(id, "CREAR_CONTRATO");
        int pagos      = actividadDAO.contarPorAccion(id, "REGISTRAR_PAGO");
        int incidencias = actividadDAO.contarPorAccion(id, "RESOLVER_INCIDENCIA");
        int total      = actividadDAO.contarPorAccion(id, "");
        lblContratosCreados.setText(String.valueOf(contratos));
        lblPagosRegistrados.setText(String.valueOf(pagos));
        lblIncidenciasResueltas.setText(String.valueOf(incidencias));
        lblTotalAcciones.setText(String.valueOf(total));
    }

    private void cargarHistorial() {
        Usuario u = Session.getInstance().getUsuarioActual();
        if (u == null || vboxHistorial == null) return;
        try {
            List<ActividadLog> logs = actividadDAO.listarUltimas(u.getIdUsuario(), 10);
            vboxHistorial.getChildren().clear();
            if (logs.isEmpty()) {
                Label vacio = new Label("Sin actividad registrada todavía.");
                vacio.getStyleClass().add("info-label");
                vboxHistorial.getChildren().add(vacio);
                return;
            }
            for (ActividadLog log : logs) {
                HBox item = new HBox(10);
                item.setAlignment(Pos.CENTER_LEFT);
                item.getStyleClass().add("activity-item");

                Label icono = new Label(log.getIcono());
                icono.setStyle("-fx-font-size: 16px;");

                VBox detalle = new VBox(2);
                Label accion = new Label(log.getDescripcion() != null ? log.getDescripcion() : log.getAccion());
                accion.setStyle("-fx-text-fill: #c8c8e8; -fx-font-size: 13px;");
                Label fecha = new Label(log.getFechaFormateada());
                fecha.setStyle("-fx-text-fill: #44648a; -fx-font-size: 11px;");
                detalle.getChildren().addAll(accion, fecha);

                item.getChildren().addAll(icono, detalle);
                vboxHistorial.getChildren().add(item);
            }
        } catch (Exception ex) {
            Label err = new Label("No se pudo cargar el historial.");
            err.getStyleClass().add("info-label");
            vboxHistorial.getChildren().add(err);
        }
    }

    private void guardarPerfil() {
        String nombre = tfNombreEditar.getText().trim();
        String email  = tfEmailEditar.getText().trim();
        if (nombre.isBlank() || email.isBlank()) {
            mostrarMensajePerfil("Nombre y email son obligatorios.", false);
            return;
        }
        if (!email.contains("@")) {
            mostrarMensajePerfil("Email no válido.", false);
            return;
        }
        try {
            Usuario u = Session.getInstance().getUsuarioActual();
            usuarioDAO.actualizarPerfil(u.getIdUsuario(), nombre, email);
            u.setNombre(nombre);
            u.setEmail(email);
            actividadDAO.registrar(u.getIdUsuario(), "CAMBIAR_PERFIL",
                    "Perfil actualizado: " + nombre + " / " + email);
            cargarDatosUsuario();
            mostrarMensajePerfil("¡Perfil actualizado correctamente!", true);
        } catch (Exception ex) {
            mostrarMensajePerfil("Error: " + ex.getMessage(), false);
        }
    }

    private void cambiarPassword() {
        String actual    = pfActual.getText();
        String nueva     = pfNueva.getText();
        String confirmar = pfConfirmar.getText();

        if (actual.isBlank() || nueva.isBlank() || confirmar.isBlank()) {
            mostrarMensajePassword("Completa todos los campos.", false);
            return;
        }
        if (!nueva.equals(confirmar)) {
            mostrarMensajePassword("Las contraseñas nuevas no coinciden.", false);
            return;
        }
        if (nueva.length() < 6) {
            mostrarMensajePassword("La contraseña debe tener al menos 6 caracteres.", false);
            return;
        }
        try {
            Usuario u = Session.getInstance().getUsuarioActual();
            if (usuarioDAO.autenticar(u.getEmail(), PasswordUtil.sha256(actual)) == null) {
                mostrarMensajePassword("La contraseña actual no es correcta.", false);
                return;
            }
            usuarioDAO.actualizarPassword(u.getIdUsuario(), PasswordUtil.sha256(nueva));
            actividadDAO.registrar(u.getIdUsuario(), "CAMBIAR_PASSWORD", "Contraseña cambiada");
            pfActual.clear(); pfNueva.clear(); pfConfirmar.clear();
            mostrarMensajePassword("¡Contraseña actualizada correctamente!", true);
        } catch (Exception ex) {
            mostrarMensajePassword("Error: " + ex.getMessage(), false);
        }
    }

    private void cerrarSesion() {
        Session.getInstance().cerrarSesion();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionap/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            scene.getStylesheets().add(
                    getClass().getResource("/com/gestionap/styles.css").toExternalForm());
            Stage stage = (Stage) btnCerrarSesion.getScene().getWindow();
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.setTitle("GestionAp - Iniciar Sesión");
        } catch (Exception ex) {
            Alert err = new Alert(Alert.AlertType.ERROR, "Error al cerrar sesión: " + ex.getMessage());
            err.setTitle("Error");
            err.showAndWait();
        }
    }

    private void mostrarMensajePerfil(String msg, boolean ok) {
        if (lblMensajePerfil == null) return;
        lblMensajePerfil.setText(msg);
        lblMensajePerfil.setStyle(ok
                ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold;"
                : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        lblMensajePerfil.setVisible(true);
        lblMensajePerfil.setManaged(true);
    }

    private void mostrarMensajePassword(String msg, boolean ok) {
        if (lblMensajePassword == null) return;
        lblMensajePassword.setText(msg);
        lblMensajePassword.setStyle(ok
                ? "-fx-text-fill: #2ecc71; -fx-font-weight: bold;"
                : "-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        lblMensajePassword.setVisible(true);
        lblMensajePassword.setManaged(true);
    }
}
