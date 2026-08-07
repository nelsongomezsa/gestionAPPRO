package com.gestionap.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Historial de navegación "atrás" de MainController: qué pantalla está
 * activa y a cuáles se puede volver. Cada pantalla se identifica por su
 * ruta FXML (String) — esta clase no sabe nada de Button, FXMLLoader ni
 * JavaFX, así que es testable como una pila cualquiera.
 *
 * <p>El API está partido en pasos separados (apilar / fijar) en vez de un
 * único "navegarA()" porque replica el orden exacto del código original:
 * la pantalla vieja se apila ANTES de intentar cargar la nueva, pero la
 * nueva solo se fija como "actual" si la carga tiene éxito — si el FXML
 * falla, la pantalla activa se queda como estaba (aunque ya haya quedado
 * una entrada en el historial apuntando a ella). Esa es la particularidad
 * real del comportamiento original; no se "arregla" aquí, se replica.
 */
public final class HistorialNavegacion {

    private final Deque<String> pila = new ArrayDeque<>();
    private String actual;

    /**
     * Si hay una pantalla activa y es distinta de {@code nuevaRuta}, la
     * apila para poder volver a ella. Navegar dos veces seguidas a la
     * MISMA ruta no apila nada. No cambia la pantalla activa — eso lo
     * decide quien llama, con {@link #fijarActual}, solo si la carga de
     * la nueva pantalla tuvo éxito.
     */
    public void apilarActualSiCambia(String nuevaRuta) {
        if (actual != null && !actual.equals(nuevaRuta)) {
            pila.push(actual);
        }
    }

    /** Fija la pantalla activa, sin tocar el historial. */
    public void fijarActual(String ruta) {
        actual = ruta;
    }

    /**
     * Apila la pantalla activa y la limpia, sin registrar una nueva
     * navegación — para cuando se navega fuera del sistema de botones
     * (p. ej. abrir un detalle desde otro controller). Devuelve false
     * (y no hace nada) si no había pantalla activa.
     */
    public boolean apilarActualYLimpiar() {
        if (actual == null) return false;
        pila.push(actual);
        actual = null;
        return true;
    }

    /**
     * Saca la última pantalla del historial. No la fija como activa —
     * eso lo hace quien llama, con {@link #fijarActual}, tras cargarla.
     */
    public Optional<String> desapilar() {
        return pila.isEmpty() ? Optional.empty() : Optional.of(pila.pop());
    }

    public boolean hayHistorial() {
        return !pila.isEmpty();
    }

    public Optional<String> actual() {
        return Optional.ofNullable(actual);
    }
}
