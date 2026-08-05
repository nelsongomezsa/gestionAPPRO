package com.gestionap.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DatosPiso {

    private int        id;
    private int        idPiso;

    // Coste de adquisición
    private BigDecimal precioCompra        = BigDecimal.ZERO;
    private BigDecimal itpPagado           = BigDecimal.ZERO;
    private BigDecimal gastosNotaria       = BigDecimal.ZERO;
    private BigDecimal gastosRegistro      = BigDecimal.ZERO;
    private BigDecimal costeReforma        = BigDecimal.ZERO;
    private BigDecimal costeMobiliario     = BigDecimal.ZERO;
    private BigDecimal otrosGastosCompra   = BigDecimal.ZERO;

    // Financiación
    private boolean    tieneHipoteca       = false;
    private BigDecimal importeHipoteca     = BigDecimal.ZERO;
    private BigDecimal tipoInteres         = BigDecimal.ZERO;
    private int        plazoAnos           = 0;
    private BigDecimal cuotaMensualHipoteca = BigDecimal.ZERO;

    // Gastos operativos
    private BigDecimal gastoIbiAnual            = BigDecimal.ZERO;
    private BigDecimal gastoComunidadMensual    = BigDecimal.ZERO;
    private BigDecimal gastoSeguroAnual         = BigDecimal.ZERO;
    private BigDecimal gastoMantenimientoAnual  = BigDecimal.ZERO;

    // Valor actual
    private BigDecimal valorMercadoActual = BigDecimal.ZERO;
    private LocalDate  fechaCompra;
    private String     notas;

    public DatosPiso() {}

    public DatosPiso(int idPiso) { this.idPiso = idPiso; }

    // ── Getters / Setters ─────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdPiso() { return idPiso; }
    public void setIdPiso(int idPiso) { this.idPiso = idPiso; }

    public BigDecimal getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(BigDecimal v) { this.precioCompra = safe(v); }

    public BigDecimal getItpPagado() { return itpPagado; }
    public void setItpPagado(BigDecimal v) { this.itpPagado = safe(v); }

    public BigDecimal getGastosNotaria() { return gastosNotaria; }
    public void setGastosNotaria(BigDecimal v) { this.gastosNotaria = safe(v); }

    public BigDecimal getGastosRegistro() { return gastosRegistro; }
    public void setGastosRegistro(BigDecimal v) { this.gastosRegistro = safe(v); }

    public BigDecimal getCosteReforma() { return costeReforma; }
    public void setCosteReforma(BigDecimal v) { this.costeReforma = safe(v); }

    public BigDecimal getCosteMobiliario() { return costeMobiliario; }
    public void setCosteMobiliario(BigDecimal v) { this.costeMobiliario = safe(v); }

    public BigDecimal getOtrosGastosCompra() { return otrosGastosCompra; }
    public void setOtrosGastosCompra(BigDecimal v) { this.otrosGastosCompra = safe(v); }

    public boolean isTieneHipoteca() { return tieneHipoteca; }
    public void setTieneHipoteca(boolean v) { this.tieneHipoteca = v; }

    public BigDecimal getImporteHipoteca() { return importeHipoteca; }
    public void setImporteHipoteca(BigDecimal v) { this.importeHipoteca = safe(v); }

    public BigDecimal getTipoInteres() { return tipoInteres; }
    public void setTipoInteres(BigDecimal v) { this.tipoInteres = safe(v); }

    public int getPlazoAnos() { return plazoAnos; }
    public void setPlazoAnos(int v) { this.plazoAnos = v; }

    public BigDecimal getCuotaMensualHipoteca() { return cuotaMensualHipoteca; }
    public void setCuotaMensualHipoteca(BigDecimal v) { this.cuotaMensualHipoteca = safe(v); }

    public BigDecimal getGastoIbiAnual() { return gastoIbiAnual; }
    public void setGastoIbiAnual(BigDecimal v) { this.gastoIbiAnual = safe(v); }

    public BigDecimal getGastoComunidadMensual() { return gastoComunidadMensual; }
    public void setGastoComunidadMensual(BigDecimal v) { this.gastoComunidadMensual = safe(v); }

    public BigDecimal getGastoSeguroAnual() { return gastoSeguroAnual; }
    public void setGastoSeguroAnual(BigDecimal v) { this.gastoSeguroAnual = safe(v); }

    public BigDecimal getGastoMantenimientoAnual() { return gastoMantenimientoAnual; }
    public void setGastoMantenimientoAnual(BigDecimal v) { this.gastoMantenimientoAnual = safe(v); }

    public BigDecimal getValorMercadoActual() { return valorMercadoActual; }
    public void setValorMercadoActual(BigDecimal v) { this.valorMercadoActual = safe(v); }

    public LocalDate getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDate v) { this.fechaCompra = v; }

    public String getNotas() { return notas; }
    public void setNotas(String v) { this.notas = v; }

    // ── Computed helpers ──────────────────────────────────────────

    public BigDecimal inversionTotal() {
        return precioCompra.add(itpPagado).add(gastosNotaria).add(gastosRegistro)
                .add(costeReforma).add(costeMobiliario).add(otrosGastosCompra);
    }

    public BigDecimal gastosAnuales() {
        return gastoIbiAnual
                .add(gastoComunidadMensual.multiply(BigDecimal.valueOf(12)))
                .add(gastoSeguroAnual)
                .add(gastoMantenimientoAnual);
    }

    public BigDecimal hipotecaAnual() {
        return tieneHipoteca ? cuotaMensualHipoteca.multiply(BigDecimal.valueOf(12)) : BigDecimal.ZERO;
    }

    public BigDecimal capitalPropio() {
        BigDecimal inv = inversionTotal();
        return tieneHipoteca ? inv.subtract(importeHipoteca) : inv;
    }

    private static BigDecimal safe(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
