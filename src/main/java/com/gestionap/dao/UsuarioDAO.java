package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.Usuario;
import com.gestionap.model.Usuario.Rol;
import com.gestionap.utils.PasswordUtil;
import com.gestionap.utils.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }

    /**
     * Gestión de usuarios (alta, listado, cambio de rol, activar/desactivar)
     * es una operación de administrador. La UI ya oculta estas acciones a
     * usuarios sin rol admin, pero eso es solo cosmético: cualquier código
     * (o error de UI futuro) podía invocar estos métodos sin control real.
     * Esta comprobación es la que realmente decide.
     */
    private void requireAdmin() {
        Usuario actual = Session.getInstance().getUsuarioActual();
        if (actual == null || actual.getRol() != Rol.admin) {
            throw new SecurityException("Se requiere rol de administrador para esta operación.");
        }
    }

    /**
     * Autentica por email + contraseña en texto plano. Soporta la migración
     * de hashes SHA-256 (legacy) a bcrypt: si el hash almacenado todavía es
     * SHA-256 y la contraseña coincide, se re-hashea a bcrypt en este mismo
     * login antes de devolver el usuario. Si ya es bcrypt, se verifica
     * directo y no se toca la fila.
     */
    public Usuario autenticar(String email, String plainPassword) throws SQLException {
        String sql = "SELECT id_usuario, nombre, email, rol, activo, password_hash " +
                     "FROM Usuarios WHERE email = ? AND activo = 1";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String storedHash = rs.getString("password_hash");
                if (!PasswordUtil.verificar(plainPassword, storedHash)) return null;
                Usuario u = mapearFila(rs);
                if (!PasswordUtil.esBcrypt(storedHash)) {
                    actualizarPassword(u.getIdUsuario(), PasswordUtil.hash(plainPassword));
                }
                return u;
            }
        }
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
        requireAdmin();
        String sql = "SELECT id_usuario, nombre, email, rol, activo FROM Usuarios ORDER BY nombre";
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement ps = getConexion().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearFila(rs));
        }
        return lista;
    }

    public int insertar(Usuario u, String passwordHash) throws SQLException {
        requireAdmin();
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
        requireAdmin();
        String sql = "UPDATE Usuarios SET activo = ? WHERE id_usuario = ?";
        try (PreparedStatement ps = getConexion().prepareStatement(sql)) {
            ps.setInt(1, activo ? 1 : 0);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarRol(int idUsuario, Rol rol) throws SQLException {
        requireAdmin();
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
