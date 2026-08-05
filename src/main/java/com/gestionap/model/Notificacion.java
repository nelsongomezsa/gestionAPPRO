package com.gestionap.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notificacion {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private int           id;
    private String        tipo;
    private String        titulo;
    private String        descripcion;
    private String        referencia;
    private LocalDateTime fecha;
    private boolean       leida;
    private int           idUsuario;

    public int           getId()          { return id; }
    public void          setId(int id)    { this.id = id; }
    public String        getTipo()        { return tipo; }
    public void          setTipo(String tipo) { this.tipo = tipo; }
    public String        getTitulo()      { return titulo; }
    public void          setTitulo(String titulo) { this.titulo = titulo; }
    public String        getDescripcion() { return descripcion; }
    public void          setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String        getReferencia()  { return referencia; }
    public void          setReferencia(String referencia) { this.referencia = referencia; }
    public LocalDateTime getFecha()       { return fecha; }
    public void          setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public boolean       isLeida()        { return leida; }
    public void          setLeida(boolean leida) { this.leida = leida; }
    public int           getIdUsuario()   { return idUsuario; }
    public void          setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getFechaFormateada() {
        return fecha != null ? fecha.format(FMT) : "";
    }

    public String getIcono() {
        if (tipo == null) return "🔔";
        return switch (tipo) {
            case "contrato_vence"    -> "📋";
            case "pago_pendiente"    -> "💰";
            case "incidencia_antigua"-> "🔧";
            case "mantenimiento_largo"-> "🏠";
            default -> "🔔";
        };
    }

    public String getColor() {
        if (tipo == null) return "#c8c8e8";
        return switch (tipo) {
            case "contrato_vence"     -> "#f39c12";
            case "pago_pendiente"     -> "#e74c3c";
            case "incidencia_antigua" -> "#e67e22";
            case "mantenimiento_largo"-> "#9b59b6";
            default -> "#4A90E2";
        };
    }
}
