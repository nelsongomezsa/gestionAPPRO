package com.gestionap.utils;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.nio.file.*;
import java.util.Optional;
import java.util.Properties;

public class EmailService {

    private static final String CONFIG_DIR  = System.getProperty("user.home") + "/.gestionap";
    private static final String CONFIG_FILE = CONFIG_DIR + "/email.properties";

    // ── Config ───────────────────────────────────────────────────

    private static Properties cargarConfig() {
        Properties p = new Properties();
        File f = new File(CONFIG_FILE);
        if (f.exists()) {
            try (FileReader r = new FileReader(f)) { p.load(r); }
            catch (IOException ignored) {}
        }
        return p;
    }

    private static void guardarConfig(Properties p) {
        try {
            Files.createDirectories(Paths.get(CONFIG_DIR));
            try (FileWriter w = new FileWriter(CONFIG_FILE)) {
                p.store(w, "GestionAp - Email Config");
            }
        } catch (IOException ignored) {}
    }

    private static Properties pedirConfiguracion() {
        Dialog<Properties> dlg = new Dialog<>();
        dlg.setTitle("Configuración de Email");
        dlg.setHeaderText("Introduce las credenciales de Gmail SMTP");

        ButtonType btnOk = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField     tfUser = new TextField();
        PasswordField tfPass = new PasswordField();
        tfUser.setPromptText("tucuenta@gmail.com");
        tfPass.setPromptText("Contraseña de aplicación (App Password)");
        tfUser.setPrefWidth(280);
        tfPass.setPrefWidth(280);

        Label nota = new Label("⚠  Usa una Contraseña de Aplicación de Google,\nno tu contraseña habitual.");
        nota.setStyle("-fx-text-fill: #f39c12; -fx-font-size: 11px;");

        grid.add(new Label("Email:"),    0, 0); grid.add(tfUser, 1, 0);
        grid.add(new Label("Password:"), 0, 1); grid.add(tfPass, 1, 1);
        grid.add(nota,                   0, 2, 2, 1);

        dlg.getDialogPane().setContent(grid);
        dlg.setResultConverter(bt -> {
            if (bt != btnOk) return null;
            if (tfUser.getText().isBlank() || tfPass.getText().isBlank()) return null;
            Properties p = new Properties();
            p.setProperty("smtp.host",     "smtp.gmail.com");
            p.setProperty("smtp.port",     "587");
            p.setProperty("smtp.user",     tfUser.getText().trim());
            p.setProperty("smtp.password", tfPass.getText().trim());
            return p;
        });

        Optional<Properties> res = dlg.showAndWait();
        return res.orElse(null);
    }

    // ── Envío ────────────────────────────────────────────────────

    public static boolean enviar(String destinatario, String asunto, String cuerpo) {
        Properties config = cargarConfig();
        if (!config.containsKey("smtp.user") || config.getProperty("smtp.user").isBlank()) {
            config = pedirConfiguracion();
            if (config == null) return false;
            guardarConfig(config);
        }

        String user = config.getProperty("smtp.user");
        String pass = config.getProperty("smtp.password");
        String host = config.getProperty("smtp.host", "smtp.gmail.com");
        String port = config.getProperty("smtp.port", "587");

        Properties smtp = new Properties();
        smtp.put("mail.smtp.auth",            "true");
        smtp.put("mail.smtp.starttls.enable", "true");
        smtp.put("mail.smtp.host",            host);
        smtp.put("mail.smtp.port",            port);

        try {
            javax.mail.Session session = javax.mail.Session.getInstance(smtp, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            });

            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(user));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            msg.setSubject(asunto);
            msg.setText(cuerpo);
            Transport.send(msg);
            return true;

        } catch (MessagingException ex) {
            mostrarError("No se pudo enviar el email", ex.getMessage());
            return false;
        }
    }

    // ── Dialog de composición ────────────────────────────────────

    public static void mostrarDialogo(String destinatario, String asuntoDefault, String cuerpoDefault) {
        if (destinatario == null || destinatario.isBlank()) {
            mostrarError("Sin dirección de email", "Este inquilino no tiene email registrado.");
            return;
        }

        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Enviar Email");
        dlg.setHeaderText("Para: " + destinatario);

        ButtonType btnEnviar = new ButtonType("Enviar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnEnviar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        TextField tfAsunto = new TextField(asuntoDefault);
        TextArea  taMsg    = new TextArea(cuerpoDefault);
        taMsg.setPrefRowCount(8);
        taMsg.setPrefWidth(400);
        taMsg.setWrapText(true);
        tfAsunto.setPrefWidth(400);

        grid.add(labelForm("Asunto:"),  0, 0); grid.add(tfAsunto, 1, 0);
        grid.add(labelForm("Mensaje:"), 0, 1); grid.add(taMsg,    1, 1);

        dlg.getDialogPane().setContent(grid);
        dlg.setResultConverter(bt -> {
            if (bt == btnEnviar) {
                boolean ok = enviar(destinatario, tfAsunto.getText(), taMsg.getText());
                if (ok) {
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Email enviado");
                    info.setHeaderText(null);
                    info.setContentText("Email enviado correctamente a " + destinatario);
                    info.showAndWait();
                }
            }
            return null;
        });
        dlg.showAndWait();
    }

    // ── Helpers ──────────────────────────────────────────────────

    private static Label labelForm(String t) {
        Label l = new Label(t);
        l.setStyle("-fx-text-fill: #8888aa; -fx-font-size: 12px; -fx-font-weight: bold;");
        return l;
    }

    private static void mostrarError(String header, String msg) {
        Alert err = new Alert(Alert.AlertType.ERROR);
        err.setTitle("Error");
        err.setHeaderText(header);
        err.setContentText(msg);
        err.showAndWait();
    }
}
