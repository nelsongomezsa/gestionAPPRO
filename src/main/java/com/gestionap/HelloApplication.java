package com.gestionap;

import com.gestionap.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("login-view.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);
        scene.getStylesheets().add(
                getClass().getResource("styles.css").toExternalForm());
        stage.setTitle("GestionAp - Iniciar Sesión");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    @Override
    public void stop() {
        // TODO: deuda técnica de concurrencia — varios controllers lanzan
        // `new Thread(...)` sueltos para consultas en background (p. ej.
        // DashboardController, ContratosController) que siguen viva la app
        // y llaman a Platform.runLater() al terminar. Si el usuario cierra
        // la ventana justo mientras uno está en vuelo, este cerrarConexion()
        // puede tumbar el pool a mitad de consulta y ese hilo falla contra
        // un pool ya cerrado (en el peor caso, una traza de error en
        // consola durante el cierre — no hay corrupción de datos).
        // Aceptado como riesgo bajo para una app de escritorio de un solo
        // usuario (revisión /review del 2026-08-11). La solución correcta
        // si se quiere cerrar del todo: sustituir los `new Thread(...)` por
        // un ExecutorService compartido y hacer shutdown()+awaitTermination()
        // aquí antes de cerrarConexion().
        DatabaseConnection.getInstance().cerrarConexion();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
