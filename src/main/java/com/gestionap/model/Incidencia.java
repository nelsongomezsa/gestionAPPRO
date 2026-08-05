package com.gestionap.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Incidencia {

    public enum Estado    { pendiente, en_proceso, resuelta }
    public enum Prioridad { Alta, Media, Baja }

    private int        idIncidencia;
    private int        idHabitacion;
    private int        idInquilino;
    private String     descripcion;
    private Estado     estado;
    private LocalDate  fecha;
    private Prioridad  prioridad;
    private BigDecimal costeReparacion;

    private String nombreInquilino;
    private int    numeroHabitacion;

    public Incidencia() {}

    public Incidencia(int idIncidencia, int idHabitacion, int idInquilino,
                      String descripcion, Estado estado, LocalDate fecha) {
        this.idIncidencia = idIncidencia;
        this.idHabitacion = idHabitacion;
        this.idInquilino  = idInquilino;
        this.descripcion  = descripcion;
        this.estado       = estado;
        this.fecha        = fecha;
    }

    public int getIdIncidencia() { return idIncidencia; }
    public void setIdIncidencia(int idIncidencia) { this.idIncidencia = idIncidencia; }

    public int getIdHabitacion() { return idHabitacion; }
    public void setIdHabitacion(int idHabitacion) { this.idHabitacion = idHabitacion; }

    public int getIdInquilino() { return idInquilino; }
    public void setIdInquilino(int idInquilino) { this.idInquilino = idInquilino; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Prioridad getPrioridad() { return prioridad; }
    public void setPrioridad(Prioridad prioridad) { this.prioridad = prioridad; }

    public BigDecimal getCosteReparacion() { return costeReparacion; }
    public void setCosteReparacion(BigDecimal costeReparacion) { this.costeReparacion = costeReparacion; }

    public String getNombreInquilino() { return nombreInquilino; }
    public void setNombreInquilino(String nombreInquilino) { this.nombreInquilino = nombreInquilino; }

    public int getNumeroHabitacion() { return numeroHabitacion; }
    public void setNumeroHabitacion(int numeroHabitacion) { this.numeroHabitacion = numeroHabitacion; }

    @Override
    public String toString() {
        return String.format("Incidencia{id=%d, hab=%d, inquilino='%s', estado=%s, fecha=%s, desc='%s'}",
                idIncidencia, idHabitacion, nombreInquilino, estado, fecha, descripcion);
    }
}
