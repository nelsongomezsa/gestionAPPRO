package com.gestionap.service;

import java.util.Optional;

/**
 * A qué pantalla lleva cada tipo de notificación (MainController las genera
 * en NotificacionDAO.generarNotificaciones y las enruta al hacer clic).
 * Tabla de mapeo pura, sin JavaFX — extraída para poder verificar sin
 * arrancar la UI que cada tipo apunta a donde debe.
 */
public final class RutasNotificacion {

    private RutasNotificacion() {}

    /** Ruta FXML de la pantalla asociada a {@code tipo}, o vacío si el tipo no se reconoce. */
    public static Optional<String> rutaParaTipo(String tipo) {
        if (tipo == null) return Optional.empty();
        return switch (tipo) {
            case "contrato_vence", "pago_pendiente" -> Optional.of("/com/gestionap/contratos-view.fxml");
            case "incidencia_antigua" -> Optional.of("/com/gestionap/incidencias-view.fxml");
            case "mantenimiento_largo" -> Optional.of("/com/gestionap/habitaciones-view.fxml");
            default -> Optional.empty();
        };
    }
}
