package com.gestionap.controller;

import com.gestionap.dao.NotificacionDAO;
import com.gestionap.model.Notificacion;
import com.gestionap.model.Usuario;
import com.gestionap.utils.Session;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;

public class MainController implements Initializable {

    public static MainController instance;

    @FXML private BorderPane borderPane;
    @FXML private StackPane  panelCentro;
    @FXML private Button     btnAtras;

    @FXML private Button    btnDashboard;
    @FXML private Button    btnHabitaciones;
    @FXML private Button    btnInquilinos;
    @FXML private Button    btnContratos;
    @FXML private Button    btnPagos;
    @FXML private Button    btnCalendario;
    @FXML private Button    btnIncidencias;
    @FXML private Button    btnEstadisticas;
    @FXML private Button    btnUsuarios;
    @FXML private Button    btnCerrarSesion;
    @FXML private Button    btnToggleTema;
    @FXML private Button    btnNotificaciones;
    @FXML private Label     lblBadgeNotif;
    @FXML private Button    btnPerfil;
    @FXML private Button    btnRentabilidad;
    @FXML private Button    btnAnalisisPisos;
    @FXML private TextField txtBusquedaGlobal;

    @FXML private Label lblUsuarioNombre;
    @FXML private Label lblUsuarioRol;
    @FXML private Label lblIniciales;

    private static final String CSS_OSCURO = "/com/gestionap/styles.css";
    private static final String CSS_CLARO  = "/com/gestionap/styles-light.css";

    private List<Button>          botonesNav;
    private NotificacionDAO       notificacionDAO;
    private boolean               modoClaro = false;
    private Node                  vistaActual;

    private final Stack<String> historialPaths   = new Stack<>();
    private final Stack<Button> historialBotones = new Stack<>();
    private String              vistaActualPath  = null;
    private Button              botonActual      = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instance = this;
        notificacionDAO = new NotificacionDAO();
        botonesNav = List.of(btnDashboard, btnHabitaciones, btnInquilinos,
                             btnContratos, btnPagos, btnCalendario, btnIncidencias,
                             btnEstadisticas, btnUsuarios, btnPerfil,
                             btnRentabilidad, btnAnalisisPisos);
        initGUI();
        actions();
        configurarCierreVentana();
        configurarAtajosTeclado();
    }

    private void initGUI() {
        Usuario usuario = Session.getInstance().getUsuarioActual();
        if (usuario != null) {
            lblUsuarioNombre.setText(usuario.getNombre());
            lblUsuarioRol.setText(usuario.getRol().name());
            lblUsuarioRol.getStyleClass().clear();
            lblUsuarioRol.getStyleClass().addAll("badge", "badge-" + usuario.getRol().name());
            lblIniciales.setText(usuario.getIniciales());
            // Solo admin ve Usuarios
            boolean esAdmin = usuario.getRol() == Usuario.Rol.admin;
            btnUsuarios.setVisible(esAdmin);
            btnUsuarios.setManaged(esAdmin);
            // Generar notificaciones en background
            int uid = usuario.getIdUsuario();
            new Thread(() -> {
                notificacionDAO.generarNotificaciones(uid);
                Platform.runLater(this::actualizarBadgeNotificaciones);
            }).start();
        }
        btnAtras.setVisible(false);
        btnAtras.setManaged(false);
        btnAtras.setOnAction(e -> volverAtras());
        cargarVista("/com/gestionap/dashboard-view.fxml", btnDashboard);
        actualizarBadgeIncidencias();
    }

    private void actions() {
        btnDashboard.setOnAction(e    -> cargarVista("/com/gestionap/dashboard-view.fxml",    btnDashboard));
        btnHabitaciones.setOnAction(e -> cargarVista("/com/gestionap/habitaciones-view.fxml", btnHabitaciones));
        btnInquilinos.setOnAction(e   -> cargarVista("/com/gestionap/inquilinos-view.fxml",   btnInquilinos));
        btnContratos.setOnAction(e    -> cargarVista("/com/gestionap/contratos-view.fxml",    btnContratos));
        btnPagos.setOnAction(e        -> cargarVista("/com/gestionap/pagos-view.fxml",        btnPagos));
        btnCalendario.setOnAction(e   -> cargarVista("/com/gestionap/calendario-view.fxml",   btnCalendario));
        btnIncidencias.setOnAction(e  -> cargarVista("/com/gestionap/incidencias-view.fxml",  btnIncidencias));
        btnRentabilidad.setOnAction(e -> cargarVista("/com/gestionap/rentabilidad-view.fxml",   btnRentabilidad));
        btnAnalisisPisos.setOnAction(e -> cargarVista("/com/gestionap/analisis-pisos-view.fxml", btnAnalisisPisos));
        btnEstadisticas.setOnAction(e -> cargarVista("/com/gestionap/estadisticas-view.fxml", btnEstadisticas));
        btnUsuarios.setOnAction(e     -> cargarVista("/com/gestionap/usuarios-view.fxml",     btnUsuarios));
        btnPerfil.setOnAction(e       -> cargarVista("/com/gestionap/perfil-view.fxml",       btnPerfil));
        btnCerrarSesion.setOnAction(e -> cerrarSesion());
        btnToggleTema.setOnAction(e   -> toggleTema());
        btnNotificaciones.setOnAction(e -> mostrarPanelNotificaciones());

        txtBusquedaGlobal.textProperty().addListener((obs, old, texto) -> {
            if (!texto.isBlank()) navegarInquilinosConBusqueda(texto.trim());
        });
    }

    // ── Cierre de ventana con confirmación ──────────────────────

    private void configurarCierreVentana() {
        borderPane.sceneProperty().addListener((obs, old, scene) -> {
            if (scene != null) {
                scene.windowProperty().addListener((obs2, old2, win) -> {
                    if (win instanceof Stage stage) {
                        stage.setOnCloseRequest(event -> {
                            if (!confirmarSalida()) event.consume();
                        });
                    }
                });
            }
        });
    }

    private boolean confirmarSalida() {
        int noLeidas = 0;
        try {
            Usuario u = Session.getInstance().getUsuarioActual();
            if (u != null) noLeidas = notificacionDAO.contarNoLeidas(u.getIdUsuario());
        } catch (Exception ignored) {}

        String contenido = noLeidas > 0
                ? "Tienes " + noLeidas + " notificación(es) sin leer.\n¿Seguro que quieres salir de GestionAp?"
                : "¿Estás seguro de que quieres salir de GestionAp?";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Salir de GestionAp");
        alert.setHeaderText("Confirmar salida");
        alert.setContentText(contenido);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/gestionap/styles.css").toExternalForm());
        ButtonType btnSalir    = new ButtonType("Salir", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnSalir, btnCancelar);
        return alert.showAndWait().filter(b -> b == btnSalir).isPresent();
    }

    // ── Atajos de teclado ───────────────────────────────────────

    private void configurarAtajosTeclado() {
        Platform.runLater(() -> {
            Scene scene = borderPane.getScene();
            if (scene == null) return;
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN),
                    () -> cargarVista("/com/gestionap/habitaciones-view.fxml", btnHabitaciones));
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN),
                    () -> cargarVista("/com/gestionap/inquilinos-view.fxml", btnInquilinos));
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN),
                    () -> cargarVista("/com/gestionap/contratos-view.fxml", btnContratos));
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.P, KeyCombination.CONTROL_DOWN),
                    () -> cargarVista("/com/gestionap/pagos-view.fxml", btnPagos));
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                    () -> cargarVista("/com/gestionap/incidencias-view.fxml", btnIncidencias));
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN),
                    () -> { if (btnUsuarios.isVisible()) cargarVista("/com/gestionap/usuarios-view.fxml", btnUsuarios); });
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN),
                    () -> cargarVista("/com/gestionap/estadisticas-view.fxml", btnEstadisticas));
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
                    () -> txtBusquedaGlobal.requestFocus());
            scene.getAccelerators().put(
                    new KeyCodeCombination(KeyCode.ESCAPE),
                    () -> cargarVista("/com/gestionap/dashboard-view.fxml", btnDashboard));
        });
    }

    // ── Carga de vistas ─────────────────────────────────────────

    private void cargarVista(String fxmlPath, Button boton) {
        if (vistaActualPath != null && !fxmlPath.equals(vistaActualPath)) {
            historialPaths.push(vistaActualPath);
            historialBotones.push(botonActual);
        }
        cargarVistaInternal(fxmlPath, boton);
        actualizarBtnAtras();
    }

    private void cargarVistaInternal(String fxmlPath, Button boton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node vista = loader.load();
            aplicarTemaAVista(vista);
            vistaActual = vista;
            vistaActualPath = fxmlPath;
            botonActual = boton;
            panelCentro.getChildren().setAll(vista);
            botonesNav.forEach(b -> b.getStyleClass().remove("active"));
            if (boton != null && !boton.getStyleClass().contains("active"))
                boton.getStyleClass().add("active");
        } catch (Exception ex) {
            mostrarErrorCarga(ex);
        }
    }

    public void volverAtras() {
        if (!historialPaths.isEmpty()) {
            String path  = historialPaths.pop();
            Button boton = historialBotones.pop();
            cargarVistaInternal(path, boton);
            actualizarBtnAtras();
        }
    }

    public void pushCurrentToHistorial() {
        if (vistaActualPath != null) {
            historialPaths.push(vistaActualPath);
            historialBotones.push(botonActual);
            vistaActualPath = null;
            botonActual = null;
            actualizarBtnAtras();
        }
    }

    private void actualizarBtnAtras() {
        boolean hay = !historialPaths.isEmpty();
        btnAtras.setVisible(hay);
        btnAtras.setManaged(hay);
    }

    private void navegarInquilinosConBusqueda(String texto) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionap/inquilinos-view.fxml"));
            Node vista = loader.load();
            aplicarTemaAVista(vista);
            vistaActual = vista;
            vistaActualPath = "/com/gestionap/inquilinos-view.fxml";
            botonActual = btnInquilinos;
            panelCentro.getChildren().setAll(vista);
            botonesNav.forEach(b -> b.getStyleClass().remove("active"));
            if (!btnInquilinos.getStyleClass().contains("active"))
                btnInquilinos.getStyleClass().add("active");
            InquilinosController ctrl = loader.getController();
            ctrl.filtrar(texto);
        } catch (Exception ex) { mostrarErrorCarga(ex); }
    }

    // ── Tema ────────────────────────────────────────────────────

    private void toggleTema() {
        modoClaro = !modoClaro;
        String css = modoClaro ? CSS_CLARO : CSS_OSCURO;
        btnToggleTema.setText(modoClaro ? "☽  Tema Oscuro" : "☀  Tema Claro");
        borderPane.getStylesheets().clear();
        borderPane.getStylesheets().add(getClass().getResource(css).toExternalForm());
        aplicarTemaAVista(vistaActual);
    }

    public void aplicarTemaAVista(Node vista) {
        if (vista instanceof Parent p) {
            String css = modoClaro ? CSS_CLARO : CSS_OSCURO;
            p.getStylesheets().clear();
            p.getStylesheets().add(getClass().getResource(css).toExternalForm());
        }
    }

    // ── Notificaciones ──────────────────────────────────────────

    private void actualizarBadgeNotificaciones() {
        try {
            Usuario u = Session.getInstance().getUsuarioActual();
            if (u == null) { lblBadgeNotif.setVisible(false); return; }
            int total = notificacionDAO.contarNoLeidas(u.getIdUsuario());
            if (total > 0) {
                lblBadgeNotif.setText(String.valueOf(Math.min(total, 99)));
                lblBadgeNotif.setVisible(true);
            } else {
                lblBadgeNotif.setVisible(false);
            }
        } catch (Exception ex) {
            lblBadgeNotif.setVisible(false);
        }
    }

    private void mostrarPanelNotificaciones() {
        Usuario u = Session.getInstance().getUsuarioActual();
        if (u == null) return;

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox panel = new VBox(0);
        panel.setStyle("-fx-background-color: #0f3460; -fx-border-color: #2a4a7a; " +
                "-fx-border-width: 1; -fx-border-radius: 10; -fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.6),20,0,0,4);");
        panel.setPrefWidth(360);

        // Cabecera
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #16213e; -fx-padding: 10 16 10 16; " +
                "-fx-background-radius: 10 10 0 0;");
        Label titulo = new Label("  Notificaciones");
        titulo.setStyle("-fx-text-fill: #e8e8f8; -fx-font-weight: bold; -fx-font-size: 13px;");
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnMarcar = new Button("✓ Marcar leídas");
        btnMarcar.setStyle("-fx-background-color: #1e3050; -fx-text-fill: #64648a; " +
                "-fx-font-size: 10px; -fx-padding: 4 8 4 8; -fx-background-radius: 6; -fx-cursor: hand;");
        header.getChildren().addAll(titulo, spacer, btnMarcar);
        panel.getChildren().add(header);

        VBox items = new VBox(0);
        boolean hayNotifs = false;

        try {
            List<Notificacion> notifs = notificacionDAO.listarNoLeidas(u.getIdUsuario());
            for (Notificacion n : notifs) {
                items.getChildren().add(itemNotif(n, () -> {
                    try { notificacionDAO.marcarLeida(n.getId()); } catch (Exception ignored) {}
                    navegarPorTipo(n.getTipo());
                    popup.hide();
                    actualizarBadgeNotificaciones();
                }));
                hayNotifs = true;
            }

            btnMarcar.setOnAction(e -> {
                try {
                    notificacionDAO.marcarTodasLeidas(u.getIdUsuario());
                    popup.hide();
                    actualizarBadgeNotificaciones();
                } catch (Exception ignored) {}
            });
        } catch (Exception ex) {
            items.getChildren().add(itemSimple("Error al cargar notificaciones", "#e74c3c", null));
            hayNotifs = true;
        }

        if (!hayNotifs) {
            Label vacio = new Label("✅  Sin notificaciones pendientes");
            vacio.setStyle("-fx-text-fill: #2ecc71; -fx-padding: 16; -fx-font-size: 12px;");
            items.getChildren().add(vacio);
        }

        ScrollPane scroll = new ScrollPane(items);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setMaxHeight(360);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        panel.getChildren().add(scroll);

        popup.getContent().add(panel);
        Bounds b = btnNotificaciones.localToScreen(btnNotificaciones.getBoundsInLocal());
        popup.show(btnNotificaciones.getScene().getWindow(), b.getMaxX() - 360, b.getMaxY() + 6);
    }

    private Node itemNotif(Notificacion n, Runnable onClick) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 10 16 10 16; -fx-background-color: transparent; -fx-cursor: hand;");

        Label icono = new Label(n.getIcono());
        icono.setStyle("-fx-font-size: 16px;");

        VBox detalle = new VBox(2);
        Label titulo = new Label(n.getTitulo());
        titulo.setStyle("-fx-text-fill: " + n.getColor() + "; -fx-font-size: 12px; -fx-font-weight: bold;");
        titulo.setWrapText(true);
        titulo.setPrefWidth(280);
        String desc = n.getDescripcion();
        if (desc != null && !desc.isBlank()) {
            Label d = new Label(desc);
            d.setStyle("-fx-text-fill: #8888aa; -fx-font-size: 11px;");
            d.setWrapText(true);
            d.setPrefWidth(280);
            detalle.getChildren().addAll(titulo, d);
        } else {
            detalle.getChildren().add(titulo);
        }
        Label fecha = new Label(n.getFechaFormateada());
        fecha.setStyle("-fx-text-fill: #44648a; -fx-font-size: 10px;");
        detalle.getChildren().add(fecha);

        item.getChildren().addAll(icono, detalle);
        item.setOnMouseEntered(e -> item.setStyle("-fx-padding: 10 16 10 16; -fx-background-color: #1e3050; -fx-cursor: hand;"));
        item.setOnMouseExited(e  -> item.setStyle("-fx-padding: 10 16 10 16; -fx-background-color: transparent; -fx-cursor: hand;"));
        if (onClick != null) item.setOnMouseClicked(e -> onClick.run());
        return item;
    }

    private Node itemSimple(String texto, String color, Runnable onClick) {
        HBox item = new HBox();
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-padding: 11 16 11 16; -fx-background-color: transparent;");
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
        item.getChildren().add(lbl);
        return item;
    }

    private void navegarPorTipo(String tipo) {
        if (tipo == null) return;
        switch (tipo) {
            case "contrato_vence", "pago_pendiente" ->
                    cargarVista("/com/gestionap/contratos-view.fxml", btnContratos);
            case "incidencia_antigua" ->
                    cargarVista("/com/gestionap/incidencias-view.fxml", btnIncidencias);
            case "mantenimiento_largo" ->
                    cargarVista("/com/gestionap/habitaciones-view.fxml", btnHabitaciones);
        }
    }

    // ── Badge incidencias ────────────────────────────────────────

    private void actualizarBadgeIncidencias() {
        try {
            com.gestionap.dao.DashboardDAO dao = new com.gestionap.dao.DashboardDAO();
            int pendientes = dao.contarIncidenciasPendientes();
            if (pendientes > 0)
                btnIncidencias.setText("🔧  Incidencias  [" + pendientes + "]");
        } catch (Exception ignored) {}
    }

    // ── Sesión ───────────────────────────────────────────────────

    private void cerrarSesion() {
        Session.getInstance().cerrarSesion();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionap/login-view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            scene.getStylesheets().add(
                    getClass().getResource("/com/gestionap/styles.css").toExternalForm());
            Stage stage = (Stage) borderPane.getScene().getWindow();
            stage.setOnCloseRequest(null);
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

    private void mostrarErrorCarga(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error al cargar pantalla");
        alert.setHeaderText("No se pudo cargar la vista solicitada");
        alert.setContentText(ex.getMessage());
        try {
            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/com/gestionap/styles.css").toExternalForm());
        } catch (Exception ignored) {}
        alert.showAndWait();
    }
}
