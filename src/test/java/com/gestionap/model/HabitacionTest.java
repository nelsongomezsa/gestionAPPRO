package com.gestionap.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HabitacionTest {

    @Test
    void constructor_parametros_inicializaCamposCorrectamente() {
        BigDecimal precio = new BigDecimal("500.00");
        Habitacion h = new Habitacion(1, 101, precio, Habitacion.Estado.disponible, 2);

        assertEquals(1,                       h.getIdHabitacion());
        assertEquals(101,                     h.getNumero());
        assertEquals(0, precio.compareTo(h.getPrecio()));
        assertEquals(Habitacion.Estado.disponible, h.getEstado());
        assertEquals(2,                       h.getIdPiso());
    }

    @Test
    void estado_disponible_alquilada_mantenimiento_existenComoEnum() {
        assertNotNull(Habitacion.Estado.disponible);
        assertNotNull(Habitacion.Estado.alquilada);
        assertNotNull(Habitacion.Estado.mantenimiento);
        assertEquals(3, Habitacion.Estado.values().length);
    }

    @Test
    void setEstado_cambiaCorrecto() {
        Habitacion h = new Habitacion();
        h.setEstado(Habitacion.Estado.alquilada);
        assertEquals(Habitacion.Estado.alquilada, h.getEstado());
        h.setEstado(Habitacion.Estado.mantenimiento);
        assertEquals(Habitacion.Estado.mantenimiento, h.getEstado());
    }

    @Test
    void camposAuxiliares_setterGetter() {
        Habitacion h = new Habitacion();
        h.setDireccionPiso("Calle Gran Vía 5");
        h.setNombreCiudad("Madrid");
        h.setInquilinoActual("Pedro Sánchez");
        h.setDiasAlquilada(120L);

        assertEquals("Calle Gran Vía 5", h.getDireccionPiso());
        assertEquals("Madrid",           h.getNombreCiudad());
        assertEquals("Pedro Sánchez",    h.getInquilinoActual());
        assertEquals(120L,               h.getDiasAlquilada());
    }

    @Test
    void inquilinoActual_puedeSerNull() {
        Habitacion h = new Habitacion();
        h.setInquilinoActual(null);
        assertNull(h.getInquilinoActual());
    }

    @Test
    void toString_contieneNumeroYEstado() {
        Habitacion h = new Habitacion(1, 5, new BigDecimal("450.00"),
                Habitacion.Estado.disponible, 1);
        h.setDireccionPiso("Calle Luna");
        h.setNombreCiudad("Barcelona");
        String s = h.toString();
        assertTrue(s.contains("5"));
        assertTrue(s.contains("disponible"));
    }

    @Test
    void precio_precisión_bigDecimal() {
        Habitacion h = new Habitacion();
        BigDecimal precio = new BigDecimal("999.99");
        h.setPrecio(precio);
        assertEquals(0, precio.compareTo(h.getPrecio()));
    }
}
