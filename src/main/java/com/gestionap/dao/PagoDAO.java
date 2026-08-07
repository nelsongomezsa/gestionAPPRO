package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.Pago;
import com.gestionap.model.Pago.MetodoPago;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class PagoDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }


    public List<Pago> listarPorContrato(int idContrato) throws SQLException {
        String sql = """
                SELECT p.id_pago, p.id_contrato, p.cantidad, p.metodo_pago, p.fecha_pago,
                       CONCAT(i.nombre, ' ', i.apellidos) AS nombre_inquilino
                FROM Pagos p
                JOIN Contratos  c ON p.id_contrato  = c.id_contrato
                JOIN Inquilinos i ON c.id_inquilino = i.id_inquilino
                WHERE p.id_contrato = ?
                ORDER BY p.fecha_pago DESC
                """;
        List<Pago> lista = new ArrayList<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idContrato);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearFila(rs));
                }
            }
        }
        return lista;
    }


    public List<Pago> listarTodos() throws SQLException {
        String sql = """
                SELECT p.id_pago, p.id_contrato, p.cantidad, p.metodo_pago, p.fecha_pago,
                       CONCAT(i.nombre, ' ', i.apellidos) AS nombre_inquilino
                FROM Pagos p
                JOIN Contratos  c ON p.id_contrato  = c.id_contrato
                JOIN Inquilinos i ON c.id_inquilino = i.id_inquilino
                ORDER BY p.fecha_pago DESC
                """;
        List<Pago> lista = new ArrayList<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
        }
        return lista;
    }


    public int insertar(Pago p) throws SQLException {
        String sql = "INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES (?, ?, ?, ?)";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getIdContrato());
            ps.setBigDecimal(2, p.getCantidad());
            ps.setString(3, p.getMetodoPago().name());
            ps.setDate(4, Date.valueOf(p.getFechaPago()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener el ID generado para el pago.");
    }


    public boolean eliminar(int idPago) throws SQLException {
        String sql = "DELETE FROM Pagos WHERE id_pago = ?";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPago);
            return ps.executeUpdate() > 0;
        }
    }


    private Pago mapearFila(ResultSet rs) throws SQLException {
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
