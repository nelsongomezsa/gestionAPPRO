package com.gestionap.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ContratoTest {

    @Test
    void isActivo_fechasNull_devuelveFalse() {
        Contrato c = new Contrato();
        assertFalse(c.isActivo());
    }

    @Test
    void isActivo_soloInicioNull_devuelveFalse() {
        Contrato c = new Contrato();
        c.setFechaFin(LocalDate.now().plusMonths(6));
        assertFalse(c.isActivo());
    }

    @Test
    void isActivo_contratoVigente_devuelveTrue() {
        Contrato c = new Contrato();
        c.setFechaInicio(LocalDate.now().minusMonths(3));
        c.setFechaFin(LocalDate.now().plusMonths(3));
        assertTrue(c.isActivo());
    }

    @Test
    void isActivo_contratoExpirado_devuelveFalse() {
        Contrato c = new Contrato();
        c.setFechaInicio(LocalDate.now().minusYears(2));
        c.setFechaFin(LocalDate.now().minusDays(1));
        assertFalse(c.isActivo());
    }

    @Test
    void isActivo_contratoFuturo_devuelveFalse() {
        Contrato c = new Contrato();
        c.setFechaInicio(LocalDate.now().plusDays(1));
        c.setFechaFin(LocalDate.now().plusMonths(12));
        assertFalse(c.isActivo());
    }

    @Test
    void isActivo_inicioEsHoy_devuelveTrue() {
        Contrato c = new Contrato();
        c.setFechaInicio(LocalDate.now());
        c.setFechaFin(LocalDate.now().plusMonths(12));
        assertTrue(c.isActivo());
    }

    @Test
    void isActivo_finEsHoy_devuelveTrue() {
        Contrato c = new Contrato();
        c.setFechaInicio(LocalDate.now().minusMonths(12));
        c.setFechaFin(LocalDate.now());
        assertTrue(c.isActivo());
    }

    @Test
    void constructor_parametros_inicializaCamposCorrectamente() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fin    = LocalDate.of(2025, 12, 31);
        BigDecimal precio = new BigDecimal("650.00");

        Contrato c = new Contrato(10, 2, 5, inicio, fin, precio);

        assertEquals(10,    c.getIdContrato());
        assertEquals(2,     c.getIdHabitacion());
        assertEquals(5,     c.getIdInquilino());
        assertEquals(inicio, c.getFechaInicio());
        assertEquals(fin,    c.getFechaFin());
        assertEquals(0,      precio.compareTo(c.getPrecioMensual()));
    }

    @Test
    void camposAuxiliares_setterGetter() {
        Contrato c = new Contrato();
        c.setNombreInquilino("María López");
        c.setDniInquilino("12345678A");
        c.setNumeroHabitacion(3);
        c.setDireccionPiso("Calle Mayor 10");

        assertEquals("María López",   c.getNombreInquilino());
        assertEquals("12345678A",      c.getDniInquilino());
        assertEquals(3,                c.getNumeroHabitacion());
        assertEquals("Calle Mayor 10", c.getDireccionPiso());
    }

    @Test
    void toString_contieneInformacionRelevante() {
        Contrato c = new Contrato(1, 2, 3,
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31),
                new BigDecimal("700.00"));
        c.setNombreInquilino("Juan");
        String s = c.toString();
        assertTrue(s.contains("1"));
        assertTrue(s.contains("700"));
    }
}
