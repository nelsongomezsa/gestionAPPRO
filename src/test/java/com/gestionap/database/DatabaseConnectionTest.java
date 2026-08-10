package com.gestionap.database;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DatabaseConnectionTest {

    @Test
    void cerrarConexion_sinPoolInicializado_noLanzaExcepcion() {
        assertDoesNotThrow(() -> DatabaseConnection.getInstance().cerrarConexion());
    }

    @Test
    void cerrarConexion_llamadaDosVeces_esIdempotente() {
        DatabaseConnection.getInstance().cerrarConexion();
        assertDoesNotThrow(() -> DatabaseConnection.getInstance().cerrarConexion());
    }
}
