package com.gestionap.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Cálculos de rentabilidad financiera de un piso: inversión total, cuota de
 * hipoteca, rentabilidad bruta/neta, cash flow mensual, ROCE, capital
 * propio, plusvalía latente y PER inmobiliario.
 *
 * <p>Extraído de AnalisisPisosController (el diálogo "Datos financieros")
 * para que esta lógica sea testable sin JavaFX. Sin dependencias de
 * javafx.*: toma números, devuelve números — no sabe nada de diálogos,
 * TextFields ni Labels.
 *
 * <p>Replica exactamente el comportamiento del cálculo original, incluida
 * una particularidad: los campos {@code rentabilidadBrutaPct},
 * {@code rentabilidadNetaPct} y {@code calificacion} vienen {@code null}
 * cuando {@code inversionTotal <= 0} (no son calculables), y
 * {@code rocePct} viene {@code null} cuando {@code capitalPropio <= 0}, y
 * {@code perAnios} viene {@code null} cuando los ingresos anuales son 0 —
 * el llamador decide qué hacer en esos casos (el controller original, ante
 * un valor null, no actualiza el Label correspondiente y deja el valor
 * previamente mostrado).
 */
public final class CalculadoraFinancieraPiso {

    private CalculadoraFinancieraPiso() {}

    public enum Calificacion { EXCELENTE, ACEPTABLE, A_REVISAR }

    public record DatosAdquisicion(
            BigDecimal precioCompra,
            BigDecimal itpPagado,
            BigDecimal gastosNotaria,
            BigDecimal gastosRegistro,
            BigDecimal costeReforma,
            BigDecimal costeMobiliario,
            BigDecimal otrosGastosCompra) {

        public BigDecimal total() {
            return precioCompra.add(itpPagado).add(gastosNotaria).add(gastosRegistro)
                    .add(costeReforma).add(costeMobiliario).add(otrosGastosCompra);
        }
    }

    public record DatosHipoteca(boolean activa, BigDecimal importe, BigDecimal tipoInteresAnualPct, int plazoAnos) {}

    public record DatosGastos(
            BigDecimal ibiAnual,
            BigDecimal comunidadMensual,
            BigDecimal seguroAnual,
            BigDecimal mantenimientoAnual) {

        public BigDecimal totalAnual() {
            return ibiAnual.add(comunidadMensual.multiply(BigDecimal.valueOf(12)))
                    .add(seguroAnual).add(mantenimientoAnual);
        }
    }

    public record Entrada(
            DatosAdquisicion adquisicion,
            DatosHipoteca hipoteca,
            DatosGastos gastos,
            BigDecimal valorMercadoActual,
            BigDecimal ingresosMes) {}

    public record Resultado(
            BigDecimal inversionTotal,
            BigDecimal cuotaMensualHipoteca,
            BigDecimal totalGastosAnuales,
            Double rentabilidadBrutaPct,
            Double rentabilidadNetaPct,
            Calificacion calificacion,
            BigDecimal cashFlowMensual,
            Double rocePct,
            BigDecimal capitalPropio,
            BigDecimal plusvaliaLatente,
            Double perAnios) {}

    public static Resultado calcular(Entrada e) {
        BigDecimal invTotal = e.adquisicion().total();

        BigDecimal cuota = e.hipoteca().activa()
                ? cuotaMensual(e.hipoteca().importe(), e.hipoteca().tipoInteresAnualPct(), e.hipoteca().plazoAnos())
                : BigDecimal.ZERO;

        BigDecimal totalGastosAnuales = e.gastos().totalAnual();

        BigDecimal ingresosAnuales = e.ingresosMes().multiply(BigDecimal.valueOf(12));
        BigDecimal hipAnual = cuota.multiply(BigDecimal.valueOf(12));
        BigDecimal importeHipEfectivo = e.hipoteca().activa() ? e.hipoteca().importe() : BigDecimal.ZERO;
        BigDecimal capitalPropio = invTotal.subtract(importeHipEfectivo);

        Double rentBruta = null;
        Double rentNeta = null;
        Calificacion calificacion = null;
        if (invTotal.compareTo(BigDecimal.ZERO) > 0) {
            rentBruta = ingresosAnuales.divide(invTotal, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            rentNeta = ingresosAnuales.subtract(totalGastosAnuales).subtract(hipAnual)
                    .divide(invTotal, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            calificacion = rentBruta > 7 ? Calificacion.EXCELENTE
                    : rentBruta >= 5 ? Calificacion.ACEPTABLE
                    : Calificacion.A_REVISAR;
        }

        BigDecimal gastosMes = totalGastosAnuales.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        BigDecimal cfMes = e.ingresosMes().subtract(cuota).subtract(gastosMes);

        Double roce = null;
        if (capitalPropio.compareTo(BigDecimal.ZERO) > 0) {
            roce = cfMes.multiply(BigDecimal.valueOf(12))
                    .divide(capitalPropio, 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        BigDecimal plusvalia = e.valorMercadoActual().subtract(invTotal);

        Double per = null;
        if (ingresosAnuales.compareTo(BigDecimal.ZERO) > 0) {
            per = invTotal.divide(ingresosAnuales, 2, RoundingMode.HALF_UP).doubleValue();
        }

        return new Resultado(invTotal, cuota, totalGastosAnuales,
                rentBruta, rentNeta, calificacion,
                cfMes, roce, capitalPropio, plusvalia, per);
    }

    /** Cuota mensual de una hipoteca a cuota fija (amortización francesa). */
    public static BigDecimal cuotaMensual(BigDecimal principal, BigDecimal tasaAnualPct, int plazoAnos) {
        if (principal == null || tasaAnualPct == null || plazoAnos <= 0
                || principal.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        double p = principal.doubleValue();
        double r = tasaAnualPct.doubleValue() / 100.0 / 12.0;
        int n = plazoAnos * 12;
        if (r == 0) return BigDecimal.valueOf(p / n).setScale(2, RoundingMode.HALF_UP);
        double m = p * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
        return BigDecimal.valueOf(m).setScale(2, RoundingMode.HALF_UP);
    }

    /** Parsea un importe introducido por el usuario (admite coma decimal). Vacío/inválido -> ZERO. */
    public static BigDecimal parse(String s) {
        try {
            if (s == null || s.isBlank()) return BigDecimal.ZERO;
            return new BigDecimal(s.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /** Parsea un entero introducido por el usuario. Vacío/inválido -> 0. */
    public static int parseInt(String s) {
        try {
            return s == null || s.isBlank() ? 0 : Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
