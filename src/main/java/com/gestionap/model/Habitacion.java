package com.gestionap.model;

import java.math.BigDecimal;

public class Habitacion {

    public enum Estado { disponible, alquilada, mantenimiento }

    private int        idHabitacion;
    private int        numero;
    private BigDecimal precio;
    private Estado     estado;
    private int        idPiso;

    private String direccionPiso;
    private String nombreCiudad;
    private String inquilinoActual;
    private long   diasAlquilada;

    public Habitacion() {}

    public Habitacion(int idHabitacion, int numero, BigDecimal precio, Estado estado, int idPiso) {
        this.idHabitacion = idHabitacion;
        this.numero       = numero;
        this.precio       = precio;
        this.estado       = estado;
        this.idPiso       = idPiso;
    }

    public int getIdHabitacion() { return idHabitacion; }
    public void setIdHabitacion(int idHabitacion) { this.idHabitacion = idHabitacion; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public int getIdPiso() { return idPiso; }
    public void setIdPiso(int idPiso) { this.idPiso = idPiso; }

    public String getDireccionPiso() { return direccionPiso; }
    public void setDireccionPiso(String direccionPiso) { this.direccionPiso = direccionPiso; }

    public String getNombreCiudad() { return nombreCiudad; }
    public void setNombreCiudad(String nombreCiudad) { this.nombreCiudad = nombreCiudad; }

    public String getInquilinoActual() { return inquilinoActual; }
    public void setInquilinoActual(String inquilinoActual) { this.inquilinoActual = inquilinoActual; }

    public long getDiasAlquilada() { return diasAlquilada; }
    public void setDiasAlquilada(long diasAlquilada) { this.diasAlquilada = diasAlquilada; }

    @Override
    public String toString() {
        return String.format("Habitacion{id=%d, num=%d, precio=%.2f€, estado=%s, piso='%s', ciudad='%s'}",
                idHabitacion, numero, precio, estado, direccionPiso, nombreCiudad);
    }
}
