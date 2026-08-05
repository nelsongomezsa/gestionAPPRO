package com.gestionap.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AnalisisPisoTest {

    private AnalisisPiso piso;

    @BeforeEach
    void setUp() {
        piso = new AnalisisPiso();
        piso.setIdPiso(1);
        piso.setDireccion("Calle Mayor 10");
        piso.setCiudad("Madrid");
    }

    // ── Tasa de ocupación ────────────────────────────────────────

    @Test
    void calcularMetricas_sinHabitaciones_tasaOcupacionCero() {
        piso.setTotalHabitaciones(0);
        piso.setAlquiladas(0);
        piso.setIngresosMes(BigDecimal.ZERO);
        piso.calcularMetricas();
        assertEquals(0.0, piso.getTasaOcupacion(), 0.001);
    }

    @Test
    void calcularMetricas_todasAlquiladas_tasaOcupacion100() {
        piso.setTotalHabitaciones(4);
        piso.setAlquiladas(4);
        piso.setIngresosMes(new BigDecimal("2000"));
        piso.setPrecioRefMax(new BigDecimal("24000"));
        piso.calcularMetricas();
        assertEquals(100.0, piso.getTasaOcupacion(), 0.001);
    }

    @Test
    void calcularMetricas_mitadAlquiladas_tasaOcupacion50() {
        piso.setTotalHabitaciones(4);
        piso.setAlquiladas(2);
        piso.setIngresosMes(new BigDecimal("1000"));
        piso.calcularMetricas();
        assertEquals(50.0, piso.getTasaOcupacion(), 0.001);
    }

    // ── Ingresos ─────────────────────────────────────────────────

    @Test
    void calcularMetricas_ingresosAnualesEsIngresosMesPor12() {
        piso.setTotalHabitaciones(3);
        piso.setAlquiladas(3);
        BigDecimal ingresosMes = new BigDecimal("1500");
        piso.setIngresosMes(ingresosMes);
        piso.calcularMetricas();
        assertEquals(0, new BigDecimal("18000").compareTo(piso.getIngresosAnuales()));
    }

    // ── Gastos estimados ─────────────────────────────────────────

    @Test
    void calcularMetricas_gastosMesEs30PorCientoIngresos() {
        piso.setTotalHabitaciones(2);
        piso.setAlquiladas(2);
        piso.setIngresosMes(new BigDecimal("1000"));
        piso.calcularMetricas();
        assertEquals(0, new BigDecimal("300.00").compareTo(piso.getGastosMesEstimado()));
    }

    // ── Cash flow ────────────────────────────────────────────────

    @Test
    void calcularMetricas_cashFlowMes_ingrenosMenosGastos() {
        piso.setTotalHabitaciones(2);
        piso.setAlquiladas(2);
        piso.setIngresosMes(new BigDecimal("1000"));
        piso.calcularMetricas();
        // CashFlow = 1000 - 300 = 700
        assertEquals(0, new BigDecimal("700.00").compareTo(piso.getCashFlowMes()));
    }

    @Test
    void calcularMetricas_cashFlowAnual_cashFlowMesPor12() {
        piso.setTotalHabitaciones(2);
        piso.setAlquiladas(2);
        piso.setIngresosMes(new BigDecimal("1000"));
        piso.calcularMetricas();
        assertEquals(0, new BigDecimal("8400.00").compareTo(piso.getCashFlowAnual()));
    }

    // ── Rentabilidad ─────────────────────────────────────────────

    @Test
    void calcularMetricas_rentBruta_calculaCorrectamente() {
        piso.setTotalHabitaciones(3);
        piso.setAlquiladas(3);
        piso.setIngresosMes(new BigDecimal("1500"));   // 18000/año
        piso.setPrecioRefMax(new BigDecimal("180000")); // rentBruta = 18000/180000*100 = 10%
        piso.calcularMetricas();
        assertEquals(10.0, piso.getRentBruta(), 0.01);
    }

    @Test
    void calcularMetricas_sinPrecioRef_rentBrutaCero() {
        piso.setTotalHabitaciones(2);
        piso.setAlquiladas(2);
        piso.setIngresosMes(new BigDecimal("1000"));
        piso.setPrecioRefMax(BigDecimal.ZERO);
        piso.calcularMetricas();
        assertEquals(0.0, piso.getRentBruta(), 0.001);
    }

    @Test
    void calcularMetricas_per_precioRefDivIngresoAnuales() {
        piso.setTotalHabitaciones(3);
        piso.setAlquiladas(3);
        piso.setIngresosMes(new BigDecimal("1500"));   // 18000/año
        piso.setPrecioRefMax(new BigDecimal("180000")); // PER = 180000/18000 = 10
        piso.calcularMetricas();
        assertEquals(10.0, piso.getPer(), 0.01);
    }

    // ── Semáforo ─────────────────────────────────────────────────

    @Test
    void calcularMetricas_semaforo_excelente_cuandoRentaAltaCashFlowPositivoOcupacionAlta() {
        piso.setTotalHabitaciones(4);
        piso.setAlquiladas(4);
        // rentBruta >7%: necesito ingresos anuales > 7% de precioRef
        // 4 hab * 800€ = 3200/mes, 38400/año, precioRef = 384000 → 10%
        piso.setIngresosMes(new BigDecimal("3200"));
        piso.setPrecioRefMax(new BigDecimal("384000"));
        piso.calcularMetricas();
        // cashFlow = 3200*0.7 = 2240/mes > 200 ✓, tasaOcupacion=100% ✓, rentBruta≈10% ✓
        assertEquals(AnalisisPiso.Semaforo.EXCELENTE, piso.getSemaforo());
    }

    @Test
    void calcularMetricas_semaforo_aceptable_cuandoRentaMediaOcupacionMedia() {
        piso.setTotalHabitaciones(4);
        piso.setAlquiladas(3); // 75%
        // rentBruta ≥5%: 3*600=1800/mes, 21600/año, precioRef=360000 → 6%
        piso.setIngresosMes(new BigDecimal("1800"));
        piso.setPrecioRefMax(new BigDecimal("360000"));
        piso.calcularMetricas();
        // cashFlow = 1800*0.7 = 1260 ≥ 0 ✓, tasaOcupacion=75% ≥ 60 ✓, rentBruta=6% ≥ 5 ✓
        assertEquals(AnalisisPiso.Semaforo.ACEPTABLE, piso.getSemaforo());
    }

    @Test
    void calcularMetricas_semaforo_revisar_cuandoSinIngresos() {
        piso.setTotalHabitaciones(4);
        piso.setAlquiladas(0);
        piso.setIngresosMes(BigDecimal.ZERO);
        piso.setPrecioRefMax(new BigDecimal("200000"));
        piso.calcularMetricas();
        assertEquals(AnalisisPiso.Semaforo.REVISAR, piso.getSemaforo());
    }

    // ── Helpers de presentación ──────────────────────────────────

    @Test
    void semaforoTexto_devuelveStringsCorrecto() {
        piso.setTotalHabitaciones(0);
        piso.setAlquiladas(0);
        piso.setIngresosMes(BigDecimal.ZERO);
        piso.calcularMetricas();
        // Por defecto debería ser REVISAR
        assertEquals("REVISAR", piso.semaforoTexto());
    }

    @Test
    void semaforoColor_excelente_verde() {
        piso.setTotalHabitaciones(4);
        piso.setAlquiladas(4);
        piso.setIngresosMes(new BigDecimal("3200"));
        piso.setPrecioRefMax(new BigDecimal("384000"));
        piso.calcularMetricas();
        assertEquals(AnalisisPiso.Semaforo.EXCELENTE, piso.getSemaforo());
        assertEquals("#2ecc71", piso.semaforoColor());
    }

    @Test
    void semaforoColor_revisar_rojo() {
        piso.setTotalHabitaciones(2);
        piso.setAlquiladas(0);
        piso.setIngresosMes(BigDecimal.ZERO);
        piso.calcularMetricas();
        assertEquals("#e74c3c", piso.semaforoColor());
    }

    // ── Datos básicos ────────────────────────────────────────────

    @Test
    void datos_idDireccionCiudad_setterGetter() {
        assertEquals(1,            piso.getIdPiso());
        assertEquals("Calle Mayor 10", piso.getDireccion());
        assertEquals("Madrid",     piso.getCiudad());
    }

    @Test
    void proximosVencimientos_listaVaciaInicial() {
        assertNotNull(piso.getProximosVencimientos());
        assertTrue(piso.getProximosVencimientos().isEmpty());
    }

    @Test
    void eficienciaOcupacion_cuandoMaxEsCero_resultadoCero() {
        piso.setTotalHabitaciones(2);
        piso.setAlquiladas(2);
        piso.setIngresosMes(new BigDecimal("500"));
        piso.setPrecioRefMax(BigDecimal.ZERO); // maxMes = 0
        piso.calcularMetricas();
        assertEquals(0.0, piso.getEficienciaOcupacion(), 0.001);
    }
}
