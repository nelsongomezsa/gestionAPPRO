package com.gestionap.utils;

import com.gestionap.model.Usuario;

public class Session {

    private static Session instancia;
    private Usuario usuarioActual;

    private Session() {}

    public static Session getInstance() {
        if (instancia == null) instancia = new Session();
        return instancia;
    }

    public void iniciarSesion(Usuario usuario) { this.usuarioActual = usuario; }
    public void cerrarSesion() { this.usuarioActual = null; }
    public Usuario getUsuarioActual() { return usuarioActual; }
    public boolean estaLogueado() { return usuarioActual != null; }
}
