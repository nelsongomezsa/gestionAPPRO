package com.gestionap.service;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RutasNotificacionTest {

    @Test
    void contratoVence_llevaAContratos() {
        assertEquals(Optional.of("/com/gestionap/contratos-view.fxml"),
                RutasNotificacion.rutaParaTipo("contrato_vence"));
    }

    @Test
    void pagoPendiente_llevaAContratos() {
        assertEquals(Optional.of("/com/gestionap/contratos-view.fxml"),
                RutasNotificacion.rutaParaTipo("pago_pendiente"));
    }

    @Test
    void incidenciaAntigua_llevaAIncidencias() {
        assertEquals(Optional.of("/com/gestionap/incidencias-view.fxml"),
                RutasNotificacion.rutaParaTipo("incidencia_antigua"));
    }

    @Test
    void mantenimientoLargo_llevaAHabitaciones() {
        assertEquals(Optional.of("/com/gestionap/habitaciones-view.fxml"),
                RutasNotificacion.rutaParaTipo("mantenimiento_largo"));
    }

    @Test
    void tipoDesconocido_devuelveVacio() {
        assertTrue(RutasNotificacion.rutaParaTipo("tipo_que_no_existe").isEmpty());
    }

    @Test
    void tipoNulo_devuelveVacio() {
        assertTrue(RutasNotificacion.rutaParaTipo(null).isEmpty());
    }
}
