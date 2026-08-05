package com.gestionap.model;

import java.math.BigDecimal;
import java.time.LocalDate;


public class Contrato {

    private int        idContrato;
    private int        idHabitacion;
    private int        idInquilino;
    private LocalDate  fechaInicio;
    private LocalDate  fechaFin;
    private BigDecimal precioMensual;

    // Campos auxiliares para mostrar en listas
    private String nombreInquilino;
    private String dniInquilino;
    private int    numeroHabitacion;
    private String direccionPiso;

    public Contrato() {}

    public Contrato(int idContrato, int idHabitacion, int idInquilino,
                    LocalDate fechaInicio, LocalDate fechaFin, BigDecimal precioMensual) {
        this.idContrato    = idContrato;
        this.idHabitacion  = idHabitacion;
        this.idInquilino   = idInquilino;
        this.fechaInicio   = fechaInicio;
        this.fechaFin      = fechaFin;
        this.precioMensual = precioMensual;
    }

    public int getIdContrato() { return idContrato; }
    public void setIdContrato(int idContrato) { this.idContrato = idContrato; }

    public int getIdHabitacion() { return idHabitacion; }
    public void setIdHabitacion(int idHabitacion) { this.idHabitacion = idHabitacion; }

    public int getIdInquilino() { return idInquilino; }
    public void setIdInquilino(int idInquilino) { this.idInquilino = idInquilino; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public BigDecimal getPrecioMensual() { return precioMensual; }
    public void setPrecioMensual(BigDecimal precioMensual) { this.precioMensual = precioMensual; }

    public String getNombreInquilino() { return nombreInquilino; }
    public void setNombreInquilino(String nombreInquilino) { this.nombreInquilino = nombreInquilino; }

    public String getDniInquilino() { return dniInquilino; }
    public void setDniInquilino(String dniInquilino) { this.dniInquilino = dniInquilino; }

    public int getNumeroHabitacion() { return numeroHabitacion; }
    public void setNumeroHabitacion(int numeroHabitacion) { this.numeroHabitacion = numeroHabitacion; }

    public String getDireccionPiso() { return direccionPiso; }
    public void setDireccionPiso(String direccionPiso) { this.direccionPiso = direccionPiso; }


    public boolean isActivo() {
        if (fechaInicio == null || fechaFin == null) return false;
        LocalDate hoy = LocalDate.now();
        return !fechaInicio.isAfter(hoy) && !fechaFin.isBefore(hoy);
    }

    @Override
    public String toString() {
        return String.format("Contrato{id=%d, hab=%d, inquilino='%s', inicio=%s, fin=%s, precio=%.2f€}",
                idContrato, idHabitacion, nombreInquilino, fechaInicio, fechaFin, precioMensual);
    }
}
