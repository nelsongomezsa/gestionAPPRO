package com.gestionap.service;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HistorialNavegacionTest {

    @Test
    void alPrincipio_noHayHistorialNiPantallaActiva() {
        HistorialNavegacion h = new HistorialNavegacion();

        assertFalse(h.hayHistorial());
        assertTrue(h.actual().isEmpty());
    }

    private void navegarConExito(HistorialNavegacion h, String ruta) {
        h.apilarActualSiCambia(ruta);
        h.fijarActual(ruta);
    }

    @Test
    void navegarUnaVez_fijaLaPantallaActivaYNoGeneraHistorial() {
        HistorialNavegacion h = new HistorialNavegacion();

        navegarConExito(h, "/a.fxml");

        assertEquals(Optional.of("/a.fxml"), h.actual());
        assertFalse(h.hayHistorial(), "La primera navegación no tiene nada previo que apilar");
    }

    @Test
    void navegarADosPantallasDistintas_apilaLaPrimera() {
        HistorialNavegacion h = new HistorialNavegacion();

        navegarConExito(h, "/a.fxml");
        navegarConExito(h, "/b.fxml");

        assertEquals(Optional.of("/b.fxml"), h.actual());
        assertTrue(h.hayHistorial());
    }

    @Test
    void navegarDosVecesALaMismaRuta_noApilaNada() {
        HistorialNavegacion h = new HistorialNavegacion();

        navegarConExito(h, "/a.fxml");
        navegarConExito(h, "/a.fxml"); // clic en el mismo botón de nuevo

        assertFalse(h.hayHistorial(), "Re-navegar a la misma ruta no debe generar una entrada de historial");
    }

    @Test
    void desapilar_devuelveLaUltimaPantallaApiladaYLaQuitaDelHistorial() {
        HistorialNavegacion h = new HistorialNavegacion();
        navegarConExito(h, "/a.fxml");
        navegarConExito(h, "/b.fxml");

        Optional<String> anterior = h.desapilar();

        assertEquals(Optional.of("/a.fxml"), anterior);
        assertFalse(h.hayHistorial(), "Tras desapilar la única entrada, el historial queda vacío");
    }

    @Test
    void desapilar_sinHistorial_devuelveVacio() {
        HistorialNavegacion h = new HistorialNavegacion();

        assertTrue(h.desapilar().isEmpty());
    }

    @Test
    void volverAtras_esLifo_variosNiveles() {
        HistorialNavegacion h = new HistorialNavegacion();
        navegarConExito(h, "/a.fxml");
        navegarConExito(h, "/b.fxml");
        navegarConExito(h, "/c.fxml");

        // Simula MainController.volverAtras(): desapilar + fijarActual con lo desapilado
        String v1 = h.desapilar().orElseThrow();
        h.fijarActual(v1);
        assertEquals("/b.fxml", v1);

        String v2 = h.desapilar().orElseThrow();
        h.fijarActual(v2);
        assertEquals("/a.fxml", v2);

        assertFalse(h.hayHistorial());
    }

    @Test
    void apilarActualYLimpiar_conPantallaActiva_laApilaYLimpiaActual() {
        HistorialNavegacion h = new HistorialNavegacion();
        navegarConExito(h, "/detalle-origen.fxml");

        boolean hizoAlgo = h.apilarActualYLimpiar();

        assertTrue(hizoAlgo);
        assertTrue(h.actual().isEmpty(), "La pantalla activa se limpia tras apilarActualYLimpiar()");
        assertTrue(h.hayHistorial());
        assertEquals(Optional.of("/detalle-origen.fxml"), h.desapilar());
    }

    @Test
    void apilarActualYLimpiar_sinPantallaActiva_noHaceNadaYDevuelveFalse() {
        HistorialNavegacion h = new HistorialNavegacion();

        boolean hizoAlgo = h.apilarActualYLimpiar();

        assertFalse(hizoAlgo);
        assertFalse(h.hayHistorial());
    }

    @Test
    void fijarActual_sinApilarPrimero_noGeneraHistorial() {
        // Replica MainController.navegarInquilinosConBusqueda(): navega
        // directo sin pasar por apilarActualSiCambia().
        HistorialNavegacion h = new HistorialNavegacion();
        navegarConExito(h, "/a.fxml");

        h.fijarActual("/inquilinos-view.fxml");

        assertEquals(Optional.of("/inquilinos-view.fxml"), h.actual());
        assertFalse(h.hayHistorial(), "fijarActual() por sí solo no debe generar una entrada de historial");
    }

    @Test
    void navegacionFallida_dejaUnaEntradaDuplicadaEnHistorial_particularidadDelOriginal() {
        // Replica MainController.cargarVista(): apilarActualSiCambia() se
        // llama SIEMPRE, antes de intentar cargar el FXML. Si la carga
        // falla, fijarActual() nunca se invoca — la pantalla activa queda
        // en el valor viejo, pero ese valor viejo YA se apiló. Resultado:
        // el historial tiene una entrada que coincide con la pantalla
        // activa. Es una particularidad real del código original, no un
        // comportamiento que este refactor deba "arreglar".
        HistorialNavegacion h = new HistorialNavegacion();
        navegarConExito(h, "/a.fxml");

        h.apilarActualSiCambia("/ruta-que-fallara.fxml");
        // fijarActual() NO se llama — simula que cargarVistaInternal() lanzó una excepción.

        assertEquals(Optional.of("/a.fxml"), h.actual(),
                "Si la nueva pantalla falla al cargar, la activa sigue siendo la vieja");
        assertTrue(h.hayHistorial(),
                "Pero la vieja ya quedó apilada de todos modos — duplicado real del comportamiento original");
        assertEquals(Optional.of("/a.fxml"), h.desapilar());
    }
}
