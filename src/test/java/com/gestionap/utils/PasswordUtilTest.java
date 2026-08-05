package com.gestionap.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void sha256_conocido_devuelveHashCorrecto() {
        // echo -n "admin123" | sha256sum
        String esperado = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
        assertEquals(esperado, PasswordUtil.sha256("admin123"));
    }

    @Test
    void sha256_esDeterminista() {
        assertEquals(PasswordUtil.sha256("test"), PasswordUtil.sha256("test"));
    }

    @Test
    void sha256_stringVacio_noLanzaExcepcion() {
        String hash = PasswordUtil.sha256("");
        assertNotNull(hash);
        assertEquals(64, hash.length());
    }

    @Test
    void sha256_distingueMayusculas() {
        assertNotEquals(PasswordUtil.sha256("Abc"), PasswordUtil.sha256("abc"));
    }

    @Test
    void sha256_longitud_siempreSesenta4Hex() {
        assertEquals(64, PasswordUtil.sha256("cualquier texto").length());
        assertEquals(64, PasswordUtil.sha256("x").length());
    }

    @Test
    void verificar_contrasenaCorrecta_devuelveTrue() {
        String hash = PasswordUtil.sha256("miPassword");
        assertTrue(PasswordUtil.verificar("miPassword", hash));
    }

    @Test
    void verificar_contrasenaIncorrecta_devuelveFalse() {
        String hash = PasswordUtil.sha256("miPassword");
        assertFalse(PasswordUtil.verificar("otraPassword", hash));
    }

    @Test
    void verificar_stringVacio_funcionaCorrectamente() {
        String hash = PasswordUtil.sha256("");
        assertTrue(PasswordUtil.verificar("", hash));
        assertFalse(PasswordUtil.verificar("algo", hash));
    }
}
