package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.DatosPiso;

import java.sql.*;
import java.time.LocalDate;

public class DatosPisoDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }

    public DatosPiso obtenerPorPiso(int idPiso) throws SQLException {
        String sql = """
                SELECT id, id_piso, precio_compra, itp_pagado, gastos_notaria, gastos_registro,
                       coste_reforma, coste_mobiliario, otros_gastos_compra,
                       tiene_hipoteca, importe_hipoteca, tipo_interes, plazo_anos, cuota_mensual_hipoteca,
                       gasto_ibi_anual, gasto_comunidad_mensual, gasto_seguro_anual, gasto_mantenimiento_anual,
                       valor_mercado_actual, fecha_compra, notas
                FROM DatosPiso
                WHERE id_piso = ?
                """;
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPiso);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearFila(rs);
            }
        }
        // No hay registro todavía → devolver objeto vacío
        return new DatosPiso(idPiso);
    }

    public void guardar(DatosPiso d) throws SQLException {
        String sql = """
                INSERT INTO DatosPiso
                    (id_piso, precio_compra, itp_pagado, gastos_notaria, gastos_registro,
                     coste_reforma, coste_mobiliario, otros_gastos_compra,
                     tiene_hipoteca, importe_hipoteca, tipo_interes, plazo_anos, cuota_mensual_hipoteca,
                     gasto_ibi_anual, gasto_comunidad_mensual, gasto_seguro_anual, gasto_mantenimiento_anual,
                     valor_mercado_actual, fecha_compra, notas)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    precio_compra             = VALUES(precio_compra),
                    itp_pagado                = VALUES(itp_pagado),
                    gastos_notaria            = VALUES(gastos_notaria),
                    gastos_registro           = VALUES(gastos_registro),
                    coste_reforma             = VALUES(coste_reforma),
                    coste_mobiliario          = VALUES(coste_mobiliario),
                    otros_gastos_compra       = VALUES(otros_gastos_compra),
                    tiene_hipoteca            = VALUES(tiene_hipoteca),
                    importe_hipoteca          = VALUES(importe_hipoteca),
                    tipo_interes              = VALUES(tipo_interes),
                    plazo_anos                = VALUES(plazo_anos),
                    cuota_mensual_hipoteca    = VALUES(cuota_mensual_hipoteca),
                    gasto_ibi_anual           = VALUES(gasto_ibi_anual),
                    gasto_comunidad_mensual   = VALUES(gasto_comunidad_mensual),
                    gasto_seguro_anual        = VALUES(gasto_seguro_anual),
                    gasto_mantenimiento_anual = VALUES(gasto_mantenimiento_anual),
                    valor_mercado_actual      = VALUES(valor_mercado_actual),
                    fecha_compra              = VALUES(fecha_compra),
                    notas                     = VALUES(notas)
                """;
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            ps.setInt   (i++, d.getIdPiso());
            ps.setBigDecimal(i++, d.getPrecioCompra());
            ps.setBigDecimal(i++, d.getItpPagado());
            ps.setBigDecimal(i++, d.getGastosNotaria());
            ps.setBigDecimal(i++, d.getGastosRegistro());
            ps.setBigDecimal(i++, d.getCosteReforma());
            ps.setBigDecimal(i++, d.getCosteMobiliario());
            ps.setBigDecimal(i++, d.getOtrosGastosCompra());
            ps.setInt   (i++, d.isTieneHipoteca() ? 1 : 0);
            ps.setBigDecimal(i++, d.getImporteHipoteca());
            ps.setBigDecimal(i++, d.getTipoInteres());
            ps.setInt   (i++, d.getPlazoAnos());
            ps.setBigDecimal(i++, d.getCuotaMensualHipoteca());
            ps.setBigDecimal(i++, d.getGastoIbiAnual());
            ps.setBigDecimal(i++, d.getGastoComunidadMensual());
            ps.setBigDecimal(i++, d.getGastoSeguroAnual());
            ps.setBigDecimal(i++, d.getGastoMantenimientoAnual());
            ps.setBigDecimal(i++, d.getValorMercadoActual());
            if (d.getFechaCompra() != null)
                ps.setDate(i++, java.sql.Date.valueOf(d.getFechaCompra()));
            else
                ps.setNull(i++, Types.DATE);
            ps.setString(i, d.getNotas());
            ps.executeUpdate();
        }
    }

    private DatosPiso mapearFila(ResultSet rs) throws SQLException {
        DatosPiso d = new DatosPiso();
        d.setId(rs.getInt("id"));
        d.setIdPiso(rs.getInt("id_piso"));
        d.setPrecioCompra(rs.getBigDecimal("precio_compra"));
        d.setItpPagado(rs.getBigDecimal("itp_pagado"));
        d.setGastosNotaria(rs.getBigDecimal("gastos_notaria"));
        d.setGastosRegistro(rs.getBigDecimal("gastos_registro"));
        d.setCosteReforma(rs.getBigDecimal("coste_reforma"));
        d.setCosteMobiliario(rs.getBigDecimal("coste_mobiliario"));
        d.setOtrosGastosCompra(rs.getBigDecimal("otros_gastos_compra"));
        d.setTieneHipoteca(rs.getInt("tiene_hipoteca") == 1);
        d.setImporteHipoteca(rs.getBigDecimal("importe_hipoteca"));
        d.setTipoInteres(rs.getBigDecimal("tipo_interes"));
        d.setPlazoAnos(rs.getInt("plazo_anos"));
        d.setCuotaMensualHipoteca(rs.getBigDecimal("cuota_mensual_hipoteca"));
        d.setGastoIbiAnual(rs.getBigDecimal("gasto_ibi_anual"));
        d.setGastoComunidadMensual(rs.getBigDecimal("gasto_comunidad_mensual"));
        d.setGastoSeguroAnual(rs.getBigDecimal("gasto_seguro_anual"));
        d.setGastoMantenimientoAnual(rs.getBigDecimal("gasto_mantenimiento_anual"));
        d.setValorMercadoActual(rs.getBigDecimal("valor_mercado_actual"));
        java.sql.Date fc = rs.getDate("fecha_compra");
        if (fc != null) d.setFechaCompra(fc.toLocalDate());
        d.setNotas(rs.getString("notas"));
        return d;
    }
}
