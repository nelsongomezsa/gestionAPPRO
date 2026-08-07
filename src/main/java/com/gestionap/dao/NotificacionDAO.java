package com.gestionap.dao;

import com.gestionap.database.DatabaseConnection;
import com.gestionap.model.Notificacion;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAO {

    private Connection getConexion() throws SQLException {
        return DatabaseConnection.getInstance().getConexion();
    }

    // ── Generación automática ────────────────────────────────────

    /**
     * Corre los 4 generadores de forma independiente: si uno falla (p. ej.
     * error SQL puntual), no bloquea a los otros tres, y el fallo queda
     * registrado en vez de desaparecer en silencio.
     */
    public void generarNotificaciones(int idUsuario) {
        ejecutarGenerador("contratos por vencer",   () -> generarContratosVencen(idUsuario));
        ejecutarGenerador("pagos pendientes",       () -> generarPagosPendientes(idUsuario));
        ejecutarGenerador("incidencias antiguas",   () -> generarIncidenciasAntiguas(idUsuario));
        ejecutarGenerador("mantenimiento largo",    () -> generarMantenimientoLargo(idUsuario));
    }

    @FunctionalInterface
    private interface GeneradorNotificacion {
        void ejecutar() throws SQLException;
    }

    private void ejecutarGenerador(String descripcion, GeneradorNotificacion generador) {
        try {
            generador.ejecutar();
        } catch (SQLException e) {
            System.err.println("[NotificacionDAO] No se pudo generar notificaciones de " + descripcion + ": " + e.getMessage());
        }
    }

    private void generarContratosVencen(int idUsuario) throws SQLException {
        String sql = """
            SELECT c.id_contrato,
                   CONCAT(i.nombre, ' ', i.apellidos) AS nombre,
                   c.fecha_fin
            FROM Contratos c
            JOIN Inquilinos i ON c.id_inquilino = i.id_inquilino
            WHERE c.fecha_fin BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)
              AND c.fecha_inicio <= CURDATE()
            """;
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ref = "contrato_vence_" + rs.getInt("id_contrato");
                if (!existeHoy(idUsuario, ref)) {
                    insertar(idUsuario, "contrato_vence",
                             "Contrato próximo a vencer",
                             "Contrato de " + rs.getString("nombre") +
                             " vence el " + rs.getDate("fecha_fin"),
                             ref);
                }
            }
        }
    }

    private void generarPagosPendientes(int idUsuario) throws SQLException {
        String sql = """
            SELECT CONCAT(i.nombre, ' ', i.apellidos) AS nombre, c.id_contrato
            FROM Contratos c
            JOIN Inquilinos i ON c.id_inquilino = i.id_inquilino
            WHERE c.fecha_inicio <= CURDATE() AND c.fecha_fin >= CURDATE()
              AND NOT EXISTS (
                  SELECT 1 FROM Pagos p
                  WHERE p.id_contrato = c.id_contrato
                    AND MONTH(p.fecha_pago) = MONTH(CURDATE())
                    AND YEAR(p.fecha_pago)  = YEAR(CURDATE())
              )
            """;
        String mes = LocalDate.now().getYear() + "_" + LocalDate.now().getMonthValue();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ref = "pago_pendiente_" + rs.getInt("id_contrato") + "_" + mes;
                if (!existeHoy(idUsuario, ref)) {
                    insertar(idUsuario, "pago_pendiente",
                             "Pago pendiente este mes",
                             rs.getString("nombre") + " no tiene pago registrado este mes",
                             ref);
                }
            }
        }
    }

    private void generarIncidenciasAntiguas(int idUsuario) throws SQLException {
        String sql = """
            SELECT id_incidencia, descripcion FROM Incidencias
            WHERE estado IN ('pendiente', 'en_proceso')
              AND fecha <= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
            """;
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ref = "incidencia_antigua_" + rs.getInt("id_incidencia");
                if (!existeHoy(idUsuario, ref)) {
                    String desc = rs.getString("descripcion");
                    if (desc != null && desc.length() > 80) desc = desc.substring(0, 80) + "...";
                    insertar(idUsuario, "incidencia_antigua",
                             "Incidencia sin resolver > 7 días",
                             desc, ref);
                }
            }
        }
    }

    private void generarMantenimientoLargo(int idUsuario) throws SQLException {
        String sql = """
            SELECT h.id_habitacion, h.numero, p.direccion
            FROM Habitaciones h
            JOIN Pisos p ON h.id_piso = p.id_piso
            WHERE h.estado = 'mantenimiento'
              AND NOT EXISTS (
                  SELECT 1 FROM Contratos c
                  WHERE c.id_habitacion = h.id_habitacion
                    AND c.fecha_fin >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
              )
            """;
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ref = "mantenimiento_largo_" + rs.getInt("id_habitacion");
                if (!existeHoy(idUsuario, ref)) {
                    insertar(idUsuario, "mantenimiento_largo",
                             "Habitación en mantenimiento > 30 días",
                             "Hab. " + rs.getInt("numero") + " — " + rs.getString("direccion"),
                             ref);
                }
            }
        }
    }

    private boolean existeHoy(int idUsuario, String referencia) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Notificaciones " +
                     "WHERE id_usuario = ? AND referencia = ? AND DATE(fecha) = CURDATE()";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, referencia);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void insertar(int idUsuario, String tipo, String titulo,
                          String descripcion, String referencia) throws SQLException {
        String sql = "INSERT INTO Notificaciones (tipo, titulo, descripcion, referencia, id_usuario) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tipo);
            ps.setString(2, titulo);
            ps.setString(3, descripcion);
            ps.setString(4, referencia);
            ps.setInt(5, idUsuario);
            ps.executeUpdate();
        }
    }

    // ── Consultas ────────────────────────────────────────────────

    public List<Notificacion> listarNoLeidas(int idUsuario) throws SQLException {
        String sql = "SELECT id_notificacion, tipo, titulo, descripcion, referencia, fecha, leida " +
                     "FROM Notificaciones WHERE id_usuario = ? AND leida = 0 ORDER BY fecha DESC LIMIT 50";
        List<Notificacion> lista = new ArrayList<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs, idUsuario));
            }
        }
        return lista;
    }

    public List<Notificacion> listarTodas(int idUsuario) throws SQLException {
        String sql = "SELECT id_notificacion, tipo, titulo, descripcion, referencia, fecha, leida " +
                     "FROM Notificaciones WHERE id_usuario = ? ORDER BY leida ASC, fecha DESC LIMIT 100";
        List<Notificacion> lista = new ArrayList<>();
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs, idUsuario));
            }
        }
        return lista;
    }

    public int contarNoLeidas(int idUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Notificaciones WHERE id_usuario = ? AND leida = 0";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void marcarTodasLeidas(int idUsuario) throws SQLException {
        String sql = "UPDATE Notificaciones SET leida = 1 WHERE id_usuario = ?";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    public void marcarLeida(int idNotificacion) throws SQLException {
        String sql = "UPDATE Notificaciones SET leida = 1 WHERE id_notificacion = ?";
        try (Connection con = getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idNotificacion);
            ps.executeUpdate();
        }
    }

    private Notificacion mapear(ResultSet rs, int idUsuario) throws SQLException {
        Notificacion n = new Notificacion();
        n.setId(rs.getInt("id_notificacion"));
        n.setTipo(rs.getString("tipo"));
        n.setTitulo(rs.getString("titulo"));
        n.setDescripcion(rs.getString("descripcion"));
        n.setReferencia(rs.getString("referencia"));
        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) n.setFecha(ts.toLocalDateTime());
        n.setLeida(rs.getInt("leida") == 1);
        n.setIdUsuario(idUsuario);
        return n;
    }
}
