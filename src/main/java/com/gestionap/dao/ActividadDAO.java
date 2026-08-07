package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.ActividadLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActividadDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }

    public void registrar(int idUsuario, String accion, String descripcion) {
        try {
            String sql = "INSERT INTO Actividad (id_usuario, accion, descripcion) VALUES (?, ?, ?)";
            try (Connection con = getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, accion);
                ps.setString(3, descripcion);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            System.err.println("[ActividadDAO] No se pudo registrar actividad '"
                    + accion + "': " + ex.getMessage());
        }
    }

    public List<ActividadLog> listarUltimas(int idUsuario, int limite) throws SQLException {
        String sql = "SELECT id, id_usuario, accion, descripcion, fecha " +
                     "FROM Actividad WHERE id_usuario = ? ORDER BY fecha DESC LIMIT ?";
        List<ActividadLog> lista = new ArrayList<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<ActividadLog> listarUltimasGlobal(int limite) throws SQLException {
        String sql = "SELECT a.id, a.id_usuario, a.accion, a.descripcion, a.fecha " +
                     "FROM Actividad a ORDER BY a.fecha DESC LIMIT ?";
        List<ActividadLog> lista = new ArrayList<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public int contarPorAccion(int idUsuario, String accion) {
        try {
            boolean todAs = (accion == null || accion.isBlank());
            String sql = todAs
                ? "SELECT COUNT(*) FROM Actividad WHERE id_usuario = ?"
                : "SELECT COUNT(*) FROM Actividad WHERE id_usuario = ? AND accion = ?";
            try (Connection con = getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idUsuario);
                if (!todAs) ps.setString(2, accion);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (SQLException ex) {
            System.err.println("[ActividadDAO] No se pudo contar acciones '"
                    + accion + "': " + ex.getMessage());
        }
        return 0;
    }

    private ActividadLog mapear(ResultSet rs) throws SQLException {
        ActividadLog a = new ActividadLog();
        a.setId(rs.getInt("id"));
        a.setIdUsuario(rs.getInt("id_usuario"));
        a.setAccion(rs.getString("accion"));
        a.setDescripcion(rs.getString("descripcion"));
        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) a.setFecha(ts.toLocalDateTime());
        return a;
    }
}
