package com.gestionap.model;

public class Piso {

    private int    idPiso;
    private String direccion;
    private int    numeroHabitaciones;
    private int    idCiudad;

    // Campo auxiliar para mostrar el nombre de la ciudad en listados
    private String nombreCiudad;

    public Piso() {}

    public Piso(int idPiso, String direccion, int numeroHabitaciones, int idCiudad) {
        this.idPiso             = idPiso;
        this.direccion          = direccion;
        this.numeroHabitaciones = numeroHabitaciones;
        this.idCiudad           = idCiudad;
    }

    public int getIdPiso() { return idPiso; }
    public void setIdPiso(int idPiso) { this.idPiso = idPiso; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public int getNumeroHabitaciones() { return numeroHabitaciones; }
    public void setNumeroHabitaciones(int numeroHabitaciones) { this.numeroHabitaciones = numeroHabitaciones; }

    public int getIdCiudad() { return idCiudad; }
    public void setIdCiudad(int idCiudad) { this.idCiudad = idCiudad; }

    public String getNombreCiudad() { return nombreCiudad; }
    public void setNombreCiudad(String nombreCiudad) { this.nombreCiudad = nombreCiudad; }

    @Override
    public String toString() {
        return direccion + (nombreCiudad != null ? "  —  " + nombreCiudad : "");
    }
}
