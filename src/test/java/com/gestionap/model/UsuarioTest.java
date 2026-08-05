package com.gestionap.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void getIniciales_nombreNull_devuelveInterrogacion() {
        Usuario u = new Usuario();
        u.setNombre(null);
        assertEquals("?", u.getIniciales());
    }

    @Test
    void getIniciales_nombreBlanco_devuelveInterrogacion() {
        Usuario u = new Usuario();
        u.setNombre("   ");
        assertEquals("?", u.getIniciales());
    }

    @Test
    void getIniciales_soloUnNombre_devuelvePrimeraLetraMayuscula() {
        Usuario u = new Usuario();
        u.setNombre("nelson");
        assertEquals("N", u.getIniciales());
    }

    @Test
    void getIniciales_dosNombres_devuelveDosInicialesMayusculas() {
        Usuario u = new Usuario();
        u.setNombre("nelson gomez");
        assertEquals("NG", u.getIniciales());
    }

    @Test
    void getIniciales_tresNombres_devuelvePrimerasDos() {
        Usuario u = new Usuario();
        u.setNombre("nelson gomez sanchez");
        assertEquals("NG", u.getIniciales());
    }

    @Test
    void getIniciales_espaciosExtra_funcionaCorrectamente() {
        Usuario u = new Usuario();
        u.setNombre("  Ana   Lopez  ");
        assertEquals("AL", u.getIniciales());
    }

    @Test
    void constructor_parametros_inicializaCamposCorrectamente() {
        Usuario u = new Usuario(1, "Admin", "admin@test.com", Usuario.Rol.admin, true);
        assertEquals(1, u.getIdUsuario());
        assertEquals("Admin", u.getNombre());
        assertEquals("admin@test.com", u.getEmail());
        assertEquals(Usuario.Rol.admin, u.getRol());
        assertTrue(u.isActivo());
    }

    @Test
    void rol_admin_distinto_usuario() {
        assertNotEquals(Usuario.Rol.admin, Usuario.Rol.usuario);
    }

    @Test
    void toString_contieneNombreYEmail() {
        Usuario u = new Usuario(1, "Ana", "ana@test.com", Usuario.Rol.usuario, true);
        String s = u.toString();
        assertTrue(s.contains("Ana"));
        assertTrue(s.contains("ana@test.com"));
    }
}
