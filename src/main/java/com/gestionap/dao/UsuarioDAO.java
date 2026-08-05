package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.Usuario;
import com.gestionap.model.Usuario.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }

    public Usuario autenticar(String email, String passwordHash) throws SQLException {
        String sql = "SELECT id_usuario, nombre, email, rol, activo " +
                     "FROM Usuarios WHERE email = ? AND password_hash = ? AND activo = 1";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearFila(rs);
            }
        }
        return null;
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT id_usuario, nombre, email, rol, activo " +
                     "FROM Usuarios WHERE email = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapearFila(rs);
            }
        }
        return null;
    }

    public boolean actualizarPassword(int idUsuario, String newPasswordHash) throws SQLException {
        String sql = "UPDATE Usuarios SET password_hash = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT id_usuario, nombre, email, rol, activo FROM Usuarios ORDER BY nombre";
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearFila(rs));
        }
        return lista;
    }

    public int insertar(Usuario u, String passwordHash) throws SQLException {
        String sql = "INSERT INTO Usuarios (nombre, email, password_hash, rol, activo) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail().trim().toLowerCase());
            ps.setString(3, passwordHash);
            ps.setString(4, u.getRol().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("No se pudo obtener el ID generado para el usuario.");
    }

    public boolean activarDesactivar(int idUsuario, boolean activo) throws SQLException {
        String sql = "UPDATE Usuarios SET activo = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, activo ? 1 : 0);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarRol(int idUsuario, Rol rol) throws SQLException {
        String sql = "UPDATE Usuarios SET rol = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, rol.name());
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarPerfil(int idUsuario, String nombre, String email) throws SQLException {
        String sql = "UPDATE Usuarios SET nombre = ?, email = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setInt(3, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    private Usuario mapearFila(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setRol(Rol.valueOf(rs.getString("rol")));
        u.setActivo(rs.getInt("activo") == 1);
        return u;
    }
}
