package com.gestionap.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActividadLog {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int           id;
    private int           idUsuario;
    private String        accion;
    private String        descripcion;
    private LocalDateTime fecha;

    public int           getId()          { return id; }
    public void          setId(int id)    { this.id = id; }
    public int           getIdUsuario()   { return idUsuario; }
    public void          setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public String        getAccion()      { return accion; }
    public void          setAccion(String accion) { this.accion = accion; }
    public String        getDescripcion() { return descripcion; }
    public void          setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getFecha()       { return fecha; }
    public void          setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getFechaFormateada() {
        return fecha != null ? fecha.format(FMT) : "";
    }

    public String getIcono() {
        if (accion == null) return "•";
        return switch (accion) {
            case "CREAR_INQUILINO", "EDITAR_INQUILINO", "ELIMINAR_INQUILINO" -> "👤";
            case "CREAR_CONTRATO", "FINALIZAR_CONTRATO"                      -> "📋";
            case "REGISTRAR_PAGO"                                            -> "💰";
            case "CREAR_INCIDENCIA", "RESOLVER_INCIDENCIA"                   -> "🔧";
            case "CREAR_USUARIO", "CAMBIAR_ROL"                              -> "👥";
            case "CAMBIAR_PASSWORD", "CAMBIAR_PERFIL"                        -> "🔐";
            default -> "•";
        };
    }
}
