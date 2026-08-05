package com.gestionap.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AnalisisPiso {

    public enum Semaforo { EXCELENTE, ACEPTABLE, REVISAR }

    // ── Datos básicos ────────────────────────────────────────────
    private int    idPiso;
    private String direccion;
    private String ciudad;

    // ── Habitaciones ─────────────────────────────────────────────
    private int    totalHabitaciones;
    private int    alquiladas;
    private int    disponibles;
    private int    mantenimiento;
    private double tasaOcupacion;

    // ── Financiero real ───────────────────────────────────────────
    private BigDecimal ingresosMes       = BigDecimal.ZERO;
    private BigDecimal ingresosAnuales   = BigDecimal.ZERO;
    private BigDecimal totalCobrado      = BigDecimal.ZERO;
    private BigDecimal precioRefMax      = BigDecimal.ZERO; // sum(precio) * 12
    private BigDecimal gastosMesEstimado = BigDecimal.ZERO;
    private BigDecimal cashFlowMes       = BigDecimal.ZERO;
    private BigDecimal cashFlowAnual     = BigDecimal.ZERO;
    private BigDecimal costeIncidencias  = BigDecimal.ZERO;

    // ── Métricas calculadas ───────────────────────────────────────
    private double rentBruta;
    private double rentNeta;
    private double roce;
    private double per;
    private double puntoEquilibrio;
    private double eficienciaOcupacion;

    // ── Contratos ─────────────────────────────────────────────────
    private int          contratosActivos;
    private int          contratosTotal;
    private List<String> proximosVencimientos = new ArrayList<>();

    // ── Incidencias ───────────────────────────────────────────────
    private int totalIncidencias;

    // ── Semáforo ──────────────────────────────────────────────────
    private Semaforo semaforo = Semaforo.REVISAR;

    // ── Cálculo automático de todas las métricas ──────────────────
    public void calcularMetricas() {
        // Tasa de ocupación
        tasaOcupacion = totalHabitaciones > 0
                ? (alquiladas * 100.0) / totalHabitaciones : 0;

        // Ingresos anualizados
        ingresosAnuales = ingresosMes.multiply(BigDecimal.valueOf(12));

        // Gastos estimados: 30 % de ingresos (IBI, comunidad, seguros, mantenimiento)
        gastosMesEstimado = ingresosMes.multiply(BigDecimal.valueOf(0.30))
                .setScale(2, RoundingMode.HALF_UP);

        // Cash flow
        cashFlowMes  = ingresosMes.subtract(gastosMesEstimado).setScale(2, RoundingMode.HALF_UP);
        cashFlowAnual = cashFlowMes.multiply(BigDecimal.valueOf(12)).setScale(2, RoundingMode.HALF_UP);

        boolean refPositiva = precioRefMax.compareTo(BigDecimal.ZERO) > 0;

        // Rentabilidad bruta = ingresos anuales / precio ref. máx. * 100
        rentBruta = refPositiva
                ? ingresosAnuales.doubleValue() / precioRefMax.doubleValue() * 100 : 0;

        // Rentabilidad neta = (ingresos - gastos) anuales / precio ref. máx. * 100
        BigDecimal gastosAnuales = gastosMesEstimado.multiply(BigDecimal.valueOf(12));
        rentNeta = refPositiva
                ? ingresosAnuales.subtract(gastosAnuales).doubleValue()
                  / precioRefMax.doubleValue() * 100 : 0;

        // ROCE: cash flow anual / (precio ref. * 0,20 capital propio) * 100
        double capitalPropio = precioRefMax.doubleValue() * 0.20;
        roce = capitalPropio > 0 ? cashFlowAnual.doubleValue() / capitalPropio * 100 : 0;

        // PER inmobiliario = precio ref. / ingresos anuales
        per = ingresosAnuales.compareTo(BigDecimal.ZERO) > 0
                ? precioRefMax.doubleValue() / ingresosAnuales.doubleValue() : 0;

        // Punto de equilibrio = habitaciones necesarias para cubrir gastos
        double precioMedioHab = alquiladas > 0
                ? ingresosMes.doubleValue() / alquiladas : 0;
        puntoEquilibrio = precioMedioHab > 0
                ? gastosMesEstimado.doubleValue() / precioMedioHab : 0;

        // Eficiencia de ingresos (actual vs teórico máximo)
        BigDecimal maxMes = precioRefMax.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        eficienciaOcupacion = maxMes.compareTo(BigDecimal.ZERO) > 0
                ? ingresosMes.doubleValue() / maxMes.doubleValue() * 100 : 0;

        // Semáforo
        double cf = cashFlowMes.doubleValue();
        if (rentBruta > 7 && cf > 200 && tasaOcupacion > 80) {
            semaforo = Semaforo.EXCELENTE;
        } else if (rentBruta >= 5 && cf >= 0 && tasaOcupacion >= 60) {
            semaforo = Semaforo.ACEPTABLE;
        } else {
            semaforo = Semaforo.REVISAR;
        }
    }

    // ── Getters / Setters ─────────────────────────────────────────
    public int    getIdPiso()       { return idPiso; }
    public void   setIdPiso(int v)  { this.idPiso = v; }
    public String getDireccion()    { return direccion; }
    public void   setDireccion(String v)  { this.direccion = v; }
    public String getCiudad()       { return ciudad; }
    public void   setCiudad(String v)     { this.ciudad = v; }

    public int    getTotalHabitaciones()     { return totalHabitaciones; }
    public void   setTotalHabitaciones(int v){ this.totalHabitaciones = v; }
    public int    getAlquiladas()            { return alquiladas; }
    public void   setAlquiladas(int v)       { this.alquiladas = v; }
    public int    getDisponibles()           { return disponibles; }
    public void   setDisponibles(int v)      { this.disponibles = v; }
    public int    getMantenimiento()         { return mantenimiento; }
    public void   setMantenimiento(int v)    { this.mantenimiento = v; }
    public double getTasaOcupacion()         { return tasaOcupacion; }

    public BigDecimal getIngresosMes()       { return ingresosMes; }
    public void       setIngresosMes(BigDecimal v)      { this.ingresosMes = v; }
    public BigDecimal getIngresosAnuales()   { return ingresosAnuales; }
    public BigDecimal getTotalCobrado()      { return totalCobrado; }
    public void       setTotalCobrado(BigDecimal v)     { this.totalCobrado = v; }
    public BigDecimal getPrecioRefMax()      { return precioRefMax; }
    public void       setPrecioRefMax(BigDecimal v)     { this.precioRefMax = v; }
    public BigDecimal getGastosMesEstimado() { return gastosMesEstimado; }
    public BigDecimal getCashFlowMes()       { return cashFlowMes; }
    public BigDecimal getCashFlowAnual()     { return cashFlowAnual; }
    public BigDecimal getCosteIncidencias()  { return costeIncidencias; }
    public void       setCosteIncidencias(BigDecimal v) { this.costeIncidencias = v; }

    public double getRentBruta()        { return rentBruta; }
    public double getRentNeta()         { return rentNeta; }
    public double getRoce()             { return roce; }
    public double getPer()              { return per; }
    public double getPuntoEquilibrio()  { return puntoEquilibrio; }
    public double getEficienciaOcupacion() { return eficienciaOcupacion; }

    public int    getContratosActivos()   { return contratosActivos; }
    public void   setContratosActivos(int v)   { this.contratosActivos = v; }
    public int    getContratosTotal()     { return contratosTotal; }
    public void   setContratosTotal(int v)     { this.contratosTotal = v; }
    public List<String> getProximosVencimientos() { return proximosVencimientos; }
    public void   setProximosVencimientos(List<String> v) { this.proximosVencimientos = v; }

    public int    getTotalIncidencias()   { return totalIncidencias; }
    public void   setTotalIncidencias(int v)   { this.totalIncidencias = v; }
    public Semaforo getSemaforo()         { return semaforo; }

    // ── Helpers de presentación ───────────────────────────────────
    public String semaforoEmoji() {
        return switch (semaforo) {
            case EXCELENTE -> "🟢";
            case ACEPTABLE -> "🟡";
            case REVISAR   -> "🔴";
        };
    }

    public String semaforoTexto() {
        return switch (semaforo) {
            case EXCELENTE -> "EXCELENTE";
            case ACEPTABLE -> "ACEPTABLE";
            case REVISAR   -> "REVISAR";
        };
    }

    public String semaforoColor() {
        return switch (semaforo) {
            case EXCELENTE -> "#2ecc71";
            case ACEPTABLE -> "#f39c12";
            case REVISAR   -> "#e74c3c";
        };
    }

    // ── Clases internas para datos de detalle ─────────────────────

    public static class HabitacionDetalle {
        public int       idHabitacion;
        public int       numero;
        public BigDecimal precio;
        public String    estado;
        public String    inquilinoActual; // "—" si libre
        public LocalDate finContrato;     // null si no tiene contrato activo
    }

    public static class ContratoDetalle {
        public int        idContrato;
        public int        numeroHab;
        public String     inquilino;
        public LocalDate  fechaInicio;
        public LocalDate  fechaFin;
        public BigDecimal precioMensual;
        public boolean    activo;
    }

    public static class PagoDetalle {
        public LocalDate  fechaPago;
        public BigDecimal cantidad;
        public String     metodoPago;
        public String     inquilino;
    }

    public static class IncidenciaDetalle {
        public int        idIncidencia;
        public int        numeroHab;
        public String     inquilino;
        public String     descripcion;
        public String     estado;
        public LocalDate  fecha;
        public BigDecimal costeReparacion;
    }
}
