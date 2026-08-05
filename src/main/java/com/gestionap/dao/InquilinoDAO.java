package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.Inquilino;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class InquilinoDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }

    public List<Inquilino> listarTodos() throws SQLException {
        String sql = "SELECT id_inquilino, nombre, apellidos, dni, telefono, email " +
                     "FROM Inquilinos ORDER BY apellidos, nombre";
        List<Inquilino> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearFila(rs));
            }
        }
        return lista;
    }


    public Inquilino buscarPorDni(String dni) throws SQLException {
        String sql = "SELECT id_inquilino, nombre, apellidos, dni, telefono, email " +
                     "FROM Inquilinos WHERE dni = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearFila(rs);
            }
        }
        return null;
    }


    public Inquilino buscarPorId(int idInquilino) throws SQLException {
        String sql = "SELECT id_inquilino, nombre, apellidos, dni, telefono, email " +
                     "FROM Inquilinos WHERE id_inquilino = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idInquilino);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearFila(rs);
            }
        }
        return null;
    }


    public int insertar(Inquilino i) throws SQLException {
        String sql = "INSERT INTO Inquilinos (nombre, apellidos, dni, telefono, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, i.getNombre());
            ps.setString(2, i.getApellidos());
            ps.setString(3, i.getDni());
            ps.setString(4, i.getTelefono());
            ps.setString(5, i.getEmail());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener el ID generado para el inquilino.");
    }

    public boolean actualizar(Inquilino i) throws SQLException {
        String sql = "UPDATE Inquilinos SET nombre=?, apellidos=?, dni=?, telefono=?, email=? " +
                     "WHERE id_inquilino=?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, i.getNombre());
            ps.setString(2, i.getApellidos());
            ps.setString(3, i.getDni());
            ps.setString(4, i.getTelefono());
            ps.setString(5, i.getEmail());
            ps.setInt(6, i.getIdInquilino());
            return ps.executeUpdate() > 0;
        }
    }


    public boolean eliminar(int idInquilino) throws SQLException {
        String sql = "DELETE FROM Inquilinos WHERE id_inquilino = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, idInquilino);
            return ps.executeUpdate() > 0;
        }
    }


    private Inquilino mapearFila(ResultSet rs) throws SQLException {
        return new Inquilino(
            rs.getInt("id_inquilino"),
            rs.getString("nombre"),
            rs.getString("apellidos"),
            rs.getString("dni"),
            rs.getString("telefono"),
            rs.getString("email")
        );
    }
}
