package com.gestionap.model;

public class Usuario {

    public enum Rol { admin, usuario }

    private int    idUsuario;
    private String nombre;
    private String email;
    private String passwordHash;
    private Rol    rol;
    private boolean activo;

    public Usuario() {}

    public Usuario(int idUsuario, String nombre, String email, Rol rol, boolean activo) {
        this.idUsuario = idUsuario;
        this.nombre    = nombre;
        this.email     = email;
        this.rol       = rol;
        this.activo    = activo;
    }

    public int     getIdUsuario()    { return idUsuario; }
    public void    setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String  getNombre()       { return nombre; }
    public void    setNombre(String nombre) { this.nombre = nombre; }
    public String  getEmail()        { return email; }
    public void    setEmail(String email) { this.email = email; }
    public String  getPasswordHash() { return passwordHash; }
    public void    setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Rol     getRol()          { return rol; }
    public void    setRol(Rol rol)   { this.rol = rol; }
    public boolean isActivo()        { return activo; }
    public void    setActivo(boolean activo) { this.activo = activo; }

    public String getIniciales() {
        if (nombre == null || nombre.isBlank()) return "?";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, 1).toUpperCase();
        return (partes[0].substring(0,1) + partes[1].substring(0,1)).toUpperCase();
    }

    @Override
    public String toString() {
        return nombre + " (" + email + ")";
    }
}
