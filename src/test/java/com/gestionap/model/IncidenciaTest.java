package com.gestionap.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class IncidenciaTest {

    @Test
    void constructor_parametros_inicializaCamposCorrectamente() {
        LocalDate fecha = LocalDate.of(2025, 4, 10);
        Incidencia i = new Incidencia(1, 2, 3, "Gotera en techo",
                Incidencia.Estado.pendiente, fecha);

        assertEquals(1,                        i.getIdIncidencia());
        assertEquals(2,                        i.getIdHabitacion());
        assertEquals(3,                        i.getIdInquilino());
        assertEquals("Gotera en techo",        i.getDescripcion());
        assertEquals(Incidencia.Estado.pendiente, i.getEstado());
        assertEquals(fecha,                    i.getFecha());
    }

    @Test
    void estado_tresTiposDisponibles() {
        assertNotNull(Incidencia.Estado.pendiente);
        assertNotNull(Incidencia.Estado.en_proceso);
        assertNotNull(Incidencia.Estado.resuelta);
        assertEquals(3, Incidencia.Estado.values().length);
    }

    @Test
    void prioridad_tresTiposDisponibles() {
        assertNotNull(Incidencia.Prioridad.Alta);
        assertNotNull(Incidencia.Prioridad.Media);
        assertNotNull(Incidencia.Prioridad.Baja);
        assertEquals(3, Incidencia.Prioridad.values().length);
    }

    @Test
    void setEstado_transicionPendienteAResuelta() {
        Incidencia i = new Incidencia();
        i.setEstado(Incidencia.Estado.pendiente);
        assertEquals(Incidencia.Estado.pendiente, i.getEstado());
        i.setEstado(Incidencia.Estado.en_proceso);
        assertEquals(Incidencia.Estado.en_proceso, i.getEstado());
        i.setEstado(Incidencia.Estado.resuelta);
        assertEquals(Incidencia.Estado.resuelta, i.getEstado());
    }

    @Test
    void setPrioridad_funcionaCorrectamente() {
        Incidencia i = new Incidencia();
        i.setPrioridad(Incidencia.Prioridad.Alta);
        assertEquals(Incidencia.Prioridad.Alta, i.getPrioridad());
    }

    @Test
    void costeReparacion_setterGetter() {
        Incidencia i = new Incidencia();
        BigDecimal coste = new BigDecimal("350.50");
        i.setCosteReparacion(coste);
        assertEquals(0, coste.compareTo(i.getCosteReparacion()));
    }

    @Test
    void costeReparacion_inicialmenteNull() {
        Incidencia i = new Incidencia();
        assertNull(i.getCosteReparacion());
    }

    @Test
    void camposAuxiliares_setterGetter() {
        Incidencia i = new Incidencia();
        i.setNombreInquilino("Luis Martínez");
        i.setNumeroHabitacion(7);
        assertEquals("Luis Martínez", i.getNombreInquilino());
        assertEquals(7,               i.getNumeroHabitacion());
    }

    @Test
    void toString_contieneEstadoYDescripcion() {
        Incidencia i = new Incidencia(1, 1, 1, "Calefacción rota",
                Incidencia.Estado.en_proceso, LocalDate.now());
        i.setNombreInquilino("Test");
        String s = i.toString();
        assertTrue(s.contains("en_proceso"));
        assertTrue(s.contains("Calefacción rota"));
    }
}
