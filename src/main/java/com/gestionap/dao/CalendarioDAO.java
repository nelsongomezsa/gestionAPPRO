package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.Contrato;
import com.gestionap.model.Pago;
import com.gestionap.model.Pago.MetodoPago;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class CalendarioDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }

    public List<Contrato> listarContratosActivosMes(YearMonth mes) throws SQLException {
        LocalDate inicio = mes.atDay(1);
        LocalDate fin    = mes.atEndOfMonth();
        String sql = """
                SELECT c.id_contrato, c.id_habitacion, c.id_inquilino,
                       c.fecha_inicio, c.fecha_fin, c.precio_mensual,
                       CONCAT(i.nombre, ' ', i.apellidos) AS nombre_inquilino,
                       i.dni AS dni_inquilino,
                       h.numero AS numero_habitacion,
                       p.direccion AS direccion_piso
                FROM Contratos c
                JOIN Inquilinos   i ON c.id_inquilino  = i.id_inquilino
                JOIN Habitaciones h ON c.id_habitacion = h.id_habitacion
                JOIN Pisos        p ON h.id_piso        = p.id_piso
                WHERE c.fecha_inicio <= ? AND c.fecha_fin >= ?
                ORDER BY c.id_contrato
                """;
        List<Contrato> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fin));
            ps.setDate(2, Date.valueOf(inicio));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearContrato(rs));
            }
        }
        return lista;
    }

    public List<Pago> listarPagosMes(YearMonth mes) throws SQLException {
        LocalDate inicio = mes.atDay(1);
        LocalDate fin    = mes.atEndOfMonth();
        String sql = """
                SELECT p.id_pago, p.id_contrato, p.cantidad, p.metodo_pago, p.fecha_pago,
                       CONCAT(i.nombre, ' ', i.apellidos) AS nombre_inquilino
                FROM Pagos p
                JOIN Contratos  c ON p.id_contrato  = c.id_contrato
                JOIN Inquilinos i ON c.id_inquilino = i.id_inquilino
                WHERE p.fecha_pago >= ? AND p.fecha_pago <= ?
                ORDER BY p.fecha_pago
                """;
        List<Pago> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(inicio));
            ps.setDate(2, Date.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearPago(rs));
            }
        }
        return lista;
    }

    private Contrato mapearContrato(ResultSet rs) throws SQLException {
        Contrato c = new Contrato();
        c.setIdContrato(rs.getInt("id_contrato"));
        c.setIdHabitacion(rs.getInt("id_habitacion"));
        c.setIdInquilino(rs.getInt("id_inquilino"));
        java.sql.Date di = rs.getDate("fecha_inicio");
        java.sql.Date df = rs.getDate("fecha_fin");
        c.setFechaInicio(di != null ? di.toLocalDate() : LocalDate.now());
        c.setFechaFin(df != null ? df.toLocalDate() : LocalDate.now());
        c.setPrecioMensual(rs.getBigDecimal("precio_mensual"));
        c.setNombreInquilino(rs.getString("nombre_inquilino"));
        c.setDniInquilino(rs.getString("dni_inquilino"));
        c.setNumeroHabitacion(rs.getInt("numero_habitacion"));
        c.setDireccionPiso(rs.getString("direccion_piso"));
        return c;
    }

    private Pago mapearPago(ResultSet rs) throws SQLException {
        Pago p = new Pago();
        p.setIdPago(rs.getInt("id_pago"));
        p.setIdContrato(rs.getInt("id_contrato"));
        p.setCantidad(rs.getBigDecimal("cantidad"));
        p.setMetodoPago(MetodoPago.valueOf(rs.getString("metodo_pago")));
        java.sql.Date fp = rs.getDate("fecha_pago");
        p.setFechaPago(fp != null ? fp.toLocalDate() : LocalDate.now());
        p.setNombreInquilino(rs.getString("nombre_inquilino"));
        return p;
    }
}
