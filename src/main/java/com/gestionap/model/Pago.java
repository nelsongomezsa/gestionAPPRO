package com.gestionap.model;

import java.math.BigDecimal;
import java.time.LocalDate;


public class Pago {

    public enum MetodoPago { transferencia, efectivo, domiciliacion }

    private int        idPago;
    private int        idContrato;
    private BigDecimal cantidad;
    private MetodoPago metodoPago;
    private LocalDate  fechaPago;

    // Campo auxiliar para mostrar en listados
    private String nombreInquilino;

    public Pago() {}

    public Pago(int idPago, int idContrato, BigDecimal cantidad,
                MetodoPago metodoPago, LocalDate fechaPago) {
        this.idPago     = idPago;
        this.idContrato = idContrato;
        this.cantidad   = cantidad;
        this.metodoPago = metodoPago;
        this.fechaPago  = fechaPago;
    }

    public int getIdPago() { return idPago; }
    public void setIdPago(int idPago) { this.idPago = idPago; }

    public int getIdContrato() { return idContrato; }
    public void setIdContrato(int idContrato) { this.idContrato = idContrato; }

    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal cantidad) { this.cantidad = cantidad; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public LocalDate getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDate fechaPago) { this.fechaPago = fechaPago; }

    public String getNombreInquilino() { return nombreInquilino; }
    public void setNombreInquilino(String nombreInquilino) { this.nombreInquilino = nombreInquilino; }

    @Override
    public String toString() {
        return String.format("Pago{id=%d, contrato=%d, cantidad=%.2f€, metodo=%s, fecha=%s}",
                idPago, idContrato, cantidad, metodoPago, fechaPago);
    }
}
