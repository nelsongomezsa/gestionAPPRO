package com.gestionap.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PagoTest {

    @Test
    void constructor_parametros_inicializaCamposCorrectamente() {
        BigDecimal cantidad = new BigDecimal("650.00");
        LocalDate fecha = LocalDate.of(2025, 3, 5);

        Pago p = new Pago(1, 10, cantidad, Pago.MetodoPago.transferencia, fecha);

        assertEquals(1,                          p.getIdPago());
        assertEquals(10,                         p.getIdContrato());
        assertEquals(0, cantidad.compareTo(p.getCantidad()));
        assertEquals(Pago.MetodoPago.transferencia, p.getMetodoPago());
        assertEquals(fecha,                      p.getFechaPago());
    }

    @Test
    void metodoPago_tresTiposDisponibles() {
        assertNotNull(Pago.MetodoPago.transferencia);
        assertNotNull(Pago.MetodoPago.efectivo);
        assertNotNull(Pago.MetodoPago.domiciliacion);
        assertEquals(3, Pago.MetodoPago.values().length);
    }

    @Test
    void setMetodoPago_cambiaCorrectamente() {
        Pago p = new Pago();
        p.setMetodoPago(Pago.MetodoPago.efectivo);
        assertEquals(Pago.MetodoPago.efectivo, p.getMetodoPago());
    }

    @Test
    void nombreInquilino_setterGetter() {
        Pago p = new Pago();
        p.setNombreInquilino("Ana García");
        assertEquals("Ana García", p.getNombreInquilino());
    }

    @Test
    void nombreInquilino_puedeSerNull() {
        Pago p = new Pago();
        assertNull(p.getNombreInquilino());
    }

    @Test
    void toString_contieneIdYCantidad() {
        Pago p = new Pago(5, 2, new BigDecimal("750.00"),
                Pago.MetodoPago.domiciliacion, LocalDate.of(2025, 5, 1));
        String s = p.toString();
        assertTrue(s.contains("5"));
        assertTrue(s.contains("750"));
    }

    @Test
    void cantidad_valorNegativo_sePuedeAsignar() {
        Pago p = new Pago();
        BigDecimal negativo = new BigDecimal("-100.00");
        p.setCantidad(negativo);
        assertEquals(0, negativo.compareTo(p.getCantidad()));
    }
}
