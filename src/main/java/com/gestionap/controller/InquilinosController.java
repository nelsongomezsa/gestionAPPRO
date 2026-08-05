package com.gestionap.controller;

import com.gestionap.dao.ActividadDAO;
import com.gestionap.dao.ContratoDAO;
import com.gestionap.dao.InquilinoDAO;
import com.gestionap.model.Contrato;
import com.gestionap.model.Inquilino;
import com.gestionap.utils.EmailService;
import com.gestionap.utils.PdfExporter;
import com.gestionap.utils.Session;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class InquilinosController implements Initializable {

    @FXML private TableView<Inquilino>              tablaInquilinos;
    @FXML private TableColumn<Inquilino, Integer>   colId;
    @FXML private TableColumn<Inquilino, String>    colNombre;
    @FXML private TableColumn<Inquilino, String>    colApellidos;
    @FXML private TableColumn<Inquilino, String>    colDni;
    @FXML private TableColumn<Inquilino, String>    colTelefono;
    @FXML private TableColumn<Inquilino, String>    colEmail;
    @FXML private TextField                         txtBuscar;
    @FXML private Button                            btnAnadir;
    @FXML private Button                            btnEditar;
    @FXML private Button                            btnEliminar;
    @FXML private Button                            btnExportarPdf;
    @FXML private Button                            btnEnviarEmail;
    @FXML private Button                            btnQrPago;
    @FXML private ProgressIndicator                 progressIndicator;

    private InquilinoDAO               inquilinoDAO;
    private ContratoDAO                contratoDAO;
    private ActividadDAO               actividadDAO;
    private ObservableList<Inquilino>  listaInquilinos;
    private FilteredList<Inquilino>    listaFiltrada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instances();
        initGUI();
        actions();
    }

    private void instances() {
        inquilinoDAO    = new InquilinoDAO();
        contratoDAO     = new ContratoDAO();
        actividadDAO    = new ActividadDAO();
        listaInquilinos = FXCollections.observableArrayList();
        listaFiltrada   = new FilteredList<>(listaInquilinos, i -> true);
    }

    private void initGUI() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idInquilino"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        tablaInquilinos.setPlaceholder(new Label("No hay inquilinos registrados."));
        tablaInquilinos.setItems(listaFiltrada);
        cargarDatos();
    }

    private void actions() {
        txtBuscar.textProperty().addListener((obs, old, texto) -> {
            String lower = texto == null ? "" : texto.toLowerCase();
            listaFiltrada.setPredicate(i -> {
                if (lower.isBlank()) return true;
                return i.getNombreCompleto().toLowerCase().contains(lower)
                    || i.getDni().toLowerCase().contains(lower)
                    || (i.getEmail()    != null && i.getEmail().toLowerCase().contains(lower))
                    || (i.getTelefono() != null && i.getTelefono().contains(lower));
            });
        });

        btnAnadir.setOnAction(e       -> abrirDialogoInquilino(null));
        btnEditar.setOnAction(e       -> editarSeleccionado());
        btnEliminar.setOnAction(e     -> eliminarSeleccionado());
        btnExportarPdf.setOnAction(e  -> exportarPdf());
        btnEnviarEmail.setOnAction(e  -> enviarEmail());
        btnQrPago.setOnAction(e       -> generarQrPago());
    }

    private void cargarDatos() {
        progressIndicator.setVisible(true);
        tablaInquilinos.setDisable(true);
        new Thread(() -> {
            try {
                var datos = inquilinoDAO.listarTodos();
                Platform.runLater(() -> {
                    listaInquilinos.setAll(datos);
                    progressIndicator.setVisible(false);
                    tablaInquilinos.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    progressIndicator.setVisible(false);
                    tablaInquilinos.setDisable(false);
                    mostrarError("Error al cargar inquilinos", ex.getMessage());
                });
            }
        }).start();
    }

    private void editarSeleccionado() {
        Inquilino sel = tablaInquilinos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un inquilino para editar."); return; }
        abrirDialogoInquilino(sel);
    }

    private void eliminarSeleccionado() {
        Inquilino sel = tablaInquilinos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un inquilino para eliminar."); return; }

        Alert confirm = crearAlerta(Alert.AlertType.CONFIRMATION,
                "Confirmar eliminación",
                "¿Eliminar a " + sel.getNombreCompleto() + "?",
                "Se eliminarán también sus contratos e incidencias asociadas.");
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
            try {
                inquilinoDAO.eliminar(sel.getIdInquilino());
                uid(uid -> actividadDAO.registrar(uid, "ELIMINAR_INQUILINO",
                        "Eliminado: " + sel.getNombreCompleto()));
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo eliminar", ex.getMessage());
            }
        });
    }

    private void abrirDialogoInquilino(Inquilino inquilino) {
        boolean esNuevo = (inquilino == null);

        Dialog<Inquilino> dialog = new Dialog<>();
        dialog.setTitle(esNuevo ? "Nuevo Inquilino" : "Editar Inquilino");
        dialog.setHeaderText(esNuevo ? "Datos del nuevo inquilino"
                                     : "Modificar datos de " + inquilino.getNombreCompleto());
        aplicarCSS(dialog.getDialogPane());

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(14); grid.setVgap(12);
        grid.setPadding(new Insets(4, 0, 4, 0));

        TextField tfNombre    = new TextField();
        TextField tfApellidos = new TextField();
        TextField tfDni       = new TextField();
        TextField tfTelefono  = new TextField();
        TextField tfEmail     = new TextField();

        tfNombre.setPromptText("Nombre *");
        tfApellidos.setPromptText("Apellidos *");
        tfDni.setPromptText("DNI (ej: 12345678A) *");
        tfTelefono.setPromptText("Teléfono");
        tfEmail.setPromptText("correo@ejemplo.com");

        for (var tf : new TextField[]{tfNombre, tfApellidos, tfDni, tfTelefono, tfEmail})
            tf.setPrefWidth(280);

        if (!esNuevo) {
            tfNombre.setText(inquilino.getNombre());
            tfApellidos.setText(inquilino.getApellidos());
            tfDni.setText(inquilino.getDni());
            tfTelefono.setText(inquilino.getTelefono() != null ? inquilino.getTelefono() : "");
            tfEmail.setText(inquilino.getEmail() != null ? inquilino.getEmail() : "");
        }

        Label lblErr = new Label();
        lblErr.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");

        grid.add(labelForm("Nombre *:"),    0, 0); grid.add(tfNombre,    1, 0);
        grid.add(labelForm("Apellidos *:"), 0, 1); grid.add(tfApellidos, 1, 1);
        grid.add(labelForm("DNI *:"),       0, 2); grid.add(tfDni,       1, 2);
        grid.add(labelForm("Teléfono:"),    0, 3); grid.add(tfTelefono,  1, 3);
        grid.add(labelForm("Email:"),       0, 4); grid.add(tfEmail,     1, 4);
        grid.add(lblErr,                    0, 5, 2, 1);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node okNode = dialog.getDialogPane().lookupButton(btnGuardar);
        okNode.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String nombre    = tfNombre.getText().trim();
            String apellidos = tfApellidos.getText().trim();
            String dni       = tfDni.getText().trim().toUpperCase();
            String telefono  = tfTelefono.getText().trim();
            String email     = tfEmail.getText().trim();
            if (nombre.isBlank()) {
                lblErr.setText("El nombre es obligatorio."); event.consume(); return;
            }
            if (apellidos.isBlank()) {
                lblErr.setText("Los apellidos son obligatorios."); event.consume(); return;
            }
            if (!dni.matches("[0-9]{8}[A-Z]")) {
                lblErr.setText("DNI inválido. Formato: 8 dígitos + letra (ej: 12345678A)."); event.consume(); return;
            }
            if (!telefono.isBlank() && !telefono.matches("\\+?[0-9]{9,15}")) {
                lblErr.setText("Teléfono inválido. Introduce solo dígitos (9-15)."); event.consume(); return;
            }
            if (!email.isBlank() && !email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
                lblErr.setText("Formato de email inválido."); event.consume();
            }
        });

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            String nombre    = tfNombre.getText().trim();
            String apellidos = tfApellidos.getText().trim();
            String dni       = tfDni.getText().trim().toUpperCase();
            if (nombre.isBlank() || apellidos.isBlank() || !dni.matches("[0-9]{8}[A-Z]")) return null;

            Inquilino i = esNuevo ? new Inquilino() : inquilino;
            i.setNombre(nombre);
            i.setApellidos(apellidos);
            i.setDni(dni);
            i.setTelefono(tfTelefono.getText().trim());
            i.setEmail(tfEmail.getText().trim());
            return i;
        });

        dialog.showAndWait().ifPresent(i -> {
            try {
                if (esNuevo) {
                    inquilinoDAO.insertar(i);
                    uid(uid -> actividadDAO.registrar(uid, "CREAR_INQUILINO",
                            "Nuevo inquilino: " + i.getNombreCompleto()));
                } else {
                    inquilinoDAO.actualizar(i);
                    uid(uid -> actividadDAO.registrar(uid, "EDITAR_INQUILINO",
                            "Editado: " + i.getNombreCompleto()));
                }
                cargarDatos();
            } catch (Exception ex) {
                mostrarError("No se pudo guardar el inquilino", ex.getMessage());
            }
        });
    }

    private void generarQrPago() {
        Inquilino sel = tablaInquilinos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un inquilino para generar el QR."); return; }

        Contrato contrato;
        try {
            contrato = contratoDAO.listarActivos().stream()
                    .filter(c -> c.getIdInquilino() == sel.getIdInquilino())
                    .findFirst().orElse(null);
        } catch (Exception ex) {
            mostrarError("Error al buscar contratos", ex.getMessage());
            return;
        }
        if (contrato == null) {
            mostrarAviso("El inquilino no tiene contrato activo.");
            return;
        }

        String mes      = YearMonth.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES")));
        String concepto = "Alquiler Hab." + contrato.getNumeroHabitacion()
                        + " " + sel.getNombreCompleto() + " " + mes;
        String importe  = "EUR" + contrato.getPrecioMensual().setScale(2).toPlainString();
        String qrData   = String.join("\n", "BCD", "001", "1", "SCT",
                "CAIXESBBXXX", "GestionAp", "ES0000000000000000000000",
                importe, "", "", concepto, "");

        int size = 300;
        BitMatrix bitMatrix;
        WritableImage fxImage;
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 2);
            bitMatrix = new QRCodeWriter().encode(qrData, BarcodeFormat.QR_CODE, size, size, hints);
            fxImage   = bitMatrixToImage(bitMatrix, size);
        } catch (Exception ex) {
            mostrarError("Error al generar QR", ex.getMessage());
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("QR de Pago");
        dialog.setHeaderText("QR de pago — " + sel.getNombreCompleto());
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/com/gestionap/styles.css").toExternalForm());

        VBox content = new VBox(12);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(16));

        ImageView imgView = new ImageView(fxImage);
        imgView.setFitWidth(300);
        imgView.setFitHeight(300);
        imgView.setSmooth(false);

        Label lblInfo = new Label(
                "Beneficiario: GestionAp\n" +
                "IBAN:         ES00 0000 0000 0000 0000 0000\n" +
                "BIC:          CAIXESBBXXX\n" +
                "Importe:      " + contrato.getPrecioMensual() + " €\n" +
                "Concepto:     " + concepto);
        lblInfo.setStyle("-fx-text-fill: #c8c8e8; -fx-font-size: 11px; -fx-font-family: 'Courier New', monospace;");

        BitMatrix bm = bitMatrix;
        String    nombre = sel.getNombreCompleto();
        Button btnGuardar = new Button("Guardar QR como PNG");
        btnGuardar.getStyleClass().add("btn-primary");
        btnGuardar.setOnAction(e -> guardarQrPng(bm, nombre));

        content.getChildren().addAll(imgView, lblInfo, btnGuardar);
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private WritableImage bitMatrixToImage(BitMatrix matrix, int size) {
        WritableImage img = new WritableImage(size, size);
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < size; y++)
            for (int x = 0; x < size; x++)
                pw.setColor(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
        return img;
    }

    private void guardarQrPng(BitMatrix bitMatrix, String nombreInquilino) {
        String safe = nombreInquilino.replaceAll("[^\\w\\-. ]", "").replaceAll("\\s+", "_");
        Path ruta   = Paths.get(System.getProperty("user.home"), "Desktop", "QR_" + safe + ".png");
        try {
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", ruta);
            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("QR guardado");
            ok.setHeaderText("Archivo guardado en el Escritorio");
            ok.setContentText(ruta.toString());
            ok.getDialogPane().getStylesheets().add(
                    getClass().getResource("/com/gestionap/styles.css").toExternalForm());
            ok.showAndWait();
        } catch (Exception ex) {
            mostrarError("Error al guardar QR", ex.getMessage());
        }
    }

    public void filtrar(String texto) {
        txtBuscar.setText(texto);
    }

    private void enviarEmail() {
        Inquilino sel = tablaInquilinos.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarAviso("Selecciona un inquilino para enviar el email."); return; }
        String asunto = "Información de arrendamiento - " + sel.getNombreCompleto();
        String cuerpo = "Estimado/a " + sel.getNombreCompleto() + ",\n\n" +
                "Nos ponemos en contacto con usted en relación a su arrendamiento.\n\n" +
                "Quedo a su disposición para cualquier consulta.\n\nEquipo GestionAp";
        EmailService.mostrarDialogo(sel.getEmail(), asunto, cuerpo);
    }

    private void exportarPdf() {
        List<String> cabeceras = List.of("ID", "NOMBRE", "APELLIDOS", "DNI", "TELÉFONO", "EMAIL");
        List<String[]> filas = listaFiltrada.stream()
                .map(i -> new String[]{
                        String.valueOf(i.getIdInquilino()),
                        i.getNombre(),
                        i.getApellidos(),
                        i.getDni(),
                        i.getTelefono() != null ? i.getTelefono() : "",
                        i.getEmail()    != null ? i.getEmail()    : ""
                })
                .collect(Collectors.toList());
        PdfExporter.exportar("Inquilinos", cabeceras, filas);
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
