package com.gestionap.model;

public class Inquilino {

    private int    idInquilino;
    private String nombre;
    private String apellidos;
    private String dni;
    private String telefono;
    private String email;

    public Inquilino() {}

    public Inquilino(int idInquilino, String nombre, String apellidos,
                     String dni, String telefono, String email) {
        this.idInquilino = idInquilino;
        this.nombre      = nombre;
        this.apellidos   = apellidos;
        this.dni         = dni;
        this.telefono    = telefono;
        this.email       = email;
    }

    public int getIdInquilino() { return idInquilino; }
    public void setIdInquilino(int idInquilino) { this.idInquilino = idInquilino; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }


    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }

    @Override
    public String toString() {
        return String.format("Inquilino{id=%d, nombre='%s %s', dni='%s', tel='%s', email='%s'}",
                idInquilino, nombre, apellidos, dni, telefono, email);
    }
}
