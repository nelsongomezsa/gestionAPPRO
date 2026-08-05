package com.gestionap.controller;

import com.gestionap.dao.UsuarioDAO;
import com.gestionap.model.Usuario;
import com.gestionap.utils.PasswordUtil;
import com.gestionap.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField     txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button        btnLogin;
    @FXML private Label         lblError;

    private UsuarioDAO usuarioDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instances();
        actions();
    }

    private void instances() {
        usuarioDAO = new UsuarioDAO();
    }

    private void actions() {
        btnLogin.setOnAction(e -> login());
        txtPassword.setOnAction(e -> login());
    }

    private void login() {
        String email    = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        if (email.isBlank() || password.isBlank()) {
            mostrarError("Por favor, introduce tu email y contraseña.");
            return;
        }

        try {
            String hash    = PasswordUtil.sha256(password);
            Usuario usuario = usuarioDAO.autenticar(email, hash);

            if (usuario == null) {
                mostrarError("Credenciales incorrectas. Verifica tu email y contraseña.");
                txtPassword.clear();
                return;
            }

            Session.getInstance().iniciarSesion(usuario);
            abrirPanelPrincipal();

        } catch (Exception ex) {
            mostrarError("Error de conexión: " + ex.getMessage());
        }
    }

    private void abrirPanelPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gestionap/main-view.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 700);
            scene.getStylesheets().add(
                    getClass().getResource("/com/gestionap/styles.css").toExternalForm());
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setTitle("GestionAp");
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (Exception ex) {
            mostrarError("No se pudo cargar la aplicación: " + ex.getMessage());
        }
    }

    private void mostrarError(String mensaje) {
        lblError.setText(mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }
}
