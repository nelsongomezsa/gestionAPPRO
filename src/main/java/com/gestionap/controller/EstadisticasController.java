package com.gestionap.controller;

import com.gestionap.dao.DashboardDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
import java.util.ResourceBundle;

public class EstadisticasController implements Initializable {

    @FXML private Label                        lblIngresosAnioActual;
    @FXML private Label                        lblIngresosAnioAnterior;
    @FXML private Label                        lblAnioActual;
    @FXML private Label                        lblAnioAnterior;
    @FXML private Label                        lblTasaOcupacion;
    @FXML private Label                        lblVariacion;
    @FXML private LineChart<String, Number>    chartLineas;
    @FXML private PieChart                     chartTarta;
    @FXML private BarChart<String, Number>     chartIncidencias;

    private DashboardDAO dashboardDAO;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dashboardDAO = new DashboardDAO();
        cargarTodo();
    }

    private void cargarTodo() {
        new Thread(() -> {
            try {
                int anioActual   = LocalDate.now().getYear();
                int anioAnterior = anioActual - 1;

                BigDecimal actual   = dashboardDAO.ingresosTotalAnio(anioActual);
                BigDecimal anterior = dashboardDAO.ingresosTotalAnio(anioAnterior);
                double tasa         = dashboardDAO.tasaOcupacion();

                Map<String, BigDecimal> datosLineas      = dashboardDAO.ingresosPorMes12();
                Map<String, Integer>    datosTarta       = dashboardDAO.habitacionesPorEstado();
                Map<String, Integer>    datosIncidencias = dashboardDAO.incidenciasPorMes();

                Platform.runLater(() -> {
                    actualizarKPIs(anioActual, anioAnterior, actual, anterior, tasa);
                    actualizarGraficoLineas(datosLineas);
                    actualizarGraficoTarta(datosTarta);
                    actualizarGraficoIncidencias(datosIncidencias);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    lblIngresosAnioActual.setText("—");
                    lblIngresosAnioAnterior.setText("—");
                    lblVariacion.setText("Error: " + ex.getMessage());
                    lblVariacion.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
                });
            }
        }).start();
    }

    private void actualizarKPIs(int anioActual, int anioAnterior,
                                BigDecimal actual, BigDecimal anterior, double tasa) {
        lblIngresosAnioActual.setText(String.format("%.0f €", actual));
        lblIngresosAnioAnterior.setText(String.format("%.0f €", anterior));
        lblAnioActual.setText("INGRESOS " + anioActual);
        lblAnioAnterior.setText("INGRESOS " + anioAnterior);

        lblTasaOcupacion.setText(String.format("%.1f%%", tasa));
        lblTasaOcupacion.setStyle("-fx-text-fill: " + colorTasa(tasa) +
                "; -fx-font-size: 32px; -fx-font-weight: bold;");

        if (anterior.compareTo(BigDecimal.ZERO) > 0) {
            double var = (actual.doubleValue() - anterior.doubleValue()) / anterior.doubleValue() * 100;
            lblVariacion.setText(String.format("%+.1f%%", var));
            lblVariacion.setStyle("-fx-text-fill: " + (var >= 0 ? "#2ecc71" : "#e74c3c") +
                    "; -fx-font-size: 32px; -fx-font-weight: bold;");
        } else {
            lblVariacion.setText("N/A");
        }
    }

    private void actualizarGraficoLineas(Map<String, BigDecimal> datos) {
        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Ingresos");
            datos.forEach((mes, total) ->
                    series.getData().add(new XYChart.Data<>(mes, total)));
            chartLineas.getData().setAll(series);
        } catch (Exception ex) {
            chartLineas.setTitle("Sin datos: " + ex.getMessage());
        }
    }

    private void actualizarGraficoTarta(Map<String, Integer> datos) {
        try {
            datos.forEach((estado, cnt) ->
                    chartTarta.getData().add(new PieChart.Data(estado + " (" + cnt + ")", cnt)));
        } catch (Exception ex) {
            chartTarta.setTitle("Sin datos: " + ex.getMessage());
        }
    }

    private void actualizarGraficoIncidencias(Map<String, Integer> datos) {
        try {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Incidencias");
            datos.forEach((mes, cnt) ->
                    series.getData().add(new XYChart.Data<>(mes, cnt)));
            chartIncidencias.getData().setAll(series);
        } catch (Exception ex) {
            chartIncidencias.setTitle("Sin datos: " + ex.getMessage());
        }
    }

    private String colorTasa(double tasa) {
        if (tasa >= 75) return "#2ecc71";
        if (tasa >= 40) return "#f39c12";
        return "#e74c3c";
    }
}
