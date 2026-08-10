package com.gestionap.service;

import com.gestionap.model.Contrato;
import com.gestionap.service.VencimientoContrato.Nivel;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VencimientoContratoTest {

    // ── Bordes ─────────────────────────────────────────────────────

    @Test
    void hoyMismo_esCritico() {
        LocalDate hoy = LocalDate.now();
        assertEquals(Nivel.CRITICO, VencimientoContrato.nivel(hoy));
    }

    @Test
    void ayer_esVencido() {
        LocalDate ayer = LocalDate.now().minusDays(1);
        assertEquals(Nivel.VENCIDO, VencimientoContrato.nivel(ayer));
    }

    @Test
    void exactamenteSieteDias_esCritico() {
        LocalDate fecha = LocalDate.now().plusDays(7);
        assertEquals(Nivel.CRITICO, VencimientoContrato.nivel(fecha));
    }

    @Test
    void ochoDias_yaEsProximo_noCritico() {
        LocalDate fecha = LocalDate.now().plusDays(8);
        assertEquals(Nivel.PROXIMO, VencimientoContrato.nivel(fecha));
    }

    @Test
    void exactamenteTreintaDias_esProximo() {
        LocalDate fecha = LocalDate.now().plusDays(30);
        assertEquals(Nivel.PROXIMO, VencimientoContrato.nivel(fecha));
    }

    @Test
    void treintaYUnDias_yaEsNormal_noProximo() {
        LocalDate fecha = LocalDate.now().plusDays(31);
        assertEquals(Nivel.NORMAL, VencimientoContrato.nivel(fecha));
    }

    // ── Casos generales ────────────────────────────────────────────

    @Test
    void muyPasado_esVencido() {
        assertEquals(Nivel.VENCIDO, VencimientoContrato.nivel(LocalDate.now().minusYears(1)));
    }

    @Test
    void muyLejano_esNormal() {
        assertEquals(Nivel.NORMAL, VencimientoContrato.nivel(LocalDate.now().plusYears(1)));
    }

    @Test
    void tresDias_esCritico() {
        assertEquals(Nivel.CRITICO, VencimientoContrato.nivel(LocalDate.now().plusDays(3)));
    }

    @Test
    void quinceDias_esProximo() {
        // 15 no es un umbral propio de VencimientoContrato (ver DashboardController,
        // que tiene su propio corte cosmético de color en 15 días); a nivel de
        // negocio, 15 días cae dentro de PROXIMO (<=30).
        assertEquals(Nivel.PROXIMO, VencimientoContrato.nivel(LocalDate.now().plusDays(15)));
    }

    // ── diasHastaVencimiento ───────────────────────────────────────

    @Test
    void diasHastaVencimiento_hoy_esCero() {
        assertEquals(0, VencimientoContrato.diasHastaVencimiento(LocalDate.now()));
    }

    @Test
    void diasHastaVencimiento_ayer_esNegativoUno() {
        assertEquals(-1, VencimientoContrato.diasHastaVencimiento(LocalDate.now().minusDays(1)));
    }

    @Test
    void diasHastaVencimiento_enTreintaDias_esTreinta() {
        assertEquals(30, VencimientoContrato.diasHastaVencimiento(LocalDate.now().plusDays(30)));
    }

    // ── Overload con Contrato ────────────────────────────────────────

    @Test
    void nivelDeContrato_delegaEnFechaFin() {
        Contrato contrato = new Contrato();
        contrato.setFechaFin(LocalDate.now().plusDays(7));

        assertEquals(Nivel.CRITICO, VencimientoContrato.nivel(contrato));
    }

    @Test
    void nivelDeContrato_vencido() {
        Contrato contrato = new Contrato();
        contrato.setFechaFin(LocalDate.now().minusDays(5));

        assertEquals(Nivel.VENCIDO, VencimientoContrato.nivel(contrato));
    }

    // ── Umbrales con nombre expuestos ──────────────────────────────

    @Test
    void umbralesPublicados_tienenLosValoresDeNegocio() {
        assertEquals(7, VencimientoContrato.UMBRAL_CRITICO_DIAS);
        assertEquals(30, VencimientoContrato.UMBRAL_PROXIMO_DIAS);
    }
}
