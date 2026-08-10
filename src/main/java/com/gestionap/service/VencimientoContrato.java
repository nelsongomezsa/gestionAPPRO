package com.gestionap.service;

import com.gestionap.model.Contrato;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Clasifica la urgencia de vencimiento de un contrato en niveles con nombre,
 * para que las pantallas que necesitan avisar de contratos por vencer
 * (Dashboard, Contratos, ...) usen el mismo criterio en vez de repetir cada
 * una sus propios umbrales sueltos.
 *
 * <p>Sin dependencias de javafx.*: toma una fecha (o un {@link Contrato}),
 * devuelve un {@link Nivel}. Cada pantalla decide cómo colorear/mostrar cada
 * nivel; esta clase no sabe nada de Labels ni estilos.
 *
 * <p><b>No confundir con la ventana de pagos de CalendarioController</b>
 * (días 1 a 5 del mes, ver {@code CalendarioController.calcularEstado}):
 * esa es una regla de PAGOS — cuándo se espera que se cobre la mensualidad
 * de un contrato ya activo — no de VENCIMIENTO del contrato en sí. Son
 * conceptos de dominio distintos aunque ambos hablen de "días" y fechas;
 * no unificarlos aquí ni en el futuro.
 */
public final class VencimientoContrato {

    private VencimientoContrato() {}

    /**
     * Un contrato entra en {@link Nivel#CRITICO} cuando le quedan como
     * mucho estos días para vencer (y no ha vencido ya).
     */
    public static final int UMBRAL_CRITICO_DIAS = 7;

    /**
     * Un contrato entra en {@link Nivel#PROXIMO} cuando le quedan como
     * mucho estos días para vencer (y no está ya en {@link Nivel#CRITICO}
     * ni ha vencido).
     */
    public static final int UMBRAL_PROXIMO_DIAS = 30;

    public enum Nivel { CRITICO, PROXIMO, NORMAL, VENCIDO }

    /**
     * Nivel de urgencia de {@code fechaFin} respecto a hoy:
     * <ul>
     *   <li>{@link Nivel#VENCIDO} — fechaFin ya pasó (días hasta vencer &lt; 0)</li>
     *   <li>{@link Nivel#CRITICO} — vence hoy o dentro de {@value #UMBRAL_CRITICO_DIAS} días</li>
     *   <li>{@link Nivel#PROXIMO} — vence en más de {@value #UMBRAL_CRITICO_DIAS}
     *       días y hasta {@value #UMBRAL_PROXIMO_DIAS} días</li>
     *   <li>{@link Nivel#NORMAL} — vence en más de {@value #UMBRAL_PROXIMO_DIAS} días</li>
     * </ul>
     */
    public static Nivel nivel(LocalDate fechaFin) {
        long dias = diasHastaVencimiento(fechaFin);
        if (dias < 0) return Nivel.VENCIDO;
        if (dias <= UMBRAL_CRITICO_DIAS) return Nivel.CRITICO;
        if (dias <= UMBRAL_PROXIMO_DIAS) return Nivel.PROXIMO;
        return Nivel.NORMAL;
    }

    /** Igual que {@link #nivel(LocalDate)} pero tomando la fecha de fin del contrato. */
    public static Nivel nivel(Contrato contrato) {
        return nivel(contrato.getFechaFin());
    }

    /** Días que faltan para que venza {@code fechaFin} (negativo si ya venció, 0 si vence hoy). */
    public static long diasHastaVencimiento(LocalDate fechaFin) {
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaFin);
    }
}
